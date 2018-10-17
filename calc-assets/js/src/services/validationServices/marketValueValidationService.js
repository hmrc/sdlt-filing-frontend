(function() {
    "use strict";

    var app = require("../module");

    app.service('marketValueValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");
            var validator = require("../../utilities/validator")();

            if (validator.isNotPopulated(data.paySDLT)) {
                state.paySDLT = "Provide an answer to continue.";
                ga('send', 'event', "userError", "marketValueError", "notPopulated");
            }

            if(data.paySDLT === "Upfront" || data.paySDLT === "Stages") {
                if (validator.isNotPopulated(data.premium)) {
                    state.marketValue = "Provide an answer to continue.";
                    ga('send', 'event', "userError", "marketValueError", "notPopulated");
                }
            }
            if(validator.isLessThanInteger(500000, data.premium)) {
                state.marketValue = "Enter a value that is £500000 or less.";
                ga('send', 'event', "userError", "marketValueError", "notPopulated");
            }
            return buildState(state);
        };

        return {
            validate: validate
        };
    });

}());
