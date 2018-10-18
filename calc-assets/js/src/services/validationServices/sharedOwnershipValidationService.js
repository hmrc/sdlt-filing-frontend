(function() {
    "use strict";

    var app = require("../module");

    app.service('sharedOwnershipValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");
            var validator = require("../../utilities/validator")();

            if (validator.isNotPopulated(data.sharedOwnership)) {
                state.sharedOwnership = "Provide an answer to continue. Select 'Yes' or 'No'";
                ga('send', 'event', "userError", "sharedOwnershipError", "notPopulated");
            }

            return buildState(state);
        };

        return {
            validate: validate
        };
    });
}());
