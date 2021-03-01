(function() {
    "use strict";

    var app = require("../module");

    app.service('sharedOwnershipValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");
            var validator = require("../../utilities/validator")();

            if (validator.isNotPopulated(data.sharedOwnership)) {
                state.sharedOwnership = "Select 'Yes' or 'No'";
            }

            return buildState(state);
        };

        return {
            validate: validate
        };
    });
}());
