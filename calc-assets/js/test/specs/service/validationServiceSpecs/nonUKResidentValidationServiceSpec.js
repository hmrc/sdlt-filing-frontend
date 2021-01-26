(function() {
    'use strict';

    describe('Non UK Resident Validation Service', function () {

        var service;

        beforeEach(angular.mock.module("calc.services"));

        beforeEach(inject(function (_nonUKResidentValidationService_) {
            service = _nonUKResidentValidationService_;
        }));

        it('nonUKResident.mandatory should return an error when no data provided', function() {
            var state = service.validate({});
            expect(state.isValid).toEqual(false);
            expect(state.hasError('nonUKResident')).toEqual('form-field--error');
            expect(state.validationMessage('nonUKResident')).toEqual("Select 'Yes' or 'No'");
        });

        it('nonUKResident.format should return no error when "Yes" selected', function() {
            var form = {
                nonUKResident : "Yes"
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(true);
            expect(state.hasError('nonUKResident')).toEqual('');
            expect(state.validationMessage('nonUKResident')).toEqual("");
        });

        it('nonUKResident.format should return no error when "No" selected', function() {
            var form = {
                nonUKResident : "No"
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(true);
            expect(state.hasError('nonUKResident')).toEqual('');
            expect(state.validationMessage('nonUKResident')).toEqual("");
        });
    });
}());
