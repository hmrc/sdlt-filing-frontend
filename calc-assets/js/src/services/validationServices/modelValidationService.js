(function() {
    "use strict";

    var app = require("../module");

    app.service('modelValidationService', function() {

        var validate = function(data) {
            var rent = require("../../utilities/displayLeasedYearRentFields");
            var validator = require("../../utilities/validator")();
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
            
            // if Freehold then must have premium
            if(data.holdingType === 'Freehold') {
                result.isPurchasePriceValid = hasError('premium');
            }

            // if Residential & >= 01/04/2016 then additional property question(s) required
            if(data.propertyType === 'Residential' && data.effectiveDate >= new Date(2016, 3, 1)) {
                result.isTwoOrMorePropertiesValid = hasError('twoOrMoreProperties');
                if (data.twoOrMoreProperties === 'Yes') {
                    result.isReplaceMainResidenceValid = hasError('replaceMainResidence');
                }
            }

            // if Leasehold must have lease dates and appropriate number of rents
            if(data.holdingType === 'Leasehold') {
                result.isStartDateValid = hasError('startDate');
                result.isEndDateValid = hasError('endDate');
                result.isPremiumValid = hasError('premium');

                result.isYear1RentValid = hasError('year1Rent');
                if (rent.displayYearTwoRent) result.isYear2RentValid = hasError('year2Rent');
                if (rent.displayYearThreeRent) result.isYear3RentValid = hasError('year3Rent');
                if (rent.displayYearFourRent) result.isYear4RentValid = hasError('year4Rent');
                if (rent.displayYearFiveRent) result.isYear5RentValid = hasError('year5Rent');

                var anyRentAbove2000 = validator.checkForRentAbove2000([data.year1Rent, data.year2Rent, data.year3Rent, data.year4Rent, data.year5Rent]);
                if(data.propertyType === 'Non-residential' && data.premium < 150000 && anyRentAbove2000){
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
