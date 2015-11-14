"use strict";

var config = require("../config");
var gulp = require("gulp");

gulp.task("copyIndex", function ()
{
    return gulp.src('*.html')
        .pipe(gulp.dest(config.directories.public));
});
