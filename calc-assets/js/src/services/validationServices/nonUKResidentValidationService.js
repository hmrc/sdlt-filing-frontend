(function() {
    "use strict";

    var app = require("../module");

    app.service('nonUKResidentValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");
            var validator = require("../../utilities/validator")();

            if (validator.isNotPopulated(data.nonUKResident)) {
                state.nonUKResident = "Select 'Yes' or 'No'";
                ga('send', 'event', "userError", "nonUKResidentError", "notPopulated");
            }

            return buildState(state);
        };
      
        return {
            validate: validate
        };
    });

}());
