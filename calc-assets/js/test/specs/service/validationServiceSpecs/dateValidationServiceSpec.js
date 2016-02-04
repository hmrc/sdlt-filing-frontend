(function() {
    'use strict';

    describe('Date Validation Service', function () {

        var service;

        beforeEach(angular.mock.module("calc.services"));

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
            expect(state.validationMessage('effectiveDate')).toEqual("Enter a valid date");
        });

        it('effectiveDate should return the correct minimum date message', function() {
            var form = { effectiveDate : new Date(2012, 2, 21), propertyType: "Residential" };
            var state = service.validate(form);
            expect(state.validationMessage('effectiveDate')).toEqual("Date can't be earlier than 22/3/2012");
        });

        it('effectiveDate should not return an error when it is set to 22 Mar 2012', function() {
            var form = { effectiveDate : new Date(2012, 2, 22) };
            var state = service.validate(form);
            expect(state.isValid).toEqual(true);
        });

        it('expected date before 22 March 2012 should be invalid for Leasehold Residential', function() {
            var form = {
                effectiveDate: new Date(2012, 2, 21),
                propertyType: "Residential",
                holdingType: "Leasehold"
            };
            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
        });

        it('expected date on or before 22 March 2012 should be invalid for Freehold Residential', function() {
            var form = {
                effectiveDate: new Date(2012, 2, 21),
                propertyType: "Residential",
                holdingType: "Freehold"
            };
            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
        });

        it('expected date on or before 22 March 2012 should be valid for Leasehold Non-residential', function() {
            var form = {
                effectiveDate: new Date(2012, 2, 21),
                propertyType: "Non-residential",
                holdingType: "Leasehold"
            };
            var state = service.validate(form);
            expect(state.isValid).toEqual(true);
        });

        it('expected date on or before 22 March 2012 should be valid for Freehold Non-residential', function() {
            var form = {
                effectiveDate: new Date(2012, 2, 21),
                propertyType: "Non-residential",
                holdingType: "Freehold"
            };
            var state = service.validate(form);
            expect(state.isValid).toEqual(true);
        });
    });
}());
