(function() {
    'use strict';

    describe('Current Value Validation Service', function () {

        var service;

        beforeEach(angular.mock.module("calc.services"));

        beforeEach(inject(function (_currentValueValidationService_) {
            service = _currentValueValidationService_;
        }));

        it('currentValue.mandatory should return an error when no answer is provided ', function() {
            var state = service.validate({});
            expect(state.isValid).toEqual(false);
            expect(state.hasError('currentValue')).toEqual('form-field--error');
            expect(state.validationMessage('currentValue')).toEqual("Select 'Yes' or 'No'");
        });

        it('currentValue.mandatory shouldnt return an error when no answer is provided ', function() {
            var state = service.validate({
                currentValue: "No"
            });
            expect(state.isValid).toEqual(true);
            expect(state.hasError('currentValue')).toEqual('');
            expect(state.validationMessage('currentValue')).toEqual("");
        });

        it('currentValue.mandatory shouldnt return an error when yes answer is provided ', function() {
            var state = service.validate({
                currentValue: "Yes"
            });
            expect(state.isValid).toEqual(true);
            expect(state.hasError('currentValue')).toEqual('');
            expect(state.validationMessage('currentValue')).toEqual("");
        });
});
}());