(function() {
    'use strict';

    describe('Shared Ownership Validation Service', function () {

        var service;

        beforeEach(angular.mock.module("calc.services"));

        beforeEach(inject(function (_sharedOwnershipValidationService_) {
            service = _sharedOwnershipValidationService_;
        }));

        it('sharedOwnership.mandatory should return an error when no answer is provided ', function() {
            var state = service.validate({});
            expect(state.isValid).toEqual(false);
            expect(state.hasError('sharedOwnership')).toEqual('form-field--error');
            expect(state.validationMessage('sharedOwnership')).toEqual("Select 'Yes' or 'No'");
        });

        it('sharedOwnership.mandatory shouldnt return an error when no answer is provided ', function() {
            var state = service.validate({
                sharedOwnership: "No"
            });
            expect(state.isValid).toEqual(true);
            expect(state.hasError('sharedOwnership')).toEqual('');
            expect(state.validationMessage('sharedOwnership')).toEqual("");
        });

        it('sharedOwnership.mandatory shouldnt return an error when yes answer is provided ', function() {
            var state = service.validate({
                sharedOwnership: "Yes"
            });
            expect(state.isValid).toEqual(true);
            expect(state.hasError('sharedOwnership')).toEqual('');
            expect(state.validationMessage('sharedOwners')).toEqual("");
        });
    });
}());