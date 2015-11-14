"use strict";

var config = require("../config");
var gulp = require("gulp");
var KarmaServer = require("karma").Server;

gulp.task("karma", function (cb)
{
    new KarmaServer(config.karmaConfig, cb).start();
});