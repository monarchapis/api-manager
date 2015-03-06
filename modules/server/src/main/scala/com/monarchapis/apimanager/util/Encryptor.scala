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

package com.monarchapis.apimanager.util

import java.security.SecureRandom
import java.util.Arrays
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec

object Encryptor {
  /**
   * name of the character set to use for converting between characters and
   * bytes
   */
  private val CHARSET_NAME = "UTF-8"

  /** random number generator algorithm */
  private val RNG_ALGORITHM = "SHA1PRNG"

  /**
   * message digest algorithm (must be sufficiently long to provide the key
   * and initialization vector)
   */
  private val DIGEST_ALGORITHM = "SHA-256"

  /** key algorithm (must be compatible with CIPHER_ALGORITHM) */
  private val KEY_ALGORITHM = "AES"

  /** cipher algorithm (must be compatible with KEY_ALGORITHM) */
  private val CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding"

  private val ZERO: Byte = 0

  def createSalt(length: Int): Array[Byte] = {
    /* generate salt randomly */
    val salt = new Array[Byte](length)
    SecureRandom.getInstance(RNG_ALGORITHM).nextBytes(salt)
    salt
  }

  /**
   * Encrypt the specified cleartext using the given password. With the
   * correct salt, number of iterations, and password, the decrypt() method
   * reverses the effect of this method. This method generates and uses a
   * random salt, and the user-specified number of iterations and password to
   * create a 16-byte secret key and 16-byte initialization vector. The secret
   * key and initialization vector are then used in the AES-128 cipher to
   * encrypt the given cleartext.
   *
   * @param salt
   *            salt that was used in the encryption (to be populated)
   * @param iterations
   *            number of iterations to use in salting
   * @param password
   *            password to be used for encryption
   * @param cleartext
   *            cleartext to be encrypted
   * @return ciphertext
   * @throws Exception
   *             on any error encountered in encryption
   */
  def encrypt(salt: Array[Byte], iterations: Int, password: String,
    cleartext: Array[Byte]): Array[Byte] = {
    val pw = password.getBytes(CHARSET_NAME)
    val ret = encrypt(salt, iterations, pw, cleartext)
    Arrays.fill(pw, ZERO)

    ret
  }

  /**
   * Encrypt the specified cleartext using the given password. With the
   * correct salt, number of iterations, and password, the decrypt() method
   * reverses the effect of this method. This method generates and uses a
   * random salt, and the user-specified number of iterations and password to
   * create a 16-byte secret key and 16-byte initialization vector. The secret
   * key and initialization vector are then used in the AES-128 cipher to
   * encrypt the given cleartext.
   *
   * @param salt
   *            salt that was used in the encryption (to be populated)
   * @param iterations
   *            number of iterations to use in salting
   * @param password
   *            password to be used for encryption
   * @param cleartext
   *            cleartext to be encrypted
   * @return ciphertext
   * @throws Exception
   *             on any error encountered in encryption
   */
  def encrypt(salt: Array[Byte], iterations: Int, password: Array[Byte],
    cleartext: Array[Byte]): Array[Byte] = {
    /* compute key and initialization vector */
    val shaDigest = MessageDigest.getInstance(DIGEST_ALGORITHM)
    var pw = password
    var i = 0;

    for (i <- 0 until iterations) {
      if (salt != null) {
        /* add salt */
        val salted = new Array[Byte](pw.length + salt.length)
        System.arraycopy(pw, 0, salted, 0, pw.length)
        System.arraycopy(salt, 0, salted, pw.length, salt.length)

        if (pw != password) {
          Arrays.fill(pw, ZERO)
        }

        /* compute SHA-256 digest */
        shaDigest.reset()
        pw = shaDigest.digest(salted)
        Arrays.fill(salted, ZERO)
      } else {
        /* compute SHA-256 digest */
        shaDigest.reset()
        val temp = shaDigest.digest(pw)

        if (pw != password) {
          Arrays.fill(pw, ZERO)
        }

        pw = temp
      }
    }

    /*
	 * extract the 16-byte key and initialization vector from the SHA-256
	 * digest
	 */
    val key = new Array[Byte](16)
    val iv = new Array[Byte](16)
    System.arraycopy(pw, 0, key, 0, 16)
    System.arraycopy(pw, 16, iv, 0, 16)

    if (pw != password) {
      Arrays.fill(pw, ZERO)
    }

    /* perform AES-128 encryption */
    val cipher = Cipher.getInstance(CIPHER_ALGORITHM);

    cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, KEY_ALGORITHM),
      new IvParameterSpec(iv));

    Arrays.fill(key, ZERO)
    Arrays.fill(iv, ZERO)

    cipher.doFinal(cleartext)
  }

  /**
   * Decrypt the specified ciphertext using the given password. With the
   * correct salt, number of iterations, and password, this method reverses
   * the effect of the encrypt() method. This method uses the user-specified
   * salt, number of iterations, and password to recreate the 16-byte secret
   * key and 16-byte initialization vector. The secret key and initialization
   * vector are then used in the AES-128 cipher to decrypt the given
   * ciphertext.
   *
   * @param salt
   *            salt to be used in decryption
   * @param iterations
   *            number of iterations to use in salting
   * @param password
   *            password to be used for decryption
   * @param ciphertext
   *            ciphertext to be decrypted
   * @return cleartext
   * @throws Exception
   *             on any error encountered in decryption
   */
  def decrypt(salt: Array[Byte], iterations: Int, password: String,
    ciphertext: Array[Byte]): Array[Byte] = {
    val pw = password.getBytes(CHARSET_NAME)
    val ret = decrypt(salt, iterations, pw, ciphertext)
    Arrays.fill(pw, ZERO)

    ret
  }

  /**
   * Decrypt the specified ciphertext using the given password. With the
   * correct salt, number of iterations, and password, this method reverses
   * the effect of the encrypt() method. This method uses the user-specified
   * salt, number of iterations, and password to recreate the 16-byte secret
   * key and 16-byte initialization vector. The secret key and initialization
   * vector are then used in the AES-128 cipher to decrypt the given
   * ciphertext.
   *
   * @param salt
   *            salt to be used in decryption
   * @param iterations
   *            number of iterations to use in salting
   * @param password
   *            password to be used for decryption
   * @param ciphertext
   *            ciphertext to be decrypted
   * @return cleartext
   * @throws Exception
   *             on any error encountered in decryption
   */
  def decrypt(salt: Array[Byte], iterations: Int, password: Array[Byte],
    ciphertext: Array[Byte]): Array[Byte] = {
    /* compute key and initialization vector */
    val shaDigest = MessageDigest.getInstance(DIGEST_ALGORITHM)
    var pw = password

    for (i <- 0 until iterations) {
      if (salt != null) {
        /* add salt */
        val salted = new Array[Byte](pw.length + salt.length)
        System.arraycopy(pw, 0, salted, 0, pw.length)
        System.arraycopy(salt, 0, salted, pw.length, salt.length)

        if (pw != password) {
          Arrays.fill(pw, ZERO);
        }

        /* compute SHA-256 digest */
        shaDigest.reset()
        pw = shaDigest.digest(salted)
        Arrays.fill(salted, ZERO)
      } else {
        /* compute SHA-256 digest */
        shaDigest.reset()
        val temp = shaDigest.digest(pw)

        if (pw != password) {
          Arrays.fill(pw, ZERO)
        }

        pw = temp;
      }
    }

    /*
	 * extract the 16-byte key and initialization vector from the SHA-256
	 * digest
	 */
    val key = new Array[Byte](16)
    val iv = new Array[Byte](16)
    System.arraycopy(pw, 0, key, 0, 16)
    System.arraycopy(pw, 16, iv, 0, 16)

    if (pw != password) {
      Arrays.fill(pw, ZERO)
    }

    /* perform AES-128 decryption */
    val cipher = Cipher.getInstance(CIPHER_ALGORITHM)

    cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, KEY_ALGORITHM),
      new IvParameterSpec(iv))

    Arrays.fill(key, ZERO)
    Arrays.fill(iv, ZERO)

    cipher.doFinal(ciphertext)
  }
}