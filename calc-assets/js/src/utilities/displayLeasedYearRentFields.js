(function() {
    "use strict";

    // decides whether rent fields should be displayed
    module.exports = function(){
        
        var reqFieldsCompleted = function(data) {
            return data.holdingType !== undefined && data.holdingType === 'Leasehold' && data.leaseTerm !== undefined;
        };


        var getFunctions = function(data) {
            var displayYearOneRent = reqFieldsCompleted(data);

            var displayYearTwoRent = reqFieldsCompleted(data) && (data.leaseTerm.years > 1 || (data.leaseTerm.years == 1 && data.leaseTerm.days > 0));

            var displayYearThreeRent = reqFieldsCompleted(data) && (data.leaseTerm.years > 2 || (data.leaseTerm.years == 2 && data.leaseTerm.days > 0));

            var displayYearFourRent = reqFieldsCompleted(data) && (data.leaseTerm.years > 3 || (data.leaseTerm.years == 3 && data.leaseTerm.days > 0));

            var displayYearFiveRent = reqFieldsCompleted(data) && (data.leaseTerm.years > 4 || (data.leaseTerm.years == 4 && data.leaseTerm.days > 0));

            return {
                displayYearOneRent : displayYearOneRent,
                displayYearTwoRent : displayYearTwoRent,
                displayYearThreeRent : displayYearThreeRent,
                displayYearFourRent : displayYearFourRent,
                displayYearFiveRent : displayYearFiveRent
            };
        };

        var addFunctionsToScope = function(scope) {
            var functions = getFunctions(scope.data);

            scope.displayYearOneRent = functions.displayYearOneRent;
            scope.displayYearTwoRent = functions.displayYearTwoRent;
            scope.displayYearThreeRent = functions.displayYearThreeRent;
            scope.displayYearFourRent = functions.displayYearFourRent;
            scope.displayYearFiveRent = functions.displayYearFiveRent;
        };

        return {
            getFunctions : getFunctions,
            addFunctionsToScope : addFunctionsToScope
        };        

    };

}());
