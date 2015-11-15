(function() {
    "use strict";

    var app = require("../module");

    app.service('purchasePriceValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");

            // validate the premium
            if ( (data.premium) && (data.premium.length > 0) ) {
                if (isNaN(data.premium)) {
                    state.premium = "You have entered an incorrect Purchase Price, check your entry and correct it";
                } else {
                    var regex = /^[0-9]+$/;
                    if (!data.premium.match(regex)) {
                        state.premium = "You have entered an incorrect Purchase Price, check your entry and correct it";
                    }
                }
            }
            else state.premium = "You must complete this box. Enter your Purchase Price";

            return buildState(state);
        };
      
        return {
            validate: validate
        };
    });

}());
