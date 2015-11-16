(function() {
    "use strict";

    var app = require("../module");

    app.service('dateValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");

            // validate the effectiveDate
            if (data.effectiveDate === '' || data.effectiveDate === undefined) {
                state.effectiveDate = "You must complete this box. Enter your date";
            } else if (data.effectiveDate === 'bad date') {
                    state.effectiveDate = "You have entered an incorrect date, check your entry and correct it";
            }

            return buildState(state);
        };
      
        return {
            validate: validate
        };
    });

}());
