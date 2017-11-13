(function() {
    "use strict";

    var app = require("../module");

    app.service('firstTimeBuyerValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");
            var validator = require("../../utilities/validator")();

            if (validator.isNotPopulated(data.firstTimeBuyer)) {
                state.firstTimeBuyer = "Provide an answer to continue. Select 'Yes' or 'No'";
            }

            return buildState(state);
        };

        return {
            validate: validate
        };
    });

}());
