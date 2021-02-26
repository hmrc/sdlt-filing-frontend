(function() {
    "use strict";

    var app = require("../module");

    app.service('mainResidenceValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");
            var validator = require("../../utilities/validator")();

            if (validator.isNotPopulated(data.mainResidence)) {
                state.mainResidence = "Select 'Yes' or 'No'";
            }

            return buildState(state);
        };

        return {
            validate: validate
        };
    });

}());
