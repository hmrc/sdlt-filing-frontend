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

            // if Residential & >= 01/04/2016 then individual question required
            if(data.propertyType === 'Residential' && data.effectiveDate >= new Date(2016, 3, 1)) {
                result.isIndividualValid = hasError('individual');
                // individual = Yes then additional property question(s) required
                if (data.individual === 'Yes') {
                    result.isTwoOrMorePropertiesValid = hasError('twoOrMoreProperties');
                     if (data.twoOrMoreProperties === 'Yes') {
                         result.isReplaceMainResidenceValid = hasError('replaceMainResidence');
                    }
                }
            }

            // if Residential, between 22/11/2017 and 30/11/2019 and individual and not
            // two or more properties then FTB information required
            if(data.propertyType === 'Residential' &&
               validator.effectiveDateWithinFTBRange(data.effectiveDate) &&
                (validator.effectiveDateIsAfterJuly2020(data.effectiveDate) ||
                validator.effectiveDateIsAfterMarch2021(data.effectiveDate)) &&
               data.individual === 'Yes' &&
               data.twoOrMoreProperties === 'No'
            ){
                result.isOwnedOtherPropertiesValid = hasError('ownedOtherProperties');
                if(data.ownedOtherProperties === 'No') {
                    result.isMainResidenceValid = hasError('mainResidence');
                }
            }

            // if Residential and effective date is after March 31st 2021
            // nonUKResident question required
            if(data.propertyType === 'Residential' &&
               validator.effectiveDateIsAfterMarch2021(data.effectiveDate)
            ){
                result.isNonUKResidentValid = hasError('nonUKResident');
            }

            // if Residential, between 22/11/2017 and 30/11/2019 and individual and not
            // two or more properties then FTB information required
            if( data.holdingType === 'Leasehold' &&
                data.propertyType === 'Residential' &&
                validator.effectiveDateWithinFTBRange(data.effectiveDate) &&
                data.individual === 'Yes' &&
                data.twoOrMoreProperties === 'No' &&
                data.ownedOtherProperties === 'No' &&
                data.mainResidence === 'Yes'
            ){
                result.isSharedOwnershipValid = hasError('sharedOwnership');
                if(data.sharedOwnership === 'Yes'){
                    result.isCurrentValueValid = hasError('currentValue');
                    if(data.currentValue === 'Yes'){
                        result.isMarketValueValid = hasError('paySDLT');
                        if(data.paySDLT === ('Using market value election')|| data.paySDLT === 'Stages') {
                            result.isPremiumValid = hasError('premium');
                        }
                    }
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

                var allRentsBelow2000 = validator.checkAllRentsBelow2000(data);
                if(data.propertyType === 'Non-residential' && data.premium < 150000 && allRentsBelow2000){
                    if (data.effectiveDate > new Date('March 16, 2016')) {
                        result.isContractPre201603Valid = hasError('contractPre201603');
                        if (data.contractPre201603 === 'Yes') {
                            result.isContractVariedPost201603Valid = hasError('contractVariedPost201603');
                        }
                        if (data.contractPre201603 === 'Yes' && data.contractVariedPost201603 === 'No') {
                            result.isRelevantRentValid = hasError('relevantRent');
                        }
                    } else {
                        result.isRelevantRentValid = hasError('relevantRent');
                    }
               }
            }
            return result;
        };
      
        return {
            validate: validate
        };
    });

}());
