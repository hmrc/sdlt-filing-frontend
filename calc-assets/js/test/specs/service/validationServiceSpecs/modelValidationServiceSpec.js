(function() {
    'use strict';

    describe('Model Validation Service', function () {

        var service;

        beforeEach(angular.mock.module("calc.services"));

        beforeEach(inject(function (_modelValidationService_) {
            service = _modelValidationService_;
        }));

        it('should return false when no data provided', function() {
            var result = service.validate({});

            expect(result.isModelValid).toEqual(false);

            // mandatory fields should error
            expect(result.isHoldingValid).toEqual('form-field--error');
            expect(result.isPropertyValid).toEqual('form-field--error');
            expect(result.isEffectiveDateValid).toEqual('form-field--error');
            
            // non-mandatory fields should be undefined
            expect(result.isTwoOrMorePropertiesValid).toEqual(undefined);
            expect(result.isMainResidenceValid).toEqual(undefined);
            expect(result.isSharedOwnershipValid).toEqual(undefined);
            expect(result.isCurrentValueValid).toEqual(undefined);
            expect(result.isReplaceMainResidenceValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual(undefined);
            expect(result.isStartDateValid).toEqual(undefined);
            expect(result.isEndDateValid).toEqual(undefined);
            expect(result.isPremiumValid).toEqual(undefined);
            expect(result.isYear1RentValid).toEqual(undefined);
            expect(result.isYear2RentValid).toEqual(undefined);
            expect(result.isYear3RentValid).toEqual(undefined);
            expect(result.isYear4RentValid).toEqual(undefined);
            expect(result.isYear5RentValid).toEqual(undefined);
            expect(result.isRelevantRentValid).toEqual(undefined);
                  
        });

        it('should return true for a valid Freehold Residential < 1/4/2016', function() {

            var data = {
                holdingType : "Freehold",
                propertyType: "Residential",
                effectiveDate : new Date(2015, 4, 1),
                premium : 100000
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(true);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isTwoOrMorePropertiesValid).toEqual(undefined);
            expect(result.isReplaceMainResidenceValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual('');
        });

        it('should return true for a valid Freehold Residential >= 1/4/2016 NOT 2nd property', function() {

            var data = {
                holdingType : "Freehold",
                propertyType: "Residential",
                effectiveDate : new Date(2016, 4, 1),
                individual : 'Yes',
                twoOrMoreProperties : "No",
                premium : 100000
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(true);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isIndividualValid).toEqual('');
            expect(result.isTwoOrMorePropertiesValid).toEqual('');
            expect(result.isReplaceMainResidenceValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual('');
        });

        it('should return true for a valid Freehold Residential >= 1/4/2016 AND 2nd property', function() {

            var data = {
                holdingType : "Freehold",
                propertyType: "Residential",
                effectiveDate : new Date(2016, 4, 1),
                individual : 'Yes',
                twoOrMoreProperties : "Yes",
                replaceMainResidence : "No",
                premium : 100000
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(true);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isIndividualValid).toEqual('');
            expect(result.isTwoOrMorePropertiesValid).toEqual('');
            expect(result.isReplaceMainResidenceValid).toEqual('');
            expect(result.isPurchasePriceValid).toEqual('');
        });

        it('should return false for a Freehold Residential >= 1/4/2016 BUT 2nd property not supplied', function() {

            var data = {
                holdingType : "Freehold",
                propertyType: "Residential",
                effectiveDate : new Date(2016, 4, 1)
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(false);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isIndividualValid).toEqual('form-field--error');
            expect(result.isTwoOrMorePropertiesValid).toEqual(undefined);
            expect(result.isReplaceMainResidenceValid).toEqual(undefined);
        });

        it('should return false for a Freehold Residential >= 1/4/2016 BUT 2nd property not supplied', function() {

            var data = {
                holdingType : "Freehold",
                propertyType: "Residential",
                effectiveDate : new Date(2016, 4, 1),
                individual : 'Yes'
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(false);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isIndividualValid).toEqual('');
            expect(result.isTwoOrMorePropertiesValid).toEqual('form-field--error');
            expect(result.isReplaceMainResidenceValid).toEqual(undefined);
        });

        it('should return false for a Freehold Residential >= 1/4/2016 AND 2nd property Yes BUT Replace Main Res not supplied', function() {

            var data = {
                holdingType : "Freehold",
                propertyType: "Residential",
                effectiveDate : new Date(2016, 4, 1),
                individual : 'Yes',
                twoOrMoreProperties : "Yes"
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(false);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isIndividualValid).toEqual('');
            expect(result.isTwoOrMorePropertiesValid).toEqual('');
            expect(result.isReplaceMainResidenceValid).toEqual('form-field--error');
        });

        it('should return true for a valid Freehold Non-Residential < 1/4/2016', function() {

            var data = {
                holdingType : "Freehold",
                propertyType: "Non-residential",
                effectiveDate : new Date(2015, 4, 1),
                premium : 100000
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(true);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isTwoOrMorePropertiesValid).toEqual(undefined);
            expect(result.isReplaceMainResidenceValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual('');
        });

        it('should return true for a valid Freehold Non-Residential >= 1/4/2016 (NB 2nd property n/a for non-Res)', function() {

            var data = {
                holdingType : "Freehold",
                propertyType: "Non-residential",
                effectiveDate : new Date(2016, 4, 1),
                premium : 100000
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(true);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isTwoOrMorePropertiesValid).toEqual(undefined);
            expect(result.isReplaceMainResidenceValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual('');
        });

        it('should return true for a valid Leasehold Residential < 1/4/2016', function() {

            var data = {
                holdingType : "Leasehold",
                propertyType: "Residential",
                effectiveDate : new Date(2015, 1, 1),

                startDate : new Date(2015, 1, 1),
                endDate : new Date(2019, 12, 31),
                premium : 200000,

                leaseTerm : {
                    years : 5
                },
                year1Rent : 2500,
                year2Rent : 2500,
                year3Rent : 2500,
                year4Rent : 2500,
                year5Rent : 2500

            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(true);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');

            // the following are n/a in this scenario
            expect(result.isTwoOrMorePropertiesValid).toEqual(undefined);
            expect(result.isReplaceMainResidenceValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual(undefined);
            expect(result.isRelevantRentValid).toEqual(undefined);

            expect(result.isStartDateValid).toEqual('');
            expect(result.isEndDateValid).toEqual('');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isYear2RentValid).toEqual('');
            expect(result.isYear3RentValid).toEqual('');
            expect(result.isYear4RentValid).toEqual('');
            expect(result.isYear5RentValid).toEqual('');
        });

        it('should return false for a Leasehold Residential >= 1/4/2016, individual not answered', function() {

            var data = {
                holdingType : "Leasehold",
                propertyType: "Residential",
                effectiveDate : new Date(2016, 4, 1),

                startDate : new Date(2015, 1, 1),
                endDate : new Date(2019, 12, 31),
                premium : 200000,

                leaseTerm : {
                    years : 5
                },
                year1Rent : 2500,
                year2Rent : 2500,
                year3Rent : 2500,
                year4Rent : 2500,
                year5Rent : 2500

            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(false);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isIndividualValid).toEqual('form-field--error');

            // the following are n/a in this scenario
            expect(result.isTwoOrMorePropertiesValid).toEqual(undefined);
            expect(result.isReplaceMainResidenceValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual(undefined);
            expect(result.isRelevantRentValid).toEqual(undefined);

            expect(result.isStartDateValid).toEqual('');
            expect(result.isEndDateValid).toEqual('');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isYear2RentValid).toEqual('');
            expect(result.isYear3RentValid).toEqual('');
            expect(result.isYear4RentValid).toEqual('');
            expect(result.isYear5RentValid).toEqual('');
        });

        it('should return true for a valid Leasehold Residential >= 1/4/2016 NOT 2nd property', function() {

            var data = {
                holdingType : "Leasehold",
                propertyType: "Residential",
                effectiveDate : new Date(2016, 4, 1),
                individual : 'Yes',
                twoOrMoreProperties : "No",

                startDate : new Date(2015, 1, 1),
                endDate : new Date(2019, 12, 31),
                premium : 200000,

                leaseTerm : {
                    years : 5
                },
                year1Rent : 2500,
                year2Rent : 2500,
                year3Rent : 2500,
                year4Rent : 2500,
                year5Rent : 2500

            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(true);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isIndividualValid).toEqual('');
            expect(result.isTwoOrMorePropertiesValid).toEqual('');

            // the following are n/a in this scenario
            expect(result.isReplaceMainResidenceValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual(undefined);
            expect(result.isRelevantRentValid).toEqual(undefined);

            expect(result.isStartDateValid).toEqual('');
            expect(result.isEndDateValid).toEqual('');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isYear2RentValid).toEqual('');
            expect(result.isYear3RentValid).toEqual('');
            expect(result.isYear4RentValid).toEqual('');
            expect(result.isYear5RentValid).toEqual('');
        });

        it('should return true for a valid Leasehold Residential >= 1/4/2016 AND 2nd property', function() {

            var data = {
                holdingType : "Leasehold",
                propertyType: "Residential",
                effectiveDate : new Date(2016, 4, 1),
                individual : 'Yes',
                twoOrMoreProperties : "Yes",
                replaceMainResidence : "No",

                startDate : new Date(2015, 1, 1),
                endDate : new Date(2019, 12, 31),
                premium : 200000,

                leaseTerm : {
                    years : 5
                },
                year1Rent : 2500,
                year2Rent : 2500,
                year3Rent : 2500,
                year4Rent : 2500,
                year5Rent : 2500

            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(true);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isIndividualValid).toEqual('');
            expect(result.isTwoOrMorePropertiesValid).toEqual('');
            expect(result.isReplaceMainResidenceValid).toEqual('');

            // the following are n/a in this scenario
            expect(result.isPurchasePriceValid).toEqual(undefined);
            expect(result.isRelevantRentValid).toEqual(undefined);

            expect(result.isStartDateValid).toEqual('');
            expect(result.isEndDateValid).toEqual('');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isYear2RentValid).toEqual('');
            expect(result.isYear3RentValid).toEqual('');
            expect(result.isYear4RentValid).toEqual('');
            expect(result.isYear5RentValid).toEqual('');
        });

        it('should return false for a Leasehold Residential >= 1/4/2016 BUT 2nd property not supplied', function() {

            var data = {
                holdingType : "Leasehold",
                propertyType: "Residential",
                effectiveDate : new Date(2016, 4, 1),
                individual : 'Yes',

                startDate : new Date(2015, 1, 1),
                endDate : new Date(2019, 12, 31),
                premium : 200000,

                leaseTerm : {
                    years : 5
                },
                year1Rent : 2500,
                year2Rent : 2500,
                year3Rent : 2500,
                year4Rent : 2500,
                year5Rent : 2500

            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(false);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isIndividualValid).toEqual('');
            expect(result.isTwoOrMorePropertiesValid).toEqual('form-field--error');

            // the following are n/a in this scenario
            expect(result.isReplaceMainResidenceValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual(undefined);
            expect(result.isRelevantRentValid).toEqual(undefined);

            expect(result.isStartDateValid).toEqual('');
            expect(result.isEndDateValid).toEqual('');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isYear2RentValid).toEqual('');
            expect(result.isYear3RentValid).toEqual('');
            expect(result.isYear4RentValid).toEqual('');
            expect(result.isYear5RentValid).toEqual('');
        });

        it('should return false for a Leasehold Residential when rent data not entered', function() {

            var data = {
                holdingType : "Leasehold",
                propertyType: "Residential",
                effectiveDate : new Date(2015, 4, 1),

                startDate : new Date(2015, 1, 1),
                endDate : new Date(2019, 12, 31),
                premium : 200000
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(false);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isStartDateValid).toEqual('');
            expect(result.isEndDateValid).toEqual('');
            expect(result.isPremiumValid).toEqual('');

            expect(result.isYear1RentValid).toEqual('form-field--error');

            // the following are n/a in this scenario
            expect(result.isTwoOrMorePropertiesValid).toEqual(undefined);
            expect(result.isReplaceMainResidenceValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual(undefined);
            expect(result.isRelevantRentValid).toEqual(undefined);
            expect(result.isYear2RentValid).toEqual(undefined);
            expect(result.isYear3RentValid).toEqual(undefined);
            expect(result.isYear4RentValid).toEqual(undefined);
            expect(result.isYear5RentValid).toEqual(undefined);
        });

        it('should return false for a Leasehold Residential >= 1/4/2016 AND 2nd property Yes BUT Replace Main Res not supplied', function() {

            var data = {
                holdingType : "Leasehold",
                propertyType: "Residential",
                effectiveDate : new Date(2016, 4, 1),
                individual : 'Yes',
                twoOrMoreProperties : "Yes",

                startDate : new Date(2015, 1, 1),
                endDate : new Date(2019, 12, 31),
                premium : 200000,

                leaseTerm : {
                    years : 5
                },
                year1Rent : 2500,
                year2Rent : 2500,
                year3Rent : 2500,
                year4Rent : 2500,
                year5Rent : 2500

            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(false);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isIndividualValid).toEqual('');
            expect(result.isTwoOrMorePropertiesValid).toEqual('');
            expect(result.isReplaceMainResidenceValid).toEqual('form-field--error');

            // the following are n/a in this scenario
            expect(result.isPurchasePriceValid).toEqual(undefined);
            expect(result.isRelevantRentValid).toEqual(undefined);

            expect(result.isStartDateValid).toEqual('');
            expect(result.isEndDateValid).toEqual('');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isYear2RentValid).toEqual('');
            expect(result.isYear3RentValid).toEqual('');
            expect(result.isYear4RentValid).toEqual('');
            expect(result.isYear5RentValid).toEqual('');
        });

        it('should return true for a Leasehold Residential >= 22/11/2017 FTB and not Shared Ownership', function() {

            var data = {
                holdingType : "Leasehold",
                propertyType: "Residential",
                effectiveDate : new Date(2017, 11, 22),
                individual : "Yes",
                twoOrMoreProperties : "No",
                ownedOtherProperties : "No",
                mainResidence : "Yes",
                sharedOwnership : "No",
                startDate : new Date(2015, 1, 1),
                endDate : new Date(2019, 12, 31),
                premium : 200000,

                leaseTerm : {
                    years : 5
                },
                year1Rent : 2500,
                year2Rent : 2500,
                year3Rent : 2500,
                year4Rent : 2500,
                year5Rent : 2500

            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(true);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isIndividualValid).toEqual('');
            expect(result.isTwoOrMorePropertiesValid).toEqual('');
            expect(result.isOwnedOtherPropertiesValid).toEqual('');
            expect(result.isMainResidenceValid).toEqual('');
            expect(result.isSharedOwnershipValid).toEqual('');


            // the following are n/a in this scenario
            expect(result.isPurchasePriceValid).toEqual(undefined);
            expect(result.isRelevantRentValid).toEqual(undefined);

            expect(result.isStartDateValid).toEqual('');
            expect(result.isEndDateValid).toEqual('');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isYear2RentValid).toEqual('');
            expect(result.isYear3RentValid).toEqual('');
            expect(result.isYear4RentValid).toEqual('');
            expect(result.isYear5RentValid).toEqual('');
        });

        it('should return true for a Leasehold Residential >= 01/07/2021 FTB and not Shared Ownership', function() {

            var data = {
                holdingType : "Leasehold",
                propertyType: "Residential",
                effectiveDate : new Date(2021, 7, 1),
                nonUKResident : "No",
                individual : "Yes",
                twoOrMoreProperties : "No",
                ownedOtherProperties : "No",
                mainResidence : "Yes",
                sharedOwnership : "No",
                startDate : new Date(2021, 7, 1),
                endDate : new Date(2120, 6 , 30),
                premium : 200000,

                leaseTerm : {
                    years : 5
                },
                year1Rent : 2500,
                year2Rent : 2500,
                year3Rent : 2500,
                year4Rent : 2500,
                year5Rent : 2500

            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(true);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isNonUKResidentValid).toEqual('');
            expect(result.isIndividualValid).toEqual('');
            expect(result.isTwoOrMorePropertiesValid).toEqual('');
            expect(result.isOwnedOtherPropertiesValid).toEqual('');
            expect(result.isMainResidenceValid).toEqual('');
            expect(result.isSharedOwnershipValid).toEqual('');
            expect(result.isStartDateValid).toEqual('');
            expect(result.isEndDateValid).toEqual('');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isYear2RentValid).toEqual('');
            expect(result.isYear3RentValid).toEqual('');
            expect(result.isYear4RentValid).toEqual('');
            expect(result.isYear5RentValid).toEqual('');

            // the following are n/a in this scenario
            expect(result.isPurchasePriceValid).toEqual(undefined);
            expect(result.isRelevantRentValid).toEqual(undefined);

        });

        it('should return true for a Leasehold Residential >= 22/11/2017 FTB and Shared Ownership is undefined', function() {

            var data = {
                holdingType : "Leasehold",
                propertyType: "Residential",
                effectiveDate : new Date(2017, 11, 22),
                individual : "Yes",
                twoOrMoreProperties : "No",
                ownedOtherProperties : "No",
                mainResidence : "Yes",
                startDate : new Date(2015, 1, 1),
                endDate : new Date(2019, 12, 31),
                premium : 200000,

                leaseTerm : {
                    years : 5
                },
                year1Rent : 2500,
                year2Rent : 2500,
                year3Rent : 2500,
                year4Rent : 2500,
                year5Rent : 2500

            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(false);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isIndividualValid).toEqual('');
            expect(result.isTwoOrMorePropertiesValid).toEqual('');
            expect(result.isOwnedOtherPropertiesValid).toEqual('');
            expect(result.isMainResidenceValid).toEqual('');
            expect(result.isSharedOwnershipValid).toEqual('form-field--error');

            // the following are n/a in this scenario
            expect(result.isRelevantRentValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual(undefined);

            expect(result.isStartDateValid).toEqual('');
            expect(result.isEndDateValid).toEqual('');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isYear2RentValid).toEqual('');
            expect(result.isYear3RentValid).toEqual('');
            expect(result.isYear4RentValid).toEqual('');
            expect(result.isYear5RentValid).toEqual('');
        });

        it('should return true for a Leasehold Residential >= 22/11/2017 FTB and Shared Ownership is Yes, Current Value is No', function() {

            var data = {
                holdingType : "Leasehold",
                propertyType: "Residential",
                effectiveDate : new Date(2017, 11, 22),
                individual : "Yes",
                twoOrMoreProperties : "No",
                ownedOtherProperties : "No",
                mainResidence : "Yes",
                sharedOwnership : 'Yes',
                currentValue: 'No',
                startDate : new Date(2015, 1, 1),
                endDate : new Date(2019, 12, 31),
                premium : 200000,

                leaseTerm : {
                    years : 5
                },
                year1Rent : 2500,
                year2Rent : 2500,
                year3Rent : 2500,
                year4Rent : 2500,
                year5Rent : 2500

            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(true);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isIndividualValid).toEqual('');
            expect(result.isTwoOrMorePropertiesValid).toEqual('');
            expect(result.isOwnedOtherPropertiesValid).toEqual('');
            expect(result.isMainResidenceValid).toEqual('');
            expect(result.isSharedOwnershipValid).toEqual('');
            expect(result.isCurrentValueValid).toEqual('');

            // the following are n/a in this scenario
            expect(result.isRelevantRentValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual(undefined);

            expect(result.isStartDateValid).toEqual('');
            expect(result.isEndDateValid).toEqual('');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isYear2RentValid).toEqual('');
            expect(result.isYear3RentValid).toEqual('');
            expect(result.isYear4RentValid).toEqual('');
            expect(result.isYear5RentValid).toEqual('');
        });

        it('should return true for a Leasehold Residential >= 22/11/2017 FTB and Shared Ownership is Yes, Current Value is undefined', function() {

            var data = {
                holdingType : "Leasehold",
                propertyType: "Residential",
                effectiveDate : new Date(2017, 11, 22),
                individual : "Yes",
                twoOrMoreProperties : "No",
                ownedOtherProperties : "No",
                mainResidence : "Yes",
                sharedOwnership : 'Yes',
                startDate : new Date(2015, 1, 1),
                endDate : new Date(2019, 12, 31),
                premium : 200000,

                leaseTerm : {
                    years : 5
                },
                year1Rent : 2500,
                year2Rent : 2500,
                year3Rent : 2500,
                year4Rent : 2500,
                year5Rent : 2500

            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(false);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isIndividualValid).toEqual('');
            expect(result.isTwoOrMorePropertiesValid).toEqual('');
            expect(result.isOwnedOtherPropertiesValid).toEqual('');
            expect(result.isMainResidenceValid).toEqual('');
            expect(result.isSharedOwnershipValid).toEqual('');
            expect(result.isCurrentValueValid).toEqual('form-field--error');

            // the following are n/a in this scenario
            expect(result.isRelevantRentValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual(undefined);

            expect(result.isStartDateValid).toEqual('');
            expect(result.isEndDateValid).toEqual('');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isYear2RentValid).toEqual('');
            expect(result.isYear3RentValid).toEqual('');
            expect(result.isYear4RentValid).toEqual('');
            expect(result.isYear5RentValid).toEqual('');
        });

        it('should return true for a Leasehold Residential >= 22/11/2017 FTB and Shared Ownership is Yes, Current Value is Yes, PaySDLT is Using market value election', function() {

            var data = {
                holdingType : "Leasehold",
                propertyType: "Residential",
                effectiveDate : new Date(2017, 11, 22),
                individual : "Yes",
                twoOrMoreProperties : "No",
                ownedOtherProperties : "No",
                mainResidence : "Yes",
                sharedOwnership : 'Yes',
                currentValue : 'Yes',
                paySDLT : 'Using market value election',
                startDate : new Date(2015, 1, 1),
                endDate : new Date(2019, 12, 31),
                premium : 200000,

                leaseTerm : {
                    years : 5
                },
                year1Rent : 2500,
                year2Rent : 2500,
                year3Rent : 2500,
                year4Rent : 2500,
                year5Rent : 2500

            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(true);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isIndividualValid).toEqual('');
            expect(result.isTwoOrMorePropertiesValid).toEqual('');
            expect(result.isOwnedOtherPropertiesValid).toEqual('');
            expect(result.isMainResidenceValid).toEqual('');
            expect(result.isSharedOwnershipValid).toEqual('');
            expect(result.isCurrentValueValid).toEqual('');
            expect(result.isMarketValueValid).toEqual('');

            // the following are n/a in this scenario
            expect(result.isRelevantRentValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual(undefined);

            expect(result.isStartDateValid).toEqual('');
            expect(result.isEndDateValid).toEqual('');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isYear2RentValid).toEqual('');
            expect(result.isYear3RentValid).toEqual('');
            expect(result.isYear4RentValid).toEqual('');
            expect(result.isYear5RentValid).toEqual('');
        });

        it('should return true for a Leasehold Residential >= 22/11/2017 FTB and Shared Ownership is Yes, Current Value is Yes, PaySDLT is Stages', function() {

            var data = {
                holdingType : "Leasehold",
                propertyType: "Residential",
                effectiveDate : new Date(2017, 11, 22),
                individual : "Yes",
                twoOrMoreProperties : "No",
                ownedOtherProperties : "No",
                mainResidence : "Yes",
                sharedOwnership : 'Yes',
                currentValue : 'Yes',
                paySDLT : 'Stages',
                startDate : new Date(2015, 1, 1),
                endDate : new Date(2019, 12, 31),
                premium : 200000,

                leaseTerm : {
                    years : 5
                },
                year1Rent : 2500,
                year2Rent : 2500,
                year3Rent : 2500,
                year4Rent : 2500,
                year5Rent : 2500

            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(true);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isIndividualValid).toEqual('');
            expect(result.isTwoOrMorePropertiesValid).toEqual('');
            expect(result.isOwnedOtherPropertiesValid).toEqual('');
            expect(result.isMainResidenceValid).toEqual('');
            expect(result.isSharedOwnershipValid).toEqual('');
            expect(result.isCurrentValueValid).toEqual('');
            expect(result.isMarketValueValid).toEqual('');

            // the following are n/a in this scenario
            expect(result.isRelevantRentValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual(undefined);

            expect(result.isStartDateValid).toEqual('');
            expect(result.isEndDateValid).toEqual('');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isYear2RentValid).toEqual('');
            expect(result.isYear3RentValid).toEqual('');
            expect(result.isYear4RentValid).toEqual('');
            expect(result.isYear5RentValid).toEqual('');
        });

        it('should return true for a Leasehold Residential >= 22/11/2017 FTB and Shared Ownership is Yes, Current Value is Yes, PaySDLT is Using market value election, Premium is undefined', function() {

            var data = {
                holdingType : "Leasehold",
                propertyType: "Residential",
                effectiveDate : new Date(2017, 11, 22),
                individual : "Yes",
                twoOrMoreProperties : "No",
                ownedOtherProperties : "No",
                mainResidence : "Yes",
                sharedOwnership : 'Yes',
                currentValue : 'Yes',
                paySDLT : 'Using market value election',
                startDate : new Date(2015, 1, 1),
                endDate : new Date(2019, 12, 31),

                leaseTerm : {
                    years : 5
                },
                year1Rent : 2500,
                year2Rent : 2500,
                year3Rent : 2500,
                year4Rent : 2500,
                year5Rent : 2500

            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(false);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isIndividualValid).toEqual('');
            expect(result.isTwoOrMorePropertiesValid).toEqual('');
            expect(result.isOwnedOtherPropertiesValid).toEqual('');
            expect(result.isMainResidenceValid).toEqual('');
            expect(result.isSharedOwnershipValid).toEqual('');
            expect(result.isCurrentValueValid).toEqual('');
            expect(result.isMarketValueValid).toEqual('');

            // the following are n/a in this scenario
            expect(result.isRelevantRentValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual(undefined);

            expect(result.isStartDateValid).toEqual('');
            expect(result.isEndDateValid).toEqual('');
            expect(result.isPremiumValid).toEqual('form-field--error');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isYear2RentValid).toEqual('');
            expect(result.isYear3RentValid).toEqual('');
            expect(result.isYear4RentValid).toEqual('');
            expect(result.isYear5RentValid).toEqual('');
        });

        it('should return true for a Leasehold Residential >= 22/11/2017 FTB and Shared Ownership is Yes, Current Value is Yes, PaySDLT is Stages, Premium is undefined', function() {

            var data = {
                holdingType : "Leasehold",
                propertyType: "Residential",
                effectiveDate : new Date(2017, 11, 22),
                individual : "Yes",
                twoOrMoreProperties : "No",
                ownedOtherProperties : "No",
                mainResidence : "Yes",
                sharedOwnership : 'Yes',
                currentValue : 'Yes',
                paySDLT : 'Stages',
                startDate : new Date(2015, 1, 1),
                endDate : new Date(2019, 12, 31),

                leaseTerm : {
                    years : 5
                },
                year1Rent : 2500,
                year2Rent : 2500,
                year3Rent : 2500,
                year4Rent : 2500,
                year5Rent : 2500

            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(false);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isIndividualValid).toEqual('');
            expect(result.isTwoOrMorePropertiesValid).toEqual('');
            expect(result.isOwnedOtherPropertiesValid).toEqual('');
            expect(result.isMainResidenceValid).toEqual('');
            expect(result.isSharedOwnershipValid).toEqual('');
            expect(result.isCurrentValueValid).toEqual('');
            expect(result.isMarketValueValid).toEqual('');

            // the following are n/a in this scenario
            expect(result.isRelevantRentValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual(undefined);

            expect(result.isStartDateValid).toEqual('');
            expect(result.isEndDateValid).toEqual('');
            expect(result.isPremiumValid).toEqual('form-field--error');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isYear2RentValid).toEqual('');
            expect(result.isYear3RentValid).toEqual('');
            expect(result.isYear4RentValid).toEqual('');
            expect(result.isYear5RentValid).toEqual('');
        });

        it('should return true for a Leasehold Residential >= 22/11/2017 FTB and Shared Ownership is Yes, Current Value is Yes, PaySDLT is undefined', function() {

            var data = {
                holdingType : "Leasehold",
                propertyType: "Residential",
                effectiveDate : new Date(2017, 11, 22),
                individual : "Yes",
                twoOrMoreProperties : "No",
                ownedOtherProperties : "No",
                mainResidence : "Yes",
                sharedOwnership : 'Yes',
                currentValue : 'Yes',
                startDate : new Date(2015, 1, 1),
                endDate : new Date(2019, 12, 31),
                premium : 200000,

                leaseTerm : {
                    years : 5
                },
                year1Rent : 2500,
                year2Rent : 2500,
                year3Rent : 2500,
                year4Rent : 2500,
                year5Rent : 2500

            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(false);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isIndividualValid).toEqual('');
            expect(result.isTwoOrMorePropertiesValid).toEqual('');
            expect(result.isOwnedOtherPropertiesValid).toEqual('');
            expect(result.isMainResidenceValid).toEqual('');
            expect(result.isSharedOwnershipValid).toEqual('');
            expect(result.isCurrentValueValid).toEqual('');
            expect(result.isMarketValueValid).toEqual('form-field--error');

            // the following are n/a in this scenario
            expect(result.isRelevantRentValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual(undefined);

            expect(result.isStartDateValid).toEqual('');
            expect(result.isEndDateValid).toEqual('');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isYear2RentValid).toEqual('');
            expect(result.isYear3RentValid).toEqual('');
            expect(result.isYear4RentValid).toEqual('');
            expect(result.isYear5RentValid).toEqual('');
        });

        it('should return true for a valid Leasehold Non-residential with 1 year rent without relevant rent', function() {

            var data = {
                holdingType : "Leasehold",
                propertyType: "Non-residential",
                effectiveDate : new Date(2015, 4, 1),
                startDate : new Date(2015, 1, 1),
                endDate : new Date(2015, 12, 31),
                premium : 150000,
                leaseTerm : {
                    years : 1
                },
                year1Rent : 2500

            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(true);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isStartDateValid).toEqual('');
            expect(result.isEndDateValid).toEqual('');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');

            expect(result.isTwoOrMorePropertiesValid).toEqual(undefined);
            expect(result.isReplaceMainResidenceValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual(undefined);
            expect(result.isYear2RentValid).toEqual(undefined);
            expect(result.isYear3RentValid).toEqual(undefined);
            expect(result.isYear4RentValid).toEqual(undefined);
            expect(result.isYear5RentValid).toEqual(undefined);
            expect(result.isRelevantRentValid).toEqual(undefined);
        });

        it('should return true for a valid Leasehold Non-residential with 2 years rent without relevant rent', function() {

            var data = {
                holdingType : "Leasehold",
                propertyType: "Non-residential",
                effectiveDate : new Date(2015, 4, 1),
                startDate : new Date(2015, 1, 1),
                endDate : new Date(2016, 12, 31),
                premium : 150000,
                leaseTerm : {
                    years : 2
                },
                year1Rent : 2500,
                year2Rent : 2500
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(true);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isStartDateValid).toEqual('');
            expect(result.isEndDateValid).toEqual('');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isYear2RentValid).toEqual('');

            expect(result.isTwoOrMorePropertiesValid).toEqual(undefined);
            expect(result.isReplaceMainResidenceValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual(undefined);
            expect(result.isYear3RentValid).toEqual(undefined);
            expect(result.isYear4RentValid).toEqual(undefined);
            expect(result.isYear5RentValid).toEqual(undefined);
            expect(result.isRelevantRentValid).toEqual(undefined);
        });

        it('should return true for a valid Leasehold Non-residential with 3 years rent without relevant rent', function() {

            var data = {
                holdingType : "Leasehold",
                propertyType: "Non-residential",
                effectiveDate : new Date(2015, 4, 1),
                startDate : new Date(2015, 1, 1),
                endDate : new Date(2017, 12, 31),
                premium : 150000,
                leaseTerm : {
                    years : 3
                },
                year1Rent : 2500,
                year2Rent : 2500,
                year3Rent : 2500
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(true);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isStartDateValid).toEqual('');
            expect(result.isEndDateValid).toEqual('');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isYear2RentValid).toEqual('');
            expect(result.isYear3RentValid).toEqual('');

            expect(result.isTwoOrMorePropertiesValid).toEqual(undefined);
            expect(result.isReplaceMainResidenceValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual(undefined);
            expect(result.isYear4RentValid).toEqual(undefined);
            expect(result.isYear5RentValid).toEqual(undefined);
            expect(result.isRelevantRentValid).toEqual(undefined);
        });

        it('should return true for a valid Leasehold Non-residential with 4 years rent without relevant rent', function() {

            var data = {
                holdingType : "Leasehold",
                propertyType: "Non-residential",
                effectiveDate : new Date(2015, 4, 1),
                startDate : new Date(2015, 1, 1),
                endDate : new Date(2018, 12, 31),
                premium : 150000,
                leaseTerm : {
                    years : 4
                },
                year1Rent : 2500,
                year2Rent : 2500,
                year3Rent : 2500,
                year4Rent : 2500
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(true);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isStartDateValid).toEqual('');
            expect(result.isEndDateValid).toEqual('');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isYear2RentValid).toEqual('');
            expect(result.isYear3RentValid).toEqual('');
            expect(result.isYear4RentValid).toEqual('');

            expect(result.isTwoOrMorePropertiesValid).toEqual(undefined);
            expect(result.isReplaceMainResidenceValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual(undefined);
            expect(result.isYear5RentValid).toEqual(undefined);
            expect(result.isRelevantRentValid).toEqual(undefined);
        });

        it('should return true for a valid Leasehold Non-residential with 5 years rent without relevant rent', function() {

            var data = {
                holdingType : "Leasehold",
                propertyType: "Non-residential",
                effectiveDate : new Date(2015, 4, 1),
                startDate : new Date(2015, 1, 1),
                endDate : new Date(2019, 12, 31),
                premium : 150000,
                leaseTerm : {
                    years : 5
                },
                year1Rent : 2500,
                year2Rent : 2500,
                year3Rent : 2500,
                year4Rent : 2500,
                year5Rent : 2500
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(true);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isStartDateValid).toEqual('');
            expect(result.isEndDateValid).toEqual('');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isYear2RentValid).toEqual('');
            expect(result.isYear3RentValid).toEqual('');
            expect(result.isYear4RentValid).toEqual('');
            expect(result.isYear5RentValid).toEqual('');

            expect(result.isTwoOrMorePropertiesValid).toEqual(undefined);
            expect(result.isReplaceMainResidenceValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual(undefined);
            expect(result.isRelevantRentValid).toEqual(undefined);
        });

        it('should return true for a valid Leasehold Non-residential with 5 years rent WITH relevant rent', function() {

            var data = {
                holdingType : "Leasehold",
                propertyType: "Non-residential",
                effectiveDate : new Date(2015, 4, 1),
                startDate : new Date(2015, 1, 1),
                endDate : new Date(2019, 12, 31),
                premium : 149999,
                leaseTerm : {
                    years : 5
                },
                year1Rent : 1999,
                year2Rent : 1999,
                year3Rent : 1999,
                year4Rent : 1999,
                year5Rent : 1999,
                relevantRent : 1999
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(true);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isStartDateValid).toEqual('');
            expect(result.isEndDateValid).toEqual('');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isYear2RentValid).toEqual('');
            expect(result.isYear3RentValid).toEqual('');
            expect(result.isYear4RentValid).toEqual('');
            expect(result.isYear5RentValid).toEqual('');
            expect(result.isRelevantRentValid).toEqual('');

            expect(result.isTwoOrMorePropertiesValid).toEqual(undefined);
            expect(result.isReplaceMainResidenceValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual(undefined);
        });

        it('should return false for a Leasehold Non-residential with 5 years rent when relevant rent expected but missing', function() {

            var data = {
                holdingType : "Leasehold",
                propertyType: "Non-residential",
                effectiveDate : new Date(2015, 4, 1),
                startDate : new Date(2015, 1, 1),
                endDate : new Date(2019, 12, 31),
                premium : 149999,
                leaseTerm : {
                    years : 5
                },
                year1Rent : 1999,
                year2Rent : 1999,
                year3Rent : 1999,
                year4Rent : 1999,
                year5Rent : 1999
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(false);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isStartDateValid).toEqual('');
            expect(result.isEndDateValid).toEqual('');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isYear2RentValid).toEqual('');
            expect(result.isYear3RentValid).toEqual('');
            expect(result.isYear4RentValid).toEqual('');
            expect(result.isYear5RentValid).toEqual('');
            expect(result.isRelevantRentValid).toEqual('form-field--error');

            expect(result.isTwoOrMorePropertiesValid).toEqual(undefined);
            expect(result.isReplaceMainResidenceValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual(undefined);
        });

        it('should return false for a Leasehold Non-residential, ContractPre201603 & contractVariedPost201603 not present', function() {

            var data = {
                holdingType : "Leasehold",
                propertyType: "Non-residential",
                effectiveDate : new Date(2016, 2, 17),
                startDate : new Date(2015, 1, 1),
                endDate : new Date(2019, 12, 31),
                premium : 149999,
                leaseTerm : {
                    years : 5
                },
                year1Rent : 1999,
                year2Rent : 1999,
                year3Rent : 1999,
                year4Rent : 1999,
                year5Rent : 1999
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(false);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isStartDateValid).toEqual('');
            expect(result.isEndDateValid).toEqual('');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isYear2RentValid).toEqual('');
            expect(result.isYear3RentValid).toEqual('');
            expect(result.isYear4RentValid).toEqual('');
            expect(result.isYear5RentValid).toEqual('');

            expect(result.isContractPre201603Valid).toEqual('form-field--error');
            expect(result.isRelevantRentValid).toEqual(undefined);

            expect(result.isTwoOrMorePropertiesValid).toEqual(undefined);
            expect(result.isReplaceMainResidenceValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual(undefined);
        });

        it('should return false for a Leasehold Non-residential, ContractPre201603 = Yes, contractVariedPost201603 not present', function() {

            var data = {
                holdingType : "Leasehold",
                propertyType: "Non-residential",
                effectiveDate : new Date(2016, 2, 17),
                startDate : new Date(2015, 1, 1),
                endDate : new Date(2019, 12, 31),
                premium : 149999,
                leaseTerm : {
                    years : 5
                },
                year1Rent : 1999,
                year2Rent : 1999,
                year3Rent : 1999,
                year4Rent : 1999,
                year5Rent : 1999,
                contractPre201603 : 'Yes'
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(false);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isStartDateValid).toEqual('');
            expect(result.isEndDateValid).toEqual('');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isYear2RentValid).toEqual('');
            expect(result.isYear3RentValid).toEqual('');
            expect(result.isYear4RentValid).toEqual('');
            expect(result.isYear5RentValid).toEqual('');

            expect(result.isContractPre201603Valid).toEqual('');
            expect(result.isContractVariedPost201603Valid).toEqual('form-field--error');
            expect(result.isRelevantRentValid).toEqual(undefined);

            expect(result.isTwoOrMorePropertiesValid).toEqual(undefined);
            expect(result.isReplaceMainResidenceValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual(undefined);
        });

        it('should return true for a Leasehold Non-residential, ContractPre201603 = No & contractVariedPost201603 not present', function() {

            var data = {
                holdingType : "Leasehold",
                propertyType: "Non-residential",
                effectiveDate : new Date(2016, 2, 17),
                startDate : new Date(2015, 1, 1),
                endDate : new Date(2019, 12, 31),
                premium : 149999,
                leaseTerm : {
                    years : 5
                },
                year1Rent : 1999,
                year2Rent : 1999,
                year3Rent : 1999,
                year4Rent : 1999,
                year5Rent : 1999,
                contractPre201603 : 'No'
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(true);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isStartDateValid).toEqual('');
            expect(result.isEndDateValid).toEqual('');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isYear2RentValid).toEqual('');
            expect(result.isYear3RentValid).toEqual('');
            expect(result.isYear4RentValid).toEqual('');
            expect(result.isYear5RentValid).toEqual('');

            expect(result.isContractPre201603Valid).toEqual('');
            expect(result.isContractVariedPost201603Valid).toEqual(undefined);
            expect(result.isRelevantRentValid).toEqual(undefined);

            expect(result.isTwoOrMorePropertiesValid).toEqual(undefined);
            expect(result.isReplaceMainResidenceValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual(undefined);
        });

        it('should return true for a Leasehold Non-residential, ContractPre201603 = Yes, contractVariedPost201603 = No', function() {

            var data = {
                holdingType : "Leasehold",
                propertyType: "Non-residential",
                effectiveDate : new Date(2016, 2, 17),
                startDate : new Date(2015, 1, 1),
                endDate : new Date(2019, 12, 31),
                premium : 149999,
                leaseTerm : {
                    years : 5
                },
                year1Rent : 1999,
                year2Rent : 1999,
                year3Rent : 1999,
                year4Rent : 1999,
                year5Rent : 1999,
                contractPre201603 : 'Yes',
                contractVariedPost201603 : 'No',
                relevantRent : 999
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(true);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isStartDateValid).toEqual('');
            expect(result.isEndDateValid).toEqual('');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isYear2RentValid).toEqual('');
            expect(result.isYear3RentValid).toEqual('');
            expect(result.isYear4RentValid).toEqual('');
            expect(result.isYear5RentValid).toEqual('');

            expect(result.isContractPre201603Valid).toEqual('');
            expect(result.isContractVariedPost201603Valid).toEqual('');
            expect(result.isRelevantRentValid).toEqual('');

            expect(result.isTwoOrMorePropertiesValid).toEqual(undefined);
            expect(result.isReplaceMainResidenceValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual(undefined);
        });

        it('should return false for a Leasehold Non-residential, ContractPre201603 = Yes, contractVariedPost201603 = No but NO relevant rent present', function() {

            var data = {
                holdingType : "Leasehold",
                propertyType: "Non-residential",
                effectiveDate : new Date(2016, 2, 17),
                startDate : new Date(2015, 1, 1),
                endDate : new Date(2019, 12, 31),
                premium : 149999,
                leaseTerm : {
                    years : 5
                },
                year1Rent : 1999,
                year2Rent : 1999,
                year3Rent : 1999,
                year4Rent : 1999,
                year5Rent : 1999,
                contractPre201603 : 'Yes',
                contractVariedPost201603 : 'No'
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(false);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isStartDateValid).toEqual('');
            expect(result.isEndDateValid).toEqual('');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isYear2RentValid).toEqual('');
            expect(result.isYear3RentValid).toEqual('');
            expect(result.isYear4RentValid).toEqual('');
            expect(result.isYear5RentValid).toEqual('');

            expect(result.isContractPre201603Valid).toEqual('');
            expect(result.isContractVariedPost201603Valid).toEqual('');
            expect(result.isRelevantRentValid).toEqual('form-field--error');

            expect(result.isTwoOrMorePropertiesValid).toEqual(undefined);
            expect(result.isReplaceMainResidenceValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual(undefined);
        });

        it('should return true for a valid Freehold Residential 21/11/2017, single property', function() {

            var data = {
                holdingType : "Freehold",
                propertyType: "Residential",
                effectiveDate : new Date(2017, 10, 21),
                individual : 'Yes',
                twoOrMoreProperties : "No",
                premium : 100000
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(true);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isIndividualValid).toEqual('');
            expect(result.isTwoOrMorePropertiesValid).toEqual('');
            expect(result.isPurchasePriceValid).toEqual('');
        });

        it('should return true for a valid Freehold Residential 22/11/2017, first property', function() {

            var data = {
                holdingType : "Freehold",
                propertyType: "Residential",
                effectiveDate : new Date(2017, 10, 22),
                individual : 'Yes',
                twoOrMoreProperties : "No",
                ownedOtherProperties: "No",
                mainResidence: "Yes",
                premium : 100000
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(true);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isIndividualValid).toEqual('');
            expect(result.isTwoOrMorePropertiesValid).toEqual('');
            expect(result.isPurchasePriceValid).toEqual('');
            expect(result.isOwnedOtherPropertiesValid).toEqual('');
            expect(result.isMainResidenceValid).toEqual('');
        });

        it('should return true for a valid Freehold Residential 22/11/2017, not first property', function() {

            var data = {
                holdingType : "Freehold",
                propertyType: "Residential",
                effectiveDate : new Date(2017, 10, 22),
                individual : 'Yes',
                twoOrMoreProperties : "No",
                ownedOtherProperties: "Yes",
                premium : 100000
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(true);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isIndividualValid).toEqual('');
            expect(result.isTwoOrMorePropertiesValid).toEqual('');
            expect(result.isPurchasePriceValid).toEqual('');
            expect(result.isOwnedOtherPropertiesValid).toEqual('');
        });

        it('should return true for a valid Freehold Residential 30/11/2019, not-individual', function() {

            var data = {
                holdingType : "Freehold",
                propertyType: "Residential",
                effectiveDate : new Date(2019, 10, 30),
                individual : 'No',
                premium : 100000
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(true);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isIndividualValid).toEqual('');
            expect(result.isPurchasePriceValid).toEqual('');
        });

        it('should return true for a valid Freehold Residential 30/11/2019, two or more properties', function() {

            var data = {
                holdingType : "Freehold",
                propertyType: "Residential",
                effectiveDate : new Date(2019, 10, 30),
                individual : 'Yes',
                twoOrMoreProperties : "Yes",
                replaceMainResidence: "Yes",
                premium : 100000
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(true);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isIndividualValid).toEqual('');
            expect(result.isReplaceMainResidenceValid).toEqual('');
            expect(result.isPurchasePriceValid).toEqual('');
        });

        it('should return true for a valid Freehold Non-Residential 30/11/2019', function() {

            var data = {
                holdingType : "Freehold",
                propertyType: "Non-residential",
                effectiveDate : new Date(2019, 10, 30),
                premium : 100000
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(true);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isTwoOrMorePropertiesValid).toEqual(undefined);
            expect(result.isReplaceMainResidenceValid).toEqual(undefined);
            expect(result.isPurchasePriceValid).toEqual('');
        });

        it('should return false for a Freehold Residential 30/11/2019, first time buyer without details', function() {

            var data = {
                holdingType : "Freehold",
                propertyType: "Residential",
                effectiveDate : new Date(2019, 10, 30),
                individual : 'Yes',
                twoOrMoreProperties : "No",
                premium : 100000
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(false);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isIndividualValid).toEqual('');
            expect(result.isTwoOrMorePropertiesValid).toEqual('');
            expect(result.isPurchasePriceValid).toEqual('');
            expect(result.isOwnedOtherPropertiesValid).toEqual('form-field--error');
            expect(result.isMainResidenceValid).toEqual(undefined);
        });

        it('should return false for a Freehold Residential 30/11/2019, first time buyer without main residence details', function() {

            var data = {
                holdingType : "Freehold",
                propertyType: "Residential",
                effectiveDate : new Date(2019, 10, 30),
                individual : 'Yes',
                twoOrMoreProperties : "No",
                ownedOtherProperties: "No",
                premium : 100000
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(false);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isIndividualValid).toEqual('');
            expect(result.isTwoOrMorePropertiesValid).toEqual('');
            expect(result.isPurchasePriceValid).toEqual('');
            expect(result.isOwnedOtherPropertiesValid).toEqual('');
            expect(result.isMainResidenceValid).toEqual('form-field--error');
        });

        it('should return false for a Freehold Residential 01/07/2021, first time buyer without main residence details', function() {

            var data = {
                holdingType : "Freehold",
                propertyType: "Residential",
                effectiveDate : new Date(2021, 7, 1),
                individual : 'Yes',
                twoOrMoreProperties : "No",
                ownedOtherProperties: "No",
                premium : 100000
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(false);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('');
            expect(result.isIndividualValid).toEqual('');
            expect(result.isTwoOrMorePropertiesValid).toEqual('');
            expect(result.isPurchasePriceValid).toEqual('');
            expect(result.isOwnedOtherPropertiesValid).toEqual('');
            expect(result.isMainResidenceValid).toEqual('form-field--error');
        });

    });

}());
