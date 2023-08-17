"use strict";

var gulp = require("gulp");
var runSequence = require("gulp4-run-sequence")

gulp.task("test", async function (cb)
{
    runSequence("karma");
});
