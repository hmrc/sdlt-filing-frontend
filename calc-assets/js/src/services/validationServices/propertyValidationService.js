(function() {
    "use strict";

    var app = require("../module");

    app.service('propertyValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");
            var validator = require("../../utilities/validator")();

            if (validator.isNotPopulated(data.propertyType)) {
                state.propertyType = "Select 'Residential' or 'Non-residential'";
                ga('send', 'event', "userError", "propertyTypeError", "notPopulated");
            }

            return buildState(state);
        };
      
        return {
            validate: validate
        };
    });

}());
