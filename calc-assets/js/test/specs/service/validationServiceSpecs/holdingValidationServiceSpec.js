(function() {
    'use strict';

    describe('Holding Validation Service', function () {

        var service;

        beforeEach(angular.mock.module("calc.services"));

        beforeEach(inject(function (_holdingValidationService_) {
            service = _holdingValidationService_;
        }));

        it('holdingType.mandatory should return an error when no data provided', function() {
            var state = service.validate({});
            expect(state.isValid).toEqual(false);
            expect(state.hasError('holdingType')).toEqual('form-field--error');
            expect(state.validationMessage('holdingType')).toEqual("Provide an answer to continue. Select 'Freehold' or 'Leasehold'");
        });

        it('holdingType.format should return no error when "Freehold" selected', function() {
            var form = {
                holdingType : "freehold"
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(true);
            expect(state.hasError('holdingType')).toEqual('');
            expect(state.validationMessage('holdingType')).toEqual("");
        });

        it('holdingType.format should return no error when "Leasehold" selected', function() {
            var form = {
                holdingType : "leasehold"
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(true);
            expect(state.hasError('holdingType')).toEqual('');
            expect(state.validationMessage('holdingType')).toEqual("");
        });
    });
}());
