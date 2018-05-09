(function() {
    "use strict";

    var app = require("../module");

    app.service('purchaserValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");
            var validator = require("../../utilities/validator")();

            if (validator.isNotPopulated(data.individual)) {
                state.individual = "Provide an answer to continue. Select 'Yes' or 'No'";
                ga('send', 'event', "userError", "individualError", "notPopulated");
            }

            return buildState(state);
        };
      
        return {
            validate: validate
        };
    });

}());
