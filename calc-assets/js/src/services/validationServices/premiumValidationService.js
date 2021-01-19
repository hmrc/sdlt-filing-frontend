(function() {
    "use strict";

    var app = require("../module");

    app.service('premiumValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");
            var validator = require("../../utilities/validator")();

            if (validator.isNotPopulated(data.premium)) {
                state.premium = "Enter your Premium";
                ga('send', 'event', "userError", "premiumError", "notPopulated");
            } else if (validator.isInvalidFloat(data.premium)) {
                state.premium = "Enter the premium again - don't use any letters or characters including £";
                ga('send', 'event', "userError", "premiumError", "invalid");
            }

            return buildState(state);
        };
      
        return {
            validate: validate
        };
    });

}());
