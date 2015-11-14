(function() {
    'use strict';

    describe('Date Validation Service', function () {

        var service;

        beforeEach(angular.mock.module("sdltc.services"));

        beforeEach(inject(function (_dateValidationService_) {
            service = _dateValidationService_;
        }));

        it('effectiveDate.mandatory should return an error when no data provided', function() {
            var state = service.validate({});
            expect(state.isValid).toEqual(false);
            expect(state.hasError('effectiveDate')).toEqual('form-field--error');
            expect(state.validationMessage('effectiveDate')).toEqual("You must complete this box. Enter your date");
        });

        it('effectiveDate.mandatory should return an error when date is empty', function() {
            var form = {
                effectiveDate : ""
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
            expect(state.hasError('effectiveDate')).toEqual('form-field--error');
            expect(state.validationMessage('effectiveDate')).toEqual("You must complete this box. Enter your date");
        });

        it('effectiveDate.format should return an error when NaN', function() {
            var form = {
                effectiveDate : "hello"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('effectiveDate')).toEqual('form-field--error');
            expect(state.validationMessage('effectiveDate')).toEqual("You have entered an incorrect date, check your entry and correct it");
        });

        it('effectiveDate.format should return an error when not an integer', function() {
            var form = {
                effectiveDate : "1.1"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('effectiveDate')).toEqual('form-field--error');
            expect(state.validationMessage('effectiveDate')).toEqual("You have entered an incorrect date, check your entry and correct it");
        });


    });
}());
