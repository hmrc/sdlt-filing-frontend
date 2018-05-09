(function() {
    "use strict";

    var app = require("../module");

    app.service('purchasePriceValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");
            var validator = require("../../utilities/validator")();

            if (validator.isNotPopulated(data.premium)) {
                state.premium = "You must complete this box. Enter your Purchase Price";
                ga('send', 'event', "userError", "purchasePriceError", "notPopulated");
            } else if (validator.isInvalidFloat(data.premium)) {
                state.premium = "Enter the purchase price again - don't use any letters or characters including £";
                ga('send', 'event', "userError", "purchasePriceError", "invalid");
            }

            return buildState(state);
        };
      
        return {
            validate: validate
        };
    });

}());
