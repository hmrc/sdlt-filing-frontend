(function() {
    'use strict';

    describe('Additional property Validation Service', function () {

        var service;

        beforeEach(angular.mock.module("calc.services"));

        beforeEach(inject(function (_additionalPropertyValidationService_) {
            service = _additionalPropertyValidationService_;
        }));

        it('propertyType.mandatory should return an error when no data provided', function() {
            var state = service.validate({});
            expect(state.isValid).toEqual(false);
            expect(state.hasError('propertyType')).toEqual('form-field--error');
            expect(state.validationMessage('propertyType')).toEqual("Provide an answer to continue. Select 'Residential' or 'Non-residential'");
        });

        it('propertyType.format should return no error when "Freehold" selected', function() {
            var form = {
                propertyType : "residential"
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(true);
            expect(state.hasError('propertyType')).toEqual('');
            expect(state.validationMessage('propertyType')).toEqual("");
        });

        it('propertyType.format should return no error when "Leasehold" selected', function() {
            var form = {
                propertyType : "non-residential"
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(true);
            expect(state.hasError('propertyType')).toEqual('');
            expect(state.validationMessage('propertyType')).toEqual("");
        });
    });
}());