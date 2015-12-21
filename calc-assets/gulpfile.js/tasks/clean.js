"use strict";

var config = require("../config");
var del = require("del");
var gulp = require("gulp");

gulp.task("clean", function (cb)
{
    del([
        config.directories.public,
        "node_modules/moment/locale/*.js"
    ], { force: true }, cb);
});