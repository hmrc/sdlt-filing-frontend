"use strict";

var config = require("../config");
var gulp = require("gulp");
var replace = require("gulp-replace");
var templateCache = require("gulp-angular-templatecache");

gulp.task("templateCache", function ()
{
    return gulp.src('templates/**/*.html')
        .pipe(templateCache(config.templateCache.file, config.templateCache.options))
        .pipe(replace('put(\'/', 'put(\''))
        .pipe(gulp.dest(config.directories.outputHTML));
});