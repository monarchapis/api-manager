/*
 * Copyright (C) 2015 CapTech Ventures, Inc.
 * (http://www.captechconsulting.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.monarchapis.apimanager.service.ldap

import java.text.MessageFormat
import java.util.Hashtable

import org.apache.commons.codec.binary.Hex
import org.apache.commons.lang3.StringUtils

import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._

import grizzled.slf4j.Logging
import javax.naming.AuthenticationException
import javax.naming.Context
import javax.naming.NamingEnumeration
import javax.naming.directory.InitialDirContext
import javax.naming.directory.SearchControls
import javax.naming.directory.SearchResult

class LdapAuthenticationService(
  ldapURL: String,
  authMethod: String = "Simple",
  userDN: String,
  uidAttribute: String,
  baseDN: String,
  useSSL: Boolean = false,
  firstNameAttribute: String = "givenname",
  lastNameAttribute: String = "sn",
  idAttribute: String = "objectGUID",
  idIsBinary: Boolean = true,
  userGroups: List[String] = List(),
  adminGroups: List[String] = List())
  extends AuthenticationService with Logging {

  def this(ldapURL: String,
    authMethod: String,
    userDN: String,
    uidAttribute: String,
    baseDN: String,
    useSSL: Boolean,
    firstNameAttribute: String,
    lastNameAttribute: String,
    idAttribute: String,
    idIsBinary: Boolean,
    userGroups: String,
    adminGroups: String) = {
    this(
      ldapURL,
      authMethod,
      userDN,
      uidAttribute,
      baseDN,
      useSSL,
      firstNameAttribute,
      lastNameAttribute,
      idAttribute,
      idIsBinary,
      StringUtils.split(StringUtils.trimToEmpty(userGroups), "|") map (v => v.trim) toList,
      StringUtils.split(StringUtils.trimToEmpty(adminGroups), "|") map (v => v.trim) toList)
  }

  require(ldapURL != null, "ldapURL is required")
  require(authMethod != null, "authMethod is required")
  require(userDN != null, "userDN is required")
  require(uidAttribute != null, "uidAttribute is required")
  require(baseDN != null, "baseDN is required")
  require(firstNameAttribute != null, "firstNameAttribute is required")
  require(lastNameAttribute != null, "lastNameAttribute is required")
  require(idAttribute != null, "idAttribute is required")
  require(adminGroups != null, "adminGroups is required")

  import LdapAuthenticationService._

  info(s"$this")

  def authenticate(userName: String, password: String): Option[AuthenticatedUser] = {
    require(userName != null, "userName is required")
    require(password != null, "password is required")

    // Set up the environment for creating the initial context
    val env = new Hashtable[String, String]
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
    env.put(Context.PROVIDER_URL, ldapURL)

    // Specify SSL
    if (useSSL) {
      env.put(Context.SECURITY_PROTOCOL, "ssl")
    }

    // Authenticate as S. User and password
    env.put(Context.SECURITY_AUTHENTICATION, authMethod)
    val principalDN = MessageFormat.format(userDN, userName)

    env.put(Context.SECURITY_PRINCIPAL, principalDN)
    env.put(Context.SECURITY_CREDENTIALS, password)

    if (idIsBinary) {
      env.put("java.naming.ldap.attributes.binary", idAttribute)
    }

    var ctx: InitialDirContext = null
    var results: NamingEnumeration[SearchResult] = null

    // Create the initial context
    try {
      ctx = new InitialDirContext(env)

      val searchControls = new SearchControls()
      searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE)
      searchControls.setTimeLimit(10000)
      val attrIDs = Array(uidAttribute, firstNameAttribute, lastNameAttribute, idAttribute)
      searchControls.setReturningAttributes(attrIDs);

      // Perform the search
      results = ctx.search(
        "OU=Offices,DC=captechventures,DC=com",
        "(&(objectclass=user)(" + uidAttribute + "=" + userName + "))", searchControls)

      if (results != null && results.hasMore()) {
        val entry = results.next.asInstanceOf[SearchResult]
        val attrs = entry.getAttributes

        val id = if (idIsBinary) {
          val guid = attrs.get(idAttribute).get.asInstanceOf[Array[Byte]]
          Hex.encodeHexString(guid)
        } else {
          attrs.get(idAttribute).get.asInstanceOf[String]
        }

        val userName = attrs.get(uidAttribute).get.asInstanceOf[String]
        val firstName = attrs.get(firstNameAttribute).get.asInstanceOf[String]
        val lastName = attrs.get(lastNameAttribute).get.asInstanceOf[String]

        val administrator = inGroups(ctx, userName, adminGroups)
        val isUser = administrator || userGroups.isEmpty || inGroups(ctx, userName, userGroups)

        if (!isUser) {
          None
        } else {
          Some(AuthenticatedUser(
            id = id,
            userName = userName,
            firstName = firstName,
            lastName = lastName,
            administrator = administrator))
        }
      } else {
        None
      }
    } catch {
      case ae: AuthenticationException => None
      case e: Exception => {
        debug(s"Failure authenticating user $userName", e);
        None
      }
    } finally {
      if (results != null) {
        results.close
      }

      if (ctx != null) {
        ctx.close
      }
    }
  }

  private def inGroups(ctx: InitialDirContext, username: String, groupList: List[String]): Boolean = {
    require(baseDN != null, "baseDN is required")
    require(uidAttribute != null, "uidAttribute is required")

    if (groupList == null || groupList.isEmpty) {
      return true
    }

    var results: NamingEnumeration[SearchResult] = null

    try {

      for (group <- groupList) {
        debug(s"Testing recursively for user $username in group $group")
        val filter = "(&(" + uidAttribute + "={0})(memberOf:1.2.840.113556.1.4.1941:={1}))"
        val searchURL = ldapURL + "/" + baseDN
        val ctls = new SearchControls()
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE)
        results = ctx.search(searchURL, filter, Array[Object](username, group), ctls)

        if (results.hasMore()) {
          debug(s"User $username is in group $group")
          return true
        }
      }

      false
    } finally {
      if (results != null) {
        results.close
      }
    }
  }

  def setPassword(userName: String, password: String) {
    throw new IllegalStateException("Passwords cannot be set via this authentication provider")
  }

  override def toString = s"LdapAuthenticationService($ldapURL)"
}