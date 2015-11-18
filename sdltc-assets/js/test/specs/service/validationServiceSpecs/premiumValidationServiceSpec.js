(function() {
    'use strict';

    describe('Premium Validation Service', function () {

        var service;

        beforeEach(angular.mock.module("sdltc.services"));

        beforeEach(inject(function (_premiumValidationService_) {
            service = _premiumValidationService_;
        }));

        it('premium.mandatory should return an error when no data provided', function() {
            var state = service.validate({});
            expect(state.isValid).toEqual(false);
            expect(state.hasError('premium')).toEqual('form-field--error');
            expect(state.validationMessage('premium')).toEqual("You must complete this box. Enter your Premium");
        });

        it('premium.mandatory should return an error when Premium is empty', function() {
            var form = {
                premium : ""
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
            expect(state.hasError('premium')).toEqual('form-field--error');
            expect(state.validationMessage('premium')).toEqual("You must complete this box. Enter your Premium");
        });

        it('premium.format should return an error when NaN', function() {
            var form = {
                premium : "hello"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('premium')).toEqual('form-field--error');
            expect(state.validationMessage('premium')).toEqual("You have entered an incorrect Premium, check your entry and correct it");
        });

        it('premium.format should return no errors when valid decimal', function() {
            var form = {
                premium : "1.1"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(true);
            expect(state.hasError('premium')).toEqual('');
            expect(state.validationMessage('premium')).toEqual("");
        });

        it('premium.format should return an error when value contains comma', function() {
            var form = {
                premium : "1,000.01"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('premium')).toEqual('form-field--error');
            expect(state.validationMessage('premium')).toEqual("You have entered an incorrect Premium, check your entry and correct it");
        });

        it('premium.format should return an error when multiple decimals entered', function() {
            var form = {
                premium : "1.012.01"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('premium')).toEqual('form-field--error');
            expect(state.validationMessage('premium')).toEqual("You have entered an incorrect Premium, check your entry and correct it");
        });

        it('premium.format should return an error when multiple decimals entered', function() {
            var form = {
                premium : "1.012.01"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('premium')).toEqual('form-field--error');
            expect(state.validationMessage('premium')).toEqual("You have entered an incorrect Premium, check your entry and correct it");
        });

    });
}());
