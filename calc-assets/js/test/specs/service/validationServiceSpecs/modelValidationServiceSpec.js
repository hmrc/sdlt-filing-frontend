(function() {
    'use strict';

    describe('Model Validation Service', function () {

        var service;

        beforeEach(angular.mock.module("calc.services"));

        beforeEach(inject(function (_modelValidationService_) {
            service = _modelValidationService_;
        }));

        it('it should return false when no data provided', function() {
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

    });
}());
