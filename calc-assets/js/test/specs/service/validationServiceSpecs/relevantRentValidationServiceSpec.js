(function() {
    'use strict';

    describe('Relevant Rent Validation Service', function () {

        var service;

        beforeEach(angular.mock.module("calc.services"));

        beforeEach(inject(function (_relevantRentValidationService_) {
            service = _relevantRentValidationService_;
        }));

        it('relevantRent.mandatory should return an error when no data provided', function() {
            var state = service.validate({});
            expect(state.isValid).toEqual(false);
            expect(state.hasError('relevantRent')).toEqual('form-field--error');
            expect(state.validationMessage('relevantRent')).toEqual("Please enter the rental figure");
        });

        it('relevantRent.mandatory should return an error when Relevant Rent is empty', function() {
            var form = {
                relevantRent : ""
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
            expect(state.hasError('relevantRent')).toEqual('form-field--error');
            expect(state.validationMessage('relevantRent')).toEqual("Please enter the rental figure");
        });

        it('relevantRent.format should return an error when NaN', function() {
            var form = {
                relevantRent : "hello"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('relevantRent')).toEqual('form-field--error');
            expect(state.validationMessage('relevantRent')).toEqual("Enter the relevant rent again - don't use any letters or characters including £");
        });

        it('relevantRent.format should return an error when not an integer', function() {
            var form = {
                relevantRent : "1.1.1"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('relevantRent')).toEqual('form-field--error');
            expect(state.validationMessage('relevantRent')).toEqual("Enter the relevant rent again - don't use any letters or characters including £");
        });

        it('relevantRent should be valid', function() {
            var form = {
                relevantRent : "100"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(true);
            expect(form.relevantRent).toEqual("100");
        });


    });
}());
