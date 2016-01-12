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

            expect(result.isHoldingValid).toEqual('form-field--error');
            expect(result.isPropertyValid).toEqual('form-field--error');
            expect(result.isEffectiveDateValid).toEqual('form-field--error');
            
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

        it('should return an empty string if the fields are defined', function() {

            var data = {
                propertyType: "Non-residential",
                premium : 1000,

                holdingType : "Leasehold",
                leaseTerm : {
                    years : 5
                },
                year1Rent : "1",
                year2Rent : "2",
                year3Rent : "3",
                year4Rent : "4",
                year5Rent : "5",

                relevantRent : "Banana"
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(false);

            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('form-field--error');
            expect(result.isPurchasePriceValid).toEqual(undefined);    
            expect(result.isStartDateValid).toEqual('form-field--error');
            expect(result.isEndDateValid).toEqual('form-field--error');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isYear2RentValid).toEqual('');
            expect(result.isYear3RentValid).toEqual('');
            expect(result.isYear4RentValid).toEqual('');
            expect(result.isYear5RentValid).toEqual('');
            expect(result.isRelevantRentValid).toEqual('');
        });

        it('should not check relevant rent if property type is non-residential and premium is >15000', function() {

            var data = {
                propertyType: "Residential",
                premium : 15001,
                holdingType : "Leasehold"
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(false);
            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('form-field--error');
            expect(result.isPurchasePriceValid).toEqual(undefined);
            expect(result.isStartDateValid).toEqual('form-field--error');
            expect(result.isEndDateValid).toEqual('form-field--error');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isRelevantRentValid).toEqual(undefined);
        });



        it('should not check relevant rent if property type is non-residential and premium is < 150000 and rent >= 2000', function() {

            var data = {
                propertyType: "Non-residential",
                leaseTerm : {
                    years : 2
                },
                premium : 149999,
                year1Rent : 2000,
                holdingType : "Leasehold"
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(false);
            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('form-field--error');
            expect(result.isPurchasePriceValid).toEqual(undefined);

            expect(result.isStartDateValid).toEqual('form-field--error');
            expect(result.isEndDateValid).toEqual('form-field--error');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isRelevantRentValid).toEqual(undefined);
        });



        it('should check relevant rent if property type is non-residential and premium is < 150000 and rent < 2000', function() {

            var data = {
                propertyType: "Non-residential",
                leaseTerm : {
                    years : 2
                },
                premium : 149999,
                year1Rent : 1999,
                holdingType : "Leasehold"
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(false);
            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('form-field--error');
            expect(result.isPurchasePriceValid).toEqual(undefined);

            expect(result.isStartDateValid).toEqual('form-field--error');
            expect(result.isEndDateValid).toEqual('form-field--error');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isRelevantRentValid).toEqual('form-field--error');
        });



        it('should check relevant rent if property type is non-residential and premium is < 150000 and any rent < 2000', function() {

            var data = {
                propertyType: "Non-residential",
                leaseTerm : {
                    years : 5
                },
                premium : 149999,
                year1Rent : 2000,
                year2Rent : 2000,
                year3Rent : 2000,
                year4Rent : 2000,
                year5Rent : 1999,
                holdingType : "Leasehold"
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(false);
            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('form-field--error');
            expect(result.isPurchasePriceValid).toEqual(undefined);

            expect(result.isStartDateValid).toEqual('form-field--error');
            expect(result.isEndDateValid).toEqual('form-field--error');
            expect(result.isPremiumValid).toEqual('');
            expect(result.isYear1RentValid).toEqual('');
            expect(result.isYear2RentValid).toEqual('');
            expect(result.isYear3RentValid).toEqual('');
            expect(result.isYear4RentValid).toEqual('');
            expect(result.isYear5RentValid).toEqual('');
            expect(result.isRelevantRentValid).toEqual('form-field--error');
        });

        it('should return an empty string when holdingType is Freehold and premium has been defined', function() {

            var data = {
                propertyType: "Residential",
                premium : 15001,
                holdingType : "Freehold"
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(false);
            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('form-field--error');
            expect(result.isPurchasePriceValid).toEqual('');
        });

        it('should return form-field--error when holdingType is Freehold and premium has not been defined', function() {

            var data = {
                propertyType: "Residential",
                holdingType : "Freehold"
            };

            var result = service.validate(data);

            expect(result.isModelValid).toEqual(false);
            expect(result.isHoldingValid).toEqual('');
            expect(result.isPropertyValid).toEqual('');
            expect(result.isEffectiveDateValid).toEqual('form-field--error');
            expect(result.isPurchasePriceValid).toEqual('form-field--error');
        });         
    });
}());
