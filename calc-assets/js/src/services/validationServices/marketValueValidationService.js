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
            }

            if(data.paySDLT === "Using market value election" || data.paySDLT === "Stages") {
                if (validator.isNotPopulated(data.premium)) {
                    state.marketValue = "Provide an answer to continue.";
                } else if (validator.isInvalidFloat(data.premium)) {
                    state.marketValue = "Enter the amount again - don't use any letters or characters including £";
                }
            }

            if(validator.isGreaterThanOrEqualToDate(data.effectiveDate, new Date(2022, 8, 23))) {
                if(validator.isLessThanInteger(625000, data.premium)) {
                    state.marketValue = "Enter a value that is £625000 or less.";
                }
            } else if(validator.isLessThanInteger(500000, data.premium)) {
                state.marketValue = "Enter a value that is £500000 or less.";
            }
            
            return buildState(state);
        };

        return {
            validate: validate
        };
    });

}());
