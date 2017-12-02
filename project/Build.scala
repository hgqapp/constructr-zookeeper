import sbt._
import sbt.plugins.JvmPlugin
import sbt.Keys._
import com.typesafe.sbt.SbtScalariform
import de.heikoseeberger.sbtheader.HeaderPlugin
import de.heikoseeberger.sbtheader.license.Apache2_0
import sbtrelease.ReleasePlugin.autoImport._
import com.typesafe.sbt.SbtPgp.autoImport._
import PgpKeys._
import xerial.sbt.Sonatype.autoImport._
import ReleaseTransformations._

import scalariform.formatter.preferences.{AlignSingleLineCaseStatements, DoubleIndentClassDeclaration}

object Build extends AutoPlugin {

  override def requires = JvmPlugin && HeaderPlugin && SbtScalariform

  override def trigger = allRequirements

  override def projectSettings = Vector(
    // Core settings
    organization := "com.lightbend.constructr",
    scalaVersion := Version.scala212,
    crossScalaVersions := Vector(Version.scala212, Version.scala211),
    scalacOptions ++= Vector(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-target:jvm-1.8",
      "-encoding", "UTF-8",
      "-Ywarn-unused-import"
    ),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    homepage := Some(url("https://github.com/typesafehub/constructr-zookeeper")),
    developers := List(
      Developer("lightbend", "Lightbend Library Contributors", "", url("https://github.com/typesafehub/constructr-zookeeper/graphs/contributors"))
    ),
    scmInfo := Some(ScmInfo(url("https://github.com/typesafehub/constructr-zookeeper"), "git@github.com:typesafehub/constructr-zookeeper.git")),
    unmanagedSourceDirectories.in(Compile) := Vector(scalaSource.in(Compile).value),
    unmanagedSourceDirectories.in(Test) := Vector(scalaSource.in(Test).value),


    // Scalariform settings
    SbtScalariform.autoImport.scalariformPreferences := SbtScalariform.autoImport.scalariformPreferences.value
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
      .setPreference(DoubleIndentClassDeclaration, true),

    // Header settings
    HeaderPlugin.autoImport.headers := Map(
      "scala" -> Apache2_0("2016", "Lightbend Inc. <http://www.lightbend.com>"),
      "conf"  -> Apache2_0("2016", "Lightbend Inc. <http://www.lightbend.com>", "#")
    ),

    // Sonatype settings
    sonatypeProfileName := "com.lightbend",

    // Release settings
    releaseCrossBuild := true,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      releaseStepCommand("run-tests"),
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      ReleaseStep(action = Command.process("publishSigned", _), enableCrossBuild = true),
      setNextVersion,
      commitNextVersion,
      ReleaseStep(action = Command.process("sonatypeReleaseAll", _), enableCrossBuild = true),
      pushChanges
    )

    // FIXME
    // For some reason, +test results in tests failing due to bincompat issues, explicitly
    // issuing clean seems to work around it for now
  ) ++ addCommandAlias("run-tests", s";++${Version.scala211};clean;test;++${Version.scala212};clean;test")
}
