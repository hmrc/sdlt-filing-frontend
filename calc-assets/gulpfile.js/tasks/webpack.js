"use strict";

var config = require("../config");
var gulp = require("gulp");
var gutil = require("gulp-util");
var webpack = require("webpack");

gulp.task("webpack", function (cb)
{
    webpack(config.webpackConfig,
        function (err, stats)
        {
            if (err)
            {
                throw new gutil.PluginError("webpack", err);
            }

            gutil.log("[webpack]", stats.toString({
                // output options
            }));

            cb();
        });
});