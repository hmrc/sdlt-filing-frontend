var path = require("path");
var webdriverConfig = {
    "hostname": "localhost",
    "port": 4444
};

module.exports = function (config)
{
    config.set({
        // base path, that will be used to resolve files and exclude
        basePath: "../..",

        frameworks: ["jasmine"],

        // list of files / patterns to load in the browser
        files: [
            "../node_modules/jquery/dist/jquery.js",
            "../node_modules/angular/angular.js",
            "../node_modules/angular-route/angular-route.js",
            "../node_modules/angular-sanitize.js",
            "test/specs/**/*.js"
        ],

        // list of files to exclude
        exclude: [],

        preprocessors: {
            "js/src/**/*.js": ["webpack", "coverage"],
            "test/specs/**/*.js": ["webpack"]
        },

        // use dots reporter, as travis terminal does not support escaping sequences
        // possible values: "dots", "progress"
        // CLI --reporters progress
        reporters: ["spec", "junit", "coverage"],

        junitReporter: {
            // will be resolved to basePath (in the same way as files/exclude patterns)
            outputDir: "../../target/test-reports/"
        },

        // web server port
        // CLI --port 9876
        port: 6001,

        // enable / disable colors in the output (reporters and logs)
        // CLI --colors --no-colors
        colors: true,

        // level of logging
        // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
        // CLI --log-level debug
        logLevel: config.LOG_DEBUG,

        // enable / disable watching file and executing tests whenever any file changes
        // CLI --auto-watch --no-auto-watch
        autoWatch: false,

        // Start these browsers, currently available:
        // - Chrome
        // - ChromeCanary
        // - Firefox
        // - Opera
        // - Safari (only Mac)
        // - PhantomJS
        // - IE (only Windows)
        // CLI --browsers Chrome,Firefox,Safari
        browsers: [],

        // If browser does not capture in given timeout [ms], kill it
        // CLI --capture-timeout 5000
        captureTimeout: 20000,

        // Auto run tests on start (when browsers are captured) and exit
        // CLI --single-run --no-single-run
        singleRun: true,

        // report which specs are slower than 500ms
        // CLI --report-slower-than 500
        reportSlowerThan: 500,

        coverageReporter: {
            type: "html",
            dir: "../../target/coverage-reports/",
            check: {
                emitWarning: true,
                global: {
                    statements: 100,
                    functions: 100,
                    branches: 100,
                    lines: 100
                }
            }
        },

        customLaunchers: {
            chrome: {
                browserName: "chrome",
                base: "WebDriver",
                config: webdriverConfig,
                debug: true
            }
        },

        plugins: [
            "karma-coverage",
            "karma-jasmine",
            "karma-webdriver-launcher",
            "karma-junit-reporter",
            "karma-spec-reporter",
            "karma-webpack"
        ],

        webpack: {
            module: {
                rules: [
                    // instrument only testing sources with Istanbul
                    {
                        test: /\.js$/,
                        enforce: "pre",
                        exclude: /(node_modules|libs\/|test\/)/,
                        loader: 'istanbul-instrumenter-loader'
                    }
                ]
            },
            resolve: {
                modules: [
                    path.join(__dirname, "../../../js/src"),
                    "node_modules"
                ]
            }
        },

        webpackMiddleware: {
            noInfo: true
        }
    });
};
