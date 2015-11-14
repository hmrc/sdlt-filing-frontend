(function() {
    'use strict';

    describe('Lease Dates Validation Service', function () {

        var service;

        beforeEach(angular.mock.module("sdltc.services"));

        beforeEach(inject(function (_leaseDatesValidationService_) {
            service = _leaseDatesValidationService_;
        }));

        it('startDate.mandatory should return an error when no data provided', function() {
            var state = service.validate({});
            expect(state.isValid).toEqual(false);
            expect(state.hasError('startDate')).toEqual('form-field--error');
            expect(state.validationMessage('startDate')).toEqual("You must complete this box. Enter your start date");
        });

        it('startDate.mandatory should return an error when date is empty', function() {
            var form = {
                startDate : ""
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
            expect(state.hasError('startDate')).toEqual('form-field--error');
            expect(state.validationMessage('startDate')).toEqual("You must complete this box. Enter your start date");
        });

        it('startDate.format should return an error when NaN', function() {
            var form = {
                startDate : "hello"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('startDate')).toEqual('form-field--error');
            expect(state.validationMessage('startDate')).toEqual("You have entered an incorrect start date, check your entry and correct it");
        });

        it('startDate.format should return an error when not an integer', function() {
            var form = {
                startDate : "1.1"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('startDate')).toEqual('form-field--error');
            expect(state.validationMessage('startDate')).toEqual("You have entered an incorrect start date, check your entry and correct it");
        });


        it('endDate.mandatory should return an error when no data provided', function() {
            var state = service.validate({});
            expect(state.isValid).toEqual(false);
            expect(state.hasError('endDate')).toEqual('form-field--error');
            expect(state.validationMessage('endDate')).toEqual("You must complete this box. Enter your end date");
        });

        it('endDate.mandatory should return an error when date is empty', function() {
            var form = {
                endDate : ""
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
            expect(state.hasError('endDate')).toEqual('form-field--error');
            expect(state.validationMessage('endDate')).toEqual("You must complete this box. Enter your end date");
        });

        it('endDate.format should return an error when NaN', function() {
            var form = {
                endDate : "hello"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('endDate')).toEqual('form-field--error');
            expect(state.validationMessage('endDate')).toEqual("You have entered an incorrect end date, check your entry and correct it");
        });

        it('endDate.format should return an error when not an integer', function() {
            var form = {
                endDate : "1.1"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('endDate')).toEqual('form-field--error');
            expect(state.validationMessage('endDate')).toEqual("You have entered an incorrect end date, check your entry and correct it");
        });

    });
}());
