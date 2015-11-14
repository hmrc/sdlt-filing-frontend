"use strict";

var config = require("../config");
var gulp = require("gulp");
var templateCache = require("gulp-angular-templatecache");

gulp.task("templateCache", function ()
{
    return gulp.src('templates/**/*.html')
        .pipe(templateCache(config.templateCache.file, config.templateCache.options))
        .pipe(gulp.dest(config.directories.outputHTML));
});