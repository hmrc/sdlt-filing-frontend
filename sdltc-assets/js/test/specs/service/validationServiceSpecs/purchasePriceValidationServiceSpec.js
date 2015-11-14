(function() {
    'use strict';

    describe('Purchase Price Validation Service', function () {

        var service;

        beforeEach(angular.mock.module("sdltc.services"));

        beforeEach(inject(function (_purchasePriceValidationService_) {
            service = _purchasePriceValidationService_;
        }));

        it('purchasePrice.mandatory should return an error when no data provided', function() {
            var state = service.validate({});
            expect(state.isValid).toEqual(false);
            expect(state.hasError('purchasePrice')).toEqual('form-field--error');
            expect(state.validationMessage('purchasePrice')).toEqual("You must complete this box. Enter your Purchase Price");
        });

        it('purchasePrice.mandatory should return an error when Purchase Price is empty', function() {
            var form = {
                purchasePrice : ""
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
            expect(state.hasError('purchasePrice')).toEqual('form-field--error');
            expect(state.validationMessage('purchasePrice')).toEqual("You must complete this box. Enter your Purchase Price");
        });

        it('purchasePrice.format should return an error when NaN', function() {
            var form = {
                purchasePrice : "hello"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('purchasePrice')).toEqual('form-field--error');
            expect(state.validationMessage('purchasePrice')).toEqual("You have entered an incorrect Purchase Price, check your entry and correct it");
        });

        it('purchasePrice.format should return an error when not an integer', function() {
            var form = {
                purchasePrice : "1.1"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('purchasePrice')).toEqual('form-field--error');
            expect(state.validationMessage('purchasePrice')).toEqual("You have entered an incorrect Purchase Price, check your entry and correct it");
        });


    });
}());
