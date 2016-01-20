(function() {
    'use strict';

    describe('Purchase Price Validation Service', function () {

        var service;

        beforeEach(angular.mock.module("calc.services"));

        beforeEach(inject(function (_purchasePriceValidationService_) {
            service = _purchasePriceValidationService_;
        }));

        it('premium.mandatory should return an error when no data provided', function() {
            var state = service.validate({});
            expect(state.isValid).toEqual(false);
            expect(state.hasError('premium')).toEqual('form-field--error');
            expect(state.validationMessage('premium')).toEqual("You must complete this box. Enter your Purchase Price");
        });

        it('premium.mandatory should return an error when Purchase Price is empty', function() {
            var form = {
                premium : ""
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
            expect(state.hasError('premium')).toEqual('form-field--error');
            expect(state.validationMessage('premium')).toEqual("You must complete this box. Enter your Purchase Price");
        });

        it('premium.format should return an error when NaN', function() {
            var form = {
                premium : "hello"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('premium')).toEqual('form-field--error');
            expect(state.validationMessage('premium')).toEqual("Enter the purchase price again - don't use any letters or characters including £");
        });

        it('premium.format should return an error when not an integer', function() {
            var form = {
                premium : "1.1.1"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('premium')).toEqual('form-field--error');
            expect(state.validationMessage('premium')).toEqual("Enter the purchase price again - don't use any letters or characters including £");
        });

        it('premium should be valid', function() {
            var form = {
                premium : "250000"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(true);
            expect(form.premium).toEqual("250000");
        });


    });
}());
