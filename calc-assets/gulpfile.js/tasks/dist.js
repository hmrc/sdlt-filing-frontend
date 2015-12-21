"use strict";

var gulp = require("gulp");
var runSequence = require("run-sequence");

gulp.task("dist", function (cb)
{
    runSequence(
        "clean",
//        ["jshint", "webpack", "sass-dist"],
        ["jshint", "copyIndex", "templateCache", "webpack"],
        "uglify",
        cb
    );
});
