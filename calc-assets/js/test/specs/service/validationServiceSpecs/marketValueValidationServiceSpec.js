(function() {
    'use strict';

    describe('Market Value Validation Service', function () {

        var service;

        beforeEach(angular.mock.module("calc.services"));

        beforeEach(inject(function (_marketValueValidationService_) {
            service = _marketValueValidationService_;
        }));

        it('paySDLT.mandatory should return an error when no data provided', function() {
            var state = service.validate({});
            expect(state.isValid).toEqual(false);
            expect(state.hasError('paySDLT')).toEqual('form-field--error');
            expect(state.validationMessage('paySDLT')).toEqual("Provide an answer to continue.");
        });

        it('paySDLT.mandatory should return an error when data is Stages and no premium present', function() {
            var state = service.validate({
                paySDLT: "Stages"
            });
            expect(state.isValid).toEqual(false);
            expect(state.hasError('marketValue')).toEqual('form-field--error');
            expect(state.validationMessage('marketValue')).toEqual("Provide an answer to continue.");
        });

        it('paySDLT.mandatory should return an error when data is Stages and premium is greater than £500,000', function() {
            var state = service.validate({
                paySDLT: "Stages",
                premium : "500001"
            });
            expect(state.isValid).toEqual(false);
            expect(state.hasError('marketValue')).toEqual('form-field--error');
            expect(state.validationMessage('marketValue')).toEqual("Enter a value that is £500000 or less.");
        });

        it('paySDLT.mandatory should return an error when data is Stages and premium is greater than £500,000', function() {
            var state = service.validate({
                paySDLT: "Stages",
                premium : "499999"
            });
            expect(state.isValid).toEqual(true);
            expect(state.hasError('marketValue')).toEqual('');
            expect(state.validationMessage('marketValue')).toEqual('');
        });

        it('paySDLT.mandatory should return an error when data is Upfront and no premium present', function() {
            var state = service.validate({
                paySDLT: "Upfront"
            });
            expect(state.isValid).toEqual(false);
            expect(state.hasError('marketValue')).toEqual('form-field--error');
            expect(state.validationMessage('marketValue')).toEqual("Provide an answer to continue.");
        });

        it('paySDLT.mandatory should return an error when data is Upfront and premium is greater than £500,000', function() {
            var state = service.validate({
                paySDLT: "Upfront",
                premium : "500001"
            });
            expect(state.isValid).toEqual(false);
            expect(state.hasError('marketValue')).toEqual('form-field--error');
            expect(state.validationMessage('marketValue')).toEqual("Enter a value that is £500000 or less.");
        });

        it('paySDLT.mandatory shouldnt return an error when data is Upfront and premium is greater than £500,000', function() {
            var state = service.validate({
                paySDLT: "Upfront",
                premium : "499999"
            });
            expect(state.isValid).toEqual(true);
            expect(state.hasError('marketValue')).toEqual('');
            expect(state.validationMessage('marketValue')).toEqual('');
        });
});
}());