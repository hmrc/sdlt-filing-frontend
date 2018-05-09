(function() {
    "use strict";

    var app = require("../module");

    app.service('leaseDatesValidationService', function() {

        var validate = function(data) {
            var state = {},
                startDateValid = false,
                endDateValid = false;

            var buildState = require('../../utilities/buildState');
            var validator = require("../../utilities/validator")();

            if (validator.isNotPopulated(data.startDate)) {
                state.startDate = 'Enter a start date';
                ga('send', 'event', "userError", "startDateError", "notPopulated");
            } else if (validator.isInvalidParsedDate(data.startDate)) {
                state.startDate = 'Enter a valid date';
                ga('send', 'event', "userError", "startDateError", "invalid");
            } else {
                startDateValid = true;
            }

            if (validator.isNotPopulated(data.endDate)) {
                state.endDate = 'Enter an end date';
                ga('send', 'event', "userError", "endDateError", "notPopulated");
            } else if (validator.isInvalidParsedDate(data.endDate)) {
                state.endDate = 'Enter a valid date';
                ga('send', 'event', "userError", "endDateError", "invalid");
            } else if (startDateValid && validator.isLessThanDate(data.endDate, data.startDate)) {
                state.endDate = "End date can't be before the start date";
                ga('send', 'event', "userError", "endDateError", "outOfRange");
            } else if (data.effectiveDate && validator.isLessThanDate(data.endDate, data.effectiveDate)) {
                state.endDate = "End date can't be before the effective date";
                ga('send', 'event', "userError", "endDateError", "outOfRange");
            }

            return buildState(state);
        };
      
        return {
            validate: validate
        };
    });

}());