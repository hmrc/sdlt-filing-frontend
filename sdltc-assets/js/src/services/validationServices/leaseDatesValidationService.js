(function() {
    "use strict";

    var app = require("../module");

    app.service('leaseDatesValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");

            // validate the startDate
            if (data.startDate === '' || data.startDate === undefined) {
                state.startDate = "You must complete the start date field";
            } else if (data.startDate === 'bad date') {
                state.startDate = "You have entered an incorrect start date, check your entry and correct it";
            }

            // validate the endDate
            if (data.endDate === '' || data.endDate === undefined) {
                state.endDate = "You must complete the end date field";
            } else if (data.endDate === 'bad date') {
                state.endDate = "You have entered an incorrect end date, check your entry and correct it";
            }

            return buildState(state);
        };
      
        return {
            validate: validate
        };
    });

}());
