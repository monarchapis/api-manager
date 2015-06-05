import org.rbayer.GruntSbtPlugin._
import GruntKeys._
import com.typesafe.sbt.SbtNativePackager._
import NativePackagerHelper._

name := "api-manager"

version := "0.8.3"

scalaVersion := "2.11.6"

unmanagedSourceDirectories in Compile <<= (scalaSource in Compile)(Seq(_))

packageArchetype.java_application

gruntSettings

name in Universal := "monarch-" + version.value

mappings in Universal ++= directory("src/main/scripts") map {case (f, s) => (f, s.replaceFirst("scripts", "bin"))}

mappings in Universal ++= directory("src/main/conf")

mappings in Universal ++= directory("src/main/logs")

mappings in Universal ++= directory("src/main/webapp")