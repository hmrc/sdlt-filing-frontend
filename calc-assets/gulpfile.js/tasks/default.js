"use strict";

var gulp = require("gulp");
var runSequence = require("run-sequence");

gulp.task("default", function (cb)
{
    runSequence(
        ["jshint", "copyIndex", "templateCache", "webpack", "karma"],
         "uglify",
        cb
    );
});
