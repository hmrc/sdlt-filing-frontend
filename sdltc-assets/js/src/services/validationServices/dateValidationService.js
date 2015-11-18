(function() {
    "use strict";

    var app = require("../module");

    app.service('dateValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");

            // validate the effectiveDate
            if (data.effectiveDate === '' || data.effectiveDate === undefined) {
                state.effectiveDate = 'You must complete the effective date field';
            } else if (data.effectiveDate === 'bad date') {
                state.effectiveDate = 'You have entered an incorrect date, check your entry and correct it';
            } else if (data.effectiveDate < new Date(2012, 2, 22)) {
                state.effectiveDate = 'The effective date cannot be before 22 Mar 2012';
            }

            return buildState(state);
        };
      
        return {
            validate: validate
        };
    });

}());
