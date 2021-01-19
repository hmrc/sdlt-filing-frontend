(function() {
    'use strict';

    describe('Owned Other Properties Validation Service', function () {

        var service;

        beforeEach(angular.mock.module("calc.services"));

        beforeEach(inject(function (_ownedOtherPropertiesValidationService_) {
            service = _ownedOtherPropertiesValidationService_;
        }));

        it('ownedOtherProperties.mandatory should return an error when no data provided', function() {
            var state = service.validate({});
            expect(state.isValid).toEqual(false);
            expect(state.hasError('ownedOtherProperties')).toEqual('form-field--error');
            expect(state.validationMessage('ownedOtherProperties')).toEqual("Select 'Yes' or 'No'");
        });

        it('ownedOtherProperties.format should return no error when "Yes" selected', function() {
            var form = {
                ownedOtherProperties : "Yes"
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(true);
            expect(state.hasError('ownedOtherProperties')).toEqual('');
            expect(state.validationMessage('ownedOtherProperties')).toEqual("");
        });

        it('ownedOtherProperties.format should return no error when "No" selected', function() {
            var form = {
                ownedOtherProperties : "No"
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(true);
            expect(state.hasError('ownedOtherProperties')).toEqual('');
            expect(state.validationMessage('ownedOtherProperties')).toEqual("");
        });
    });
}());
