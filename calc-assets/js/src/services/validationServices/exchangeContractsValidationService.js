(function() {
    "use strict";

    var app = require("../module");

    app.service('exchangeContractsValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");
            var validator = require("../../utilities/validator")();

            if (validator.isNotPopulated(data.contractPre201603)) {
                state.contractPre201603 = "Select 'Yes' or 'No'";
                ga('send', 'event', "userError", "contractPre201603Error", "notPopulated");
            }

            if(data.contractPre201603 === "Yes") {
                if (validator.isNotPopulated(data.contractVariedPost201603)) {
                    state.contractVariedPost201603 = "Select 'Yes' or 'No'";
                    ga('send', 'event', "userError", "contractVariedPost201603Error", "notPopulated");
                }
            }

            return buildState(state);
        };
      
        return {
            validate: validate
        };
    });

}());
