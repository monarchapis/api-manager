import sbt._
import Keys._

object BuildSettings {
  import Dependencies._
  import Resolvers._

  val buildOrganization = "com.monarchapis"
  val buildVersion = "0.8.3"
  val buildScalaVersion = "2.11.6"

  val globalSettings = Seq(
    organization := buildOrganization,
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    scalacOptions += "-deprecation",
    fork in test := true,
    libraryDependencies ++= Seq(
      junit, scalatest, mockito,
      jodaTime, jodaConvert),
    resolvers := Seq(
      scalaToolsRepo,
      sonatypeRepo,
      ecwidRepo))

  val projectSettings = Defaults.defaultSettings ++ globalSettings
}

object Resolvers {
  val sonatypeRepo = "Sonatype Release" at "http://oss.sonatype.org/content/repositories/releases"
  val scalaToolsRepo = "Scala Tools" at "http://scala-tools.org/repo-snapshots/"
  val ecwidRepo = "ECWID" at "http://nexus.ecwid.com/content/groups/public"
}

object Dependencies {
  val junit = "junit" % "junit" % "4.12" % "test"
  val scalatest = "org.scalatest" % "scalatest_2.11" % "2.2.5" % "test"
  val mockito = "org.mockito" % "mockito-all" % "1.10.19" % "test"

  val slf4jVersion = "1.7.12"
  val slf4jSimple = "org.slf4j" % "slf4j-simple" % slf4jVersion
  val slf4jlog4j12 = "org.slf4j" % "slf4j-log4j12" % slf4jVersion
  val slf4jSimpleTest = slf4jSimple % "test"
  val log4j = "log4j" % "log4j" % "1.2.17"
  val grizzled = "org.clapper" % "grizzled-slf4j_2.11" % "1.0.2"

  val springVersion = "4.1.6.RELEASE"
  val springGroupId = "org.springframework"
  val springCore = springGroupId % "spring-core" % springVersion
  val springBeans = springGroupId % "spring-beans" % springVersion
  val springContext = springGroupId % "spring-context" % springVersion
  val springContextSupport = springGroupId % "spring-context-support" % springVersion
  val springWeb = springGroupId % "spring-web" % springVersion
  val springTx = springGroupId % "spring-tx" % springVersion
  val javaxInject = "javax.inject" % "javax.inject" % "1"

  val validationApi = "javax.validation" % "validation-api" % "1.1.0.Final"
  val hibernateValidator = "org.hibernate" % "hibernate-validator" % "5.1.3.Final"

  val jodaTime = "joda-time" % "joda-time" % "2.8"
  val jodaConvert = "org.joda" % "joda-convert" % "1.7"

  val commonsLang3 = "org.apache.commons" % "commons-lang3" % "3.4"
  val commonsIo = "commons-io" % "commons-io" % "2.4"
  val commonsCodec = "commons-codec" % "commons-codec" % "1.10"

  val jacksonVersion = "2.5.2"
  val jacksonGroupId = "com.fasterxml.jackson.core"
  val jacksonCore = jacksonGroupId % "jackson-core" % jacksonVersion
  val jacksonDatabind = jacksonGroupId % "jackson-databind" % jacksonVersion
  val jacksonAnnotations = jacksonGroupId % "jackson-annotations" % jacksonVersion
  val jacksonJaxRs = "com.fasterxml.jackson.jaxrs" % "jackson-jaxrs-json-provider" % jacksonVersion
  val jacksonScala = "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % jacksonVersion
  val jacksonJoda = "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % jacksonVersion
  val jacksonYaml = "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % jacksonVersion

  val jaxrs = "javax.ws.rs" % "javax.ws.rs-api" % "2.0.1"
  val jerseyVersion = "2.17"
  val jerseyServer = "org.glassfish.jersey.core" % "jersey-server" % jerseyVersion
  val jerseySpring = "org.glassfish.jersey.ext" % "jersey-spring3" % jerseyVersion excludeAll(
      ExclusionRule(organization = "org.springframework"))

  val jettyVersion = "8.1.15.v20140411" //"9.2.1.v20140609"
  val jettyServer = "org.eclipse.jetty" % "jetty-server" % jettyVersion
  val jettyServlet = "org.eclipse.jetty" % "jetty-servlet" % jettyVersion
  val jettyAjp = "org.eclipse.jetty" % "jetty-ajp" % "8.1.15.v20140411"
  val jettyServerTest = jettyServer % "test"
  val jettyWebApp = "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % "compile"

  val servletSpec = "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"

  val casbahCore = "org.mongodb" % "casbah-core_2.11" % "2.8.1"

  val jasypt = "org.jasypt" % "jasypt-spring31" % "1.9.2"

  val antlr = "org.antlr" % "antlr4-runtime" % "4.5"
  val maxmind = "com.maxmind.geoip2" % "geoip2" % "2.2.0"

  val ehcache = "net.sf.ehcache" % "ehcache" % "2.10.0"

  val quartz = "org.quartz-scheduler" % "quartz" % "2.2.1"

  val guava = "com.google.guava" % "guava" % "18.0"
  val gson = "com.google.code.gson" % "gson" % "2.3.1"
  val httpclient = "org.apache.httpcomponents" % "httpclient" % "4.4.1"
  val consulApi = "com.ecwid.consul" % "consul-api" % "1.1.0"

  val jjwt = "io.jsonwebtoken" % "jjwt" % "0.5"
}

object ApiPlatformBuild extends Build {
  import BuildSettings._
  import Dependencies._
  import Resolvers._

  override lazy val settings = super.settings ++ globalSettings

  val commonDeps = Seq(
    servletSpec, jaxrs,
    validationApi, hibernateValidator, commonsLang3,
    jacksonCore, jacksonDatabind)

  val serverDeps = Seq(
    log4j, slf4jlog4j12, grizzled,
    servletSpec,
    validationApi, hibernateValidator,
    commonsLang3, commonsIo, commonsCodec,
    springCore, springBeans, springContext, springContextSupport, springWeb, springTx, javaxInject,
    jacksonCore, jacksonDatabind, jacksonAnnotations, jacksonJaxRs, jacksonScala, jacksonJoda, jacksonYaml,
    jerseyServer, jerseySpring,
    casbahCore, jasypt, antlr, maxmind, ehcache, quartz, guava, gson, httpclient, consulApi, jjwt)

  var jettyApp = Seq(jettyWebApp)
  var jettyDeps = Seq(jettyServer, jettyServlet, jettyAjp)

  lazy val apiManager = Project(
    id = "api-manager-standalone",
    base = file("."),
    settings = projectSettings ++
      Seq(libraryDependencies ++= commonDeps ++ jettyApp ++ jettyDeps)) dependsOn (common, server)

  lazy val common = Project(
    id = "api-manager-common",
    base = file("modules/common"),
    settings = projectSettings ++
      Seq(libraryDependencies ++= commonDeps))

  lazy val server = Project(
    id = "api-manager-server",
    base = file("modules/server"),
    settings = projectSettings ++
      Seq(libraryDependencies ++= commonDeps ++ serverDeps)) dependsOn (common)
}
