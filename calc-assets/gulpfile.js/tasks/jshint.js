"use strict";

var config = require("../config");
var gulp = require("gulp");
var jshint = require("gulp-jshint");
var stylish = require("jshint-stylish");
var jshintXMLReporter = require('gulp-jshint-xml-file-reporter');

gulp.task("jshint", function ()
{
    var mkdirp = require('mkdirp');
    mkdirp(config.jshintConfig.reportPath, function(err) { });

    return gulp.src([
            config.filesMasks.allJsSource,
            "!js/bower_components/**/*",
            "!js/libs/**/*",
            "!js/test/specs/helpers/jasmine-beforeAll.js",
            "!js/test/specs/helpers/jasmine-matchers.js",
            "!js/Gruntfile.js"
        ])
        .pipe(jshint())
        .pipe(jshint.reporter(stylish))
        .pipe(jshint.reporter(jshintXMLReporter))
        .pipe(jshint.reporter('fail')
        .on('end', jshintXMLReporter.writeFile({
            format: 'checkstyle',
            filePath: config.jshintConfig.reportPath+config.jshintConfig.reportName,
            alwaysReport: true
        })));
});
