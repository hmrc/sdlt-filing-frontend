"use strict";

var gulp = require("gulp");
var runSequence = require("gulp4-run-sequence")

gulp.task("dist", function (cb)
{
    runSequence(
        ["jshint", "copyIndex", "templateCache", "webpack"],
        "uglify",
        cb
    );
});
