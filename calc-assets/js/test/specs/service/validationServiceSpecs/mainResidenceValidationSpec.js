(function() {
    'use strict';

    describe('Main Residence Validation Service', function () {

        var service;

        beforeEach(angular.mock.module("calc.services"));

        beforeEach(inject(function (_mainResidenceValidationService_) {
            service = _mainResidenceValidationService_;
        }));

        it('mainResidence.mandatory should return an error when no data provided', function() {
            var state = service.validate({});
            expect(state.isValid).toEqual(false);
            expect(state.hasError('mainResidence')).toEqual('form-field--error');
            expect(state.validationMessage('mainResidence')).toEqual("Select 'Yes' or 'No'");
        });

        it('mainResidence.format should return no error when "Yes" selected', function() {
            var form = {
                mainResidence : "Yes"
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(true);
            expect(state.hasError('mainResidence')).toEqual('');
            expect(state.validationMessage('mainResidence')).toEqual("");
        });

        it('mainResidence.format should return no error when "No" selected', function() {
            var form = {
                mainResidence : "No"
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(true);
            expect(state.hasError('mainResidence')).toEqual('');
            expect(state.validationMessage('mainResidence')).toEqual("");
        });
    });
}());
