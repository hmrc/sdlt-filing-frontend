(function() {
    'use strict';

    describe('First Time Buyer Validation Service', function () {

        var service;

        beforeEach(angular.mock.module("calc.services"));

        beforeEach(inject(function (_firstTimeBuyerValidationService_) {
            service = _firstTimeBuyerValidationService_;
        }));

        it('firstTimeBuyer.mandatory should return an error when no data provided', function() {
            var state = service.validate({});
            expect(state.isValid).toEqual(false);
            expect(state.hasError('firstTimeBuyer')).toEqual('form-field--error');
            expect(state.validationMessage('firstTimeBuyer')).toEqual("Provide an answer to continue. Select 'Yes' or 'No'");
        });

        it('firstTimeBuyer.format should return no error when "Yes" selected', function() {
            var form = {
                firstTimeBuyer : "Yes"
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(true);
            expect(state.hasError('firstTimeBuyer')).toEqual('');
            expect(state.validationMessage('firstTimeBuyer')).toEqual("");
        });

        it('firstTimeBuyer.format should return no error when "No" selected', function() {
            var form = {
                firstTimeBuyer : "No"
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(true);
            expect(state.hasError('firstTimeBuyer')).toEqual('');
            expect(state.validationMessage('firstTimeBuyer')).toEqual("");
        });
    });
}());
