(function() {
    "use strict";

    var app = require("../module");

    app.service('dateValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");

            // validate the effectiveDate
            if ( (data.effectiveDate) && (data.effectiveDate.length > 0) ) {
                if (isNaN(data.effectiveDate)) {
                    state.effectiveDate = "You have entered an incorrect date, check your entry and correct it";
                } else {
                    var regex = /^[0-9]+$/;
                    if (!data.effectiveDate.match(regex)) {
                        state.effectiveDate = "You have entered an incorrect date, check your entry and correct it";
                    }
                }
            }
            else state.effectiveDate = "You must complete this box. Enter your date";

            return buildState(state);
        };
      
        return {
            validate: validate
        };
    });

}());
