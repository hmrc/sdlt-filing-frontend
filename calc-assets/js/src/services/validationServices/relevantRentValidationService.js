(function() {
    "use strict";

    var app = require("../module");

    app.service('relevantRentValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");
            var validator = require("../../utilities/validator")();

            if (validator.isNotPopulated(data.relevantRent)) {
                state.relevantRent = "Please enter the rental figure";
            } else if (validator.isInvalidFloat(data.relevantRent)) {
                state.relevantRent = "Enter the relevant rent again - don't use any letters or characters including £";
            }

            return buildState(state);
        };
      
        return {
            validate: validate
        };
    });

}());
