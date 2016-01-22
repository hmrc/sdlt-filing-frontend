(function() {
    "use strict";

    var app = require("../module");

    app.service('modelValidationService', function() {

        var validate = function(data) {
            var rent = require("../../utilities/displayLeasedYearRentFields");
            rent = rent();
            rent = rent.getFunctions(data);

            var result = {
                isModelValid : true
            };

            var hasError = function(value) {
                if (data[value]) {
                    return '';
                } else {
                    result.isModelValid = false;
                    return 'form-field--error';
                }
            };

            // mandatory pages
            result.isHoldingValid = hasError('holdingType');
            result.isPropertyValid = hasError('propertyType');
            result.isEffectiveDateValid = hasError('effectiveDate');
            
            // must have freehold
            if(data.holdingType === 'Freehold') {
                result.isPurchasePriceValid = hasError('premium');
            }

            // must have leasehold
            if(data.holdingType === 'Leasehold') {
                result.isStartDateValid = hasError('startDate');
                result.isEndDateValid = hasError('endDate');
                result.isPremiumValid = hasError('premium');

                if (rent.displayYearOneRent) result.isYear1RentValid = hasError('year1Rent');
                if (rent.displayYearTwoRent) result.isYear2RentValid = hasError('year2Rent');
                if (rent.displayYearThreeRent) result.isYear3RentValid = hasError('year3Rent');
                if (rent.displayYearFourRent) result.isYear4RentValid = hasError('year4Rent');
                if (rent.displayYearFiveRent) result.isYear5RentValid = hasError('year5Rent');

                if(data.propertyType === 'Non-residential' && data.premium < 150000 && (data.year1Rent < 2000 && data.year2Rent < 2000 && data.year3Rent < 2000 && data.year4Rent < 2000 && data.year5Rent < 2000)){
                    result.isRelevantRentValid = hasError('relevantRent');
                }
            }

            return result;

        };
      
        return {
            validate: validate
        };
    });

}());
