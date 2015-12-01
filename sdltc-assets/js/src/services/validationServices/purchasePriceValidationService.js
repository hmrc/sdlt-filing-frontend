(function() {
    "use strict";

    var app = require("../module");

    app.service('purchasePriceValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");
            var validator = require("../../utilities/validator")();

            // validate the premium
            if (validator.isNotPopulated(data.premium)) {
                state.premium = "You must complete this box. Enter your Purchase Price";
            } else if (validator.isInvalidFloat(data.premium)) {
                state.premium = "You have entered an incorrect Purchase Price, check your entry and correct it";
            }

            return buildState(state);
        };
      
        return {
            validate: validate
        };
    });

}());
