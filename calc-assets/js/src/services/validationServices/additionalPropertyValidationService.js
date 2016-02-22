(function() {
    "use strict";

    var app = require("../module");

    app.service('additionalPropertyValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");
            var validator = require("../../utilities/validator")();

            if (validator.isNotPopulated(data.twoOrMoreProperties)) {
                state.twoOrMoreProperties = "Provide an answer to continue. Select 'Yes' or 'No'";
            }

            if(data.twoOrMoreProperties === "Yes") {
                if (validator.isNotPopulated(data.replaceMainResidence)) {
                    state.replaceMainResidence = "Provide an answer to continue. Select 'Yes' or 'No'";
                }
            }

            return buildState(state);
        };
      
        return {
            validate: validate
        };
    });

}());
