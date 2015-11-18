(function() {
    "use strict";

    var app = require("../module");

    app.service('relevantRentValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");

            // validate the relevantRent
            if ( (data.relevantRent) && (data.relevantRent.length > 0) ) {
                if (isNaN(data.relevantRent)) {
                    state.relevantRent = "You have entered an incorrect Relevant Rent, check your entry and correct it";
                } else {
                    var regex = /^(\d*\.\d{1,2}|\d+)$/;
                    if (!data.relevantRent.match(regex)) {
                        state.relevantRent = "You have entered an incorrect Relevant Rent, check your entry and correct it";
                    }
                }
            }
            else state.relevantRent = "You must complete this box. Enter your Relevant Rent";

            return buildState(state);
        };
      
        return {
            validate: validate
        };
    });

}());
