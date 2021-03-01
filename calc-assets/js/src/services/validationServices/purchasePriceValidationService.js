(function() {
    "use strict";

    var app = require("../module");

    app.service('purchasePriceValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");
            var validator = require("../../utilities/validator")();

            if (validator.isNotPopulated(data.premium)) {
                state.premium = "Enter your Purchase Price";
            } else if (validator.isInvalidFloat(data.premium)) {
                state.premium = "Enter the purchase price again - don't use any letters or characters including £";
            }

            return buildState(state);
        };
      
        return {
            validate: validate
        };
    });

}());
