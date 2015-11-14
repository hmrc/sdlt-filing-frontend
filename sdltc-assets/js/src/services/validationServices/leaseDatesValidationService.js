(function() {
    "use strict";

    var app = require("../module");

    app.service('leaseDatesValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");
            var regex = /^[0-9]+$/;

            // validate the startDate
            if ( (data.startDate) && (data.startDate.length > 0) ) {
                if (isNaN(data.startDate)) {
                    state.startDate = "You have entered an incorrect start date, check your entry and correct it";
                } else {                    
                    if (!data.startDate.match(regex)) {
                        state.startDate = "You have entered an incorrect start date, check your entry and correct it";
                    }
                }
            }
            else state.startDate = "You must complete this box. Enter your start date";

            // validate the endDate
            if ( (data.endDate) && (data.endDate.length > 0) ) {
                if (isNaN(data.endDate)) {
                    state.endDate = "You have entered an incorrect end date, check your entry and correct it";
                } else {
                    if (!data.endDate.match(regex)) {
                        state.endDate = "You have entered an incorrect end date, check your entry and correct it";
                    }
                }
            }
            else state.endDate = "You must complete this box. Enter your end date";

            return buildState(state);
        };
      
        return {
            validate: validate
        };
    });

}());
