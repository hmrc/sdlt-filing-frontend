(function() {
    "use strict";

    var app = require("../module");

    app.service('holdingValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");

            if (!data.holdingType) {
                state.holdingType = "You must answer this question. Select 'Freehold' or 'Leasehold'";
            }

            return buildState(state);
        };
      
        return {
            validate: validate
        };
    });

}());
