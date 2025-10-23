import sbt._
import sbt.Keys._
import com.typesafe.sbt.packager.Keys._
import play.sbt.PlayImport.PlayKeys
import sys.process._


/**
 * Build of UI in JavaScript
 */
object JavaScriptBuild {



  val uiDirectory = SettingKey[File]("ui-directory")

  val gulpBuild = TaskKey[Int]("gulp-build")
  val gulpWatch = TaskKey[Int]("gulp-watch")
  val gulpTest = TaskKey[Int]("gulp-test")
  val npmInstall = TaskKey[Int]("npm-install")


  val javaScriptUiSettings = Seq(

    // the JavaScript application resides in "ui"
    uiDirectory := (Compile / baseDirectory) { _ /"calc-assets" }.value,

    // add "npm" and "gulp" commands in sbt
    commands ++= uiDirectory { base => Seq(Gulp.gulpCommand(base), npmCommand(base))}.value,

    npmInstall := {
      val result = Gulp.npmProcess(uiDirectory.value, "ci").run().exitValue()
      if (result != 0)
        throw new Exception("Npm install failed.")
      result
    },
    gulpBuild := {
      val result = Gulp.gulpProcess(uiDirectory.value, "dist").run().exitValue()
      if (result != 0)
        throw new Exception("Gulp build failed.")
      result
    },

    gulpTest := {
      val result = Gulp.gulpProcess(uiDirectory.value, "test").run().exitValue()
      if (result != 0)
        throw new Exception("Gulp test failed.")
      result
    },

    gulpTest := (gulpTest dependsOn npmInstall).value,
    gulpBuild := (gulpBuild dependsOn npmInstall).value,

    // runs gulp before staging the application
    dist := (dist dependsOn gulpBuild).value,
    (Test / test) := ((Test / test) dependsOn gulpTest).value,


    // integrate JavaScript build into play build
    PlayKeys.playRunHooks += uiDirectory.map(ui => Gulp(ui)).value
  )

  def npmCommand(base: File) = Command.args("npm", "<npm-command>") { (state, args) =>
    if (sys.props("os.name").toLowerCase contains "windows") {
      Process("cmd" :: "/c" :: "npm" :: args.toList, base) !
    }
    else {
      Process("npm" :: args.toList, base) !
    }
    state
  }

}
