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
                state.startDate = 'You must complete the start date field';
            } else if (validator.isInvalidParsedDate(data.startDate)) {
                state.startDate = 'You have entered an incorrect start date, check your entry and correct it';
            } else {
                startDateValid = true;
            }

            if (validator.isNotPopulated(data.endDate)) {
                state.endDate = 'You must complete the end date field';
            } else if (validator.isInvalidParsedDate(data.endDate)) {
                state.endDate = 'You have entered an incorrect end date, check your entry and correct it';
            } else if (startDateValid && validator.isLessThanDate(data.endDate, data.startDate)) {
                state.endDate = 'The lease end date cannot be before the lease start date';
            } else if (data.effectiveDate && validator.isLessThanDate(data.endDate, data.effectiveDate)) {
                state.endDate = 'The lease end date cannot be before the effective date';
            }

            return buildState(state);
        };
      
        return {
            validate: validate
        };
    });

}());