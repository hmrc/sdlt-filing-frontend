(function() {
    "use strict";

    var app = require("../module");

    app.service('rentValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");
            var validator = require("../../utilities/validator")();
            var rent = require("../../utilities/displayLeasedYearRentFields");
            rent = rent().getFunctions(data);

            var validateRent = function(data, state, field) {
                if (validator.isNotPopulated(data[field])) {
                    state[field] = "Enter the annual rent for all the years";
                } else if (validator.isInvalidFloat(data[field])) {
                    state[field] = "Enter the rent again - don't use any letters or characters including £";
                }
            };

            if (rent.displayYearOneRent) {
                validateRent(data, state, 'year1Rent');
            }

            if (rent.displayYearTwoRent) {
                validateRent(data, state, 'year2Rent');
            }

            if (rent.displayYearThreeRent) {
                validateRent(data, state, 'year3Rent');
            }

            if (rent.displayYearFourRent) {
                validateRent(data, state, 'year4Rent');
            }

            if (rent.displayYearFiveRent) {
                validateRent(data, state, 'year5Rent');
            }

            return buildState(state);
        };
      
        return {
            validate: validate
        };
    });

}());
