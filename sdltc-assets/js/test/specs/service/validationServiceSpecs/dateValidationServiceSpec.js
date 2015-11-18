(function() {
    'use strict';

    describe('Date Validation Service', function () {

        var service;

        beforeEach(angular.mock.module("sdltc.services"));

        beforeEach(inject(function (_dateValidationService_) {
            service = _dateValidationService_;
        }));

        it('effectiveDate should return an error when no data provided', function() {
            var state = service.validate({});
            expect(state.isValid).toEqual(false);
        });

        it('effectiveDate should return the correct mandatory error message', function() {
            var state = service.validate({});
            expect(state.validationMessage('effectiveDate')).toEqual("You must complete the effective date field");
        });

        it('effectiveDate should return an error when date is empty', function() {
            var form = { effectiveDate : '' };
            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
        });

        it('effectiveDate should return an error when bad data supplied', function() {
            var form = { effectiveDate : 'bad date' };
            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
        });

        it('effectiveDate should return the correct invalid date message', function() {
            var form = { effectiveDate : 'bad date' };
            var state = service.validate(form);
            expect(state.validationMessage('effectiveDate')).toEqual("You have entered an incorrect date, check your entry and correct it");
        });

        it('effectiveDate should return an error when it is set to before 21 March 2012', function() {
            var form = { effectiveDate : new Date(2012, 2, 20) };
            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
        });

        it('effectiveDate should return the correct minimum date message', function() {
            var form = { effectiveDate : new Date(2012, 2, 20) };
            var state = service.validate(form);
            expect(state.validationMessage('effectiveDate')).toEqual("The effective date cannot be before 21 Mar 2012");
        });

        it('effectiveDate should not return an error when it is set to 21 Mar 2012', function() {
            var form = { effectiveDate : new Date(2012, 2, 21) };
            var state = service.validate(form);
            expect(state.isValid).toEqual(true);
        });
    });
}());
