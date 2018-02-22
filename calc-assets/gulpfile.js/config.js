"use strict";

var path = require("path");
var jsVersion = "v12";

module.exports =
{
    directories:
    {
        "public": "../public/",
        outputCss: "../public/stylesheets/",
        outputJs: "../public/javascript/",
        outputHTML: "../public/"
    },

    filesMasks:
    {
        allJsSource: "js/**/*.js",
        jsSource: "js/src/**/*.js",
        jsTestSource: "js/test/**/*.js",
        sassSource: "sass/**/*.scss",
        htmlSource: "**/*.html"
    },

    templateCache:
    {
        file: 'javascript/'+jsVersion+'-calc-templates.js',
        options: {
            module: 'calc-templates',
            standalone: true
        }
    },

    karmaConfig:
    {
        browserDisconnectTimeout: 60000,
        browserDisconnectTolerance: 10,
        browserNoActivityTimeout: 60000,
        browsers: ['PhantomJS'],
        captureTimeout: 60000,
        configFile: path.join(__dirname, "../js/test/config/karma.conf.js"),
        port: 9872,
        singleRun: true
    },

    sassConfigDev:
    {
        extension: ".css",
        options:
        {
            style: 'expanded'
        }
    },

    sassConfig:
    {
        extension: ".css",
        options:
        {
            style: 'compressed'
        }
    },

    uglifyConfig:
    {
        mangle: true
    },

    webpackConfig:
    {
        bail: true,
        resolve:
        {
            alias:
            {
//                angular: "angular/angular.min.js",
//                jquery: "jquery/dist/jquery.min.js"
            }
        },
        entry: ["./js/src/app.js"],
        output:
        {
            filename: "../public/javascript/"+jsVersion+"-calc.js"
        },
        externals: ["angular"]
    },

    jshintConfig:
    {
        reportPath: "../target/jshint-reports/",
        reportName: "jshint.xml"
    }
};
