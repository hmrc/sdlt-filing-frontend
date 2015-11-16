(function() {
    "use strict";

    var app = require("../module");

    app.service('modelValidationService', function() {

        var validate = function(data) {
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
                result.isYear1RentValid = hasError('year1Rent');
                result.isYear2RentValid = hasError('year2Rent');
                result.isYear3RentValid = hasError('year3Rent');
                result.isYear4RentValid = hasError('year4Rent');
                result.isYear5RentValid = hasError('year5Rent');
                result.isRelevantRentValid = hasError('relevantRent');
                
            }
           

            return result;

        };
      
        return {
            validate: validate
        };
    });

}());
