(function() {
    'use strict';

    describe('Lease Dates Validation Service', function () {

        var service;

        beforeEach(angular.mock.module("calc.services"));

        beforeEach(inject(function (_leaseDatesValidationService_) {
            service = _leaseDatesValidationService_;
        }));
        
        it('startDate should return an error when no data provided', function() {
            var state = service.validate({});
            expect(state.isValid).toEqual(false);
        });

        it('startDate should return the correct mandatory error message', function() {
            var state = service.validate({});
            expect(state.validationMessage('startDate')).toEqual("You must complete the start date field");
        });

        it('startDate should return an error when date is empty', function() {
            var form = { startDate : '' };
            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
        });

        it('startDate should return an error when bad data supplied', function() {
            var form = { startDate : 'bad date' };
            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
        });

        it('startDate should return the correct invalid date message', function() {
            var form = { startDate : 'bad date' };
            var state = service.validate(form);
            expect(state.validationMessage('startDate')).toEqual("You have entered an incorrect start date, check your entry and correct it");
        });

        it('endDate should return an error when no data provided', function() {
            var state = service.validate({});
            expect(state.isValid).toEqual(false);
        });

        it('endDate should return the correct mandatory error message', function() {
            var state = service.validate({});
            expect(state.validationMessage('endDate')).toEqual("You must complete the end date field");
        });

        it('endDate should return an error when date is empty', function() {
            var form = { endDate : '' };
            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
        });

        it('endDate should return an error when bad data supplied', function() {
            var form = { endDate : 'bad date' };
            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
        });

        it('endDate should return the correct invalid date message', function() {
            var form = { endDate : 'bad date' };
            var state = service.validate(form);
            expect(state.validationMessage('endDate')).toEqual("You have entered an incorrect end date, check your entry and correct it");
        });

        it('endDate cannot be less that startDate when startDate is valid', function() {
            var form = { 
                startDate: new Date(2000, 1, 2), 
                endDate: new Date(2000, 1, 1)
            };
            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
        });

        it('endDate cannot be less that effectiveDate if it exists', function() {
            var form = { 
                effectiveDate: new Date(2000, 1, 3), 
                startDate: new Date(2000, 1, 1), 
                endDate: new Date(2000, 1, 2)
            };
            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
        });  
    });
}());
