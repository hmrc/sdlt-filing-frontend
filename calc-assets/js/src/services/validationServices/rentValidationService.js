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
            } else {
                data.year1Rent = undefined;
            }

            if (rent.displayYearTwoRent) {
                validateRent(data, state, 'year2Rent');
            } else {
                data.year2Rent = undefined;
            }

            if (rent.displayYearThreeRent) {
                validateRent(data, state, 'year3Rent');
            } else {
                data.year3Rent = undefined;
            }

            if (rent.displayYearFourRent) {
                validateRent(data, state, 'year4Rent');
            } else {
                data.year4Rent = undefined;
            }

            if (rent.displayYearFiveRent) {
                validateRent(data, state, 'year5Rent');
            } else {
                data.year5Rent = undefined;
            }

            return buildState(state);
        };
      
        return {
            validate: validate
        };
    });

}());
