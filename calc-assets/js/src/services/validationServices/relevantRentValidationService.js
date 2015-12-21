(function() {
    "use strict";

    var app = require("../module");

    app.service('relevantRentValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");
            var validator = require("../../utilities/validator")();

            if (validator.isNotPopulated(data.relevantRent)) {
                state.relevantRent = "You must complete this box. Enter your Relevant Rent";
            } else if (validator.isInvalidFloat(data.relevantRent)) {
                state.relevantRent = "You have entered an incorrect Relevant Rent, check your entry and correct it";
            }

            return buildState(state);
        };
      
        return {
            validate: validate
        };
    });

}());
