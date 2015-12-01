(function() {
    "use strict";

    var app = require("../module");

    app.service('premiumValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");
            var validator = require("../../utilities/validator")();

            // validate the premium
            if ( (data.premium) && (data.premium.length > 0) ) {
                if (isNaN(data.premium)) {
                    state.premium = "You have entered an incorrect Premium, check your entry and correct it";
                } else {
                    var regex = /^(\d*\.\d{1,2}|\d+)$/;
                    if (!data.premium.match(regex)) {
                        state.premium = "You have entered an incorrect Premium, check your entry and correct it";
                    }
                }
            }
            else state.premium = "You must complete this box. Enter your Premium";

            return buildState(state);
        };
      
        return {
            validate: validate
        };
    });

}());
