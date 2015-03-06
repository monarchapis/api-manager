resolvers += Classpaths.typesafeResolver

resolvers += Classpaths.sbtPluginReleases

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.5.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.7.4")

addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.9.0")

addSbtPlugin("org.rbayer" % "grunt-sbt" % "1.0")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.0.4")
