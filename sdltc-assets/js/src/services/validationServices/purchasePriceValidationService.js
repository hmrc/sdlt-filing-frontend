(function() {
    "use strict";

    var app = require("../module");

    app.service('purchasePriceValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");

            // validate the purchasePrice
            if ( (data.purchasePrice) && (data.purchasePrice.length > 0) ) {
                if (isNaN(data.purchasePrice)) {
                    state.purchasePrice = "You have entered an incorrect Purchase Price, check your entry and correct it";
                } else {
                    var regex = /^[0-9]+$/;
                    if (!data.purchasePrice.match(regex)) {
                        state.purchasePrice = "You have entered an incorrect Purchase Price, check your entry and correct it";
                    }
                }
            }
            else state.purchasePrice = "You must complete this box. Enter your Purchase Price";

            return buildState(state);
        };
      
        return {
            validate: validate
        };
    });

}());
