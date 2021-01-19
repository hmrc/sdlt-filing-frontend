(function() {
    "use strict";

    var app = require("../module");

    app.service('holdingValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");
            var validator = require("../../utilities/validator")();

            if (validator.isNotPopulated(data.holdingType)) {
                state.holdingType = "Select 'Freehold' or 'Leasehold'";
                ga('send', 'event', "userError", "holdingTypeError", "notPopulated");
            }

            return buildState(state);
        };
      
        return {
            validate: validate
        };
    });

}());
