(function() {
    "use strict";

    var app = require("../module");

    app.service('rentValidationService', function() {

        var validate = function(data) {
            var state = {};
            var buildState = require("../../utilities/buildState");
            var regex = /^[0-9]+$/;
            var rent = require("../../utilities/displayLeasedYearRentFields");
            rent = rent();
            rent = rent.getFunctions(data);


            // validate the year1Rent
            if (rent.displayYearOneRent) {
                if ( (data.year1Rent) && (data.year1Rent.length > 0) ) {
                    if (isNaN(data.year1Rent)) {
                        state.year1Rent = "You have entered an incorrect rent, check your entry and correct it";
                    } else {                    
                        if (!data.year1Rent.match(regex)) {
                            state.year1Rent = "You have entered an incorrect rent, check your entry and correct it";
                        }
                    }
                }
                else state.year1Rent = "You must complete this box. Enter rent";
            }


            // validate the year2Rent
            if (rent.displayYearTwoRent) {
                if ( (data.year2Rent) && (data.year2Rent.length > 0) ) {
                    if (isNaN(data.year2Rent)) {
                        state.year2Rent = "You have entered an incorrect rent, check your entry and correct it";
                    } else {                    
                        if (!data.year2Rent.match(regex)) {
                            state.year2Rent = "You have entered an incorrect rent, check your entry and correct it";
                        }
                    }
                }
                else state.year2Rent = "You must complete this box. Enter rent";
            }   

            // validate the year3Rent
            if (rent.displayYearThreeRent) {
                if ( (data.year3Rent) && (data.year3Rent.length > 0) ) {
                    if (isNaN(data.year3Rent)) {
                        state.year3Rent = "You have entered an incorrect rent, check your entry and correct it";
                    } else {                    
                        if (!data.year3Rent.match(regex)) {
                            state.year3Rent = "You have entered an incorrect rent, check your entry and correct it";
                        }
                    }
                }
                else state.year3Rent = "You must complete this box. Enter rent";
            }

            // validate the year4Rent
            if (rent.displayYearFourRent) {
                if ( (rent.displayYearFourRent) && (data.year4Rent) && (data.year4Rent.length > 0) ) {
                    if (isNaN(data.year4Rent)) {
                        state.year4Rent = "You have entered an incorrect rent, check your entry and correct it";
                    } else {                    
                        if (!data.year4Rent.match(regex)) {
                            state.year4Rent = "You have entered an incorrect rent, check your entry and correct it";
                        }
                    }
                }
                else state.year4Rent = "You must complete this box. Enter rent";
            }

            // validate the year5Rent
            if (rent.displayYearFiveRent) {
                if ( (rent.displayYearFiveRent) && (data.year5Rent) && (data.year5Rent.length > 0) ) {
                    if (isNaN(data.year5Rent)) {
                        state.year5Rent = "You have entered an incorrect rent, check your entry and correct it";
                    } else {                    
                        if (!data.year5Rent.match(regex)) {
                            state.year5Rent = "You have entered an incorrect rent, check your entry and correct it";
                        }
                    }
                }
                else state.year5Rent = "You must complete this box. Enter rent";
            }

            return buildState(state);
        };
      
        return {
            validate: validate
        };
    });

}());
