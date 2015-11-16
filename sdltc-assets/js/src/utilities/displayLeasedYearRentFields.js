(function() {
    "use strict";

    // decides whether rent fields should be displayed
    module.exports = function(scope){
        
        var reqFieldsCompleted = function() {
            return scope.data.holdingType !== undefined && scope.data.holdingType === 'Leasehold' && scope.data.leaseTerm !== undefined;
        };

        scope.displayYearOneRent = reqFieldsCompleted();

        scope.displayYearTwoRent = reqFieldsCompleted() && (scope.data.leaseTerm.years > 1 || (scope.data.leaseTerm.years == 1 && scope.data.leaseTerm.days > 0));

        scope.displayYearThreeRent = reqFieldsCompleted() && (scope.data.leaseTerm.years > 2 || (scope.data.leaseTerm.years == 2 && scope.data.leaseTerm.days > 0));

        scope.displayYearFourRent = reqFieldsCompleted() && (scope.data.leaseTerm.years > 3 || (scope.data.leaseTerm.years == 3 && scope.data.leaseTerm.days > 0));

        scope.displayYearFiveRent = reqFieldsCompleted() && (scope.data.leaseTerm.years > 4 || (scope.data.leaseTerm.years == 4 && scope.data.leaseTerm.days > 0));

    };

}());
