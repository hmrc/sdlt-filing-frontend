(function() {
    "use strict";

    var app = require("./module");

    var loggingService = function() {

        var logEvent = function(category, action, label) {
            ga('send', 'event', category, action, label);
        };

        return {
            logEvent : logEvent
        };
    };

    app.service('loggingService', loggingService);

}());
