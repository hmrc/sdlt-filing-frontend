"use strict";

var config = require("../config");
var gulp = require("gulp");
var runSequence = require("gulp4-run-sequence")
var watch = require("gulp-watch");

gulp.task("build-js", function (cb)
{
    runSequence("webpack", ["jshint", "karma"], cb);
});

gulp.task("test-js", function (cb)
{
    runSequence(["jshint", "karma"], cb);
});

gulp.task("watch", function ()
{
    watch(config.filesMasks.jsSource, function ()
    {
        gulp.start("build-js");
    });
    watch(config.filesMasks.jsTestSource, function ()
    {
        gulp.start("test-js");
    });
});
