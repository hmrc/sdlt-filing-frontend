(function() {
    "use strict";

    var app = require("../module");

    app.service('dateValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");
            var validator = require("../../utilities/validator")();

            if (validator.isNotPopulated(data.effectiveDate)) {
                state.effectiveDate = 'You must complete the effective date field';
            } else if (validator.isInvalidParsedDate(data.effectiveDate)) {
                state.effectiveDate = 'Enter the date in the correct format';
            } else if (validator.isLessThanDate(data.effectiveDate, new Date(2012, 2, 22))) {
                state.effectiveDate = "Date can't be earlier than 22/3/2012";
            }

            return buildState(state);
        };
      
        return {
            validate: validate
        };
    });

}());
