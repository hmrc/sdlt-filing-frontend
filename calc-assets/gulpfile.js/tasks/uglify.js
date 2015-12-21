"use strict";

var config = require("../config");
var gulp = require("gulp");
var uglify = require("gulp-uglify");

gulp.task("uglify", function ()
{
    return gulp.src(config.webpackConfig.output.filename)
        .pipe(uglify(config.uglifyConfig))
        .pipe(gulp.dest(config.directories.outputJs));
});