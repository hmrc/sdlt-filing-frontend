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
                    state[field] = "You must complete this box. Enter rent";
                } else if (validator.isInvalidFloat(data[field])) {
                    state[field] = "You have entered an incorrect rent, check your entry and correct it";
                }
            };

            // validate the year1Rent
            if (rent.displayYearOneRent) {
                validateRent(data, state, 'year1Rent');
            }

            // validate the year2Rent
            if (rent.displayYearTwoRent) {
                validateRent(data, state, 'year2Rent');
            }

            // validate the year3Rent
            if (rent.displayYearThreeRent) {
                validateRent(data, state, 'year3Rent');
            }

            // validate the year4Rent
            if (rent.displayYearFourRent) {
                validateRent(data, state, 'year4Rent');
            }

            // validate the year5Rent
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
