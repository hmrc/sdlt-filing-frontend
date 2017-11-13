(function() {
    'use strict';

    describe('Property Validation Service', function () {

        var service;

        beforeEach(angular.mock.module("calc.services"));

        beforeEach(inject(function (_propertyValidationService_) {
            service = _propertyValidationService_;
        }));

        it('propertyType.mandatory should return an error when no data provided', function() {
            var state = service.validate({});
            expect(state.isValid).toEqual(false);
            expect(state.hasError('propertyType')).toEqual('form-field--error');
            expect(state.validationMessage('propertyType')).toEqual("Provide an answer to continue. Select 'Residential' or 'Non-residential'");
        });

        it('propertyType.format should return no error when "residential" selected', function() {
            var form = {
                propertyType : "residential"
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(true);
            expect(state.hasError('propertyType')).toEqual('');
            expect(state.validationMessage('propertyType')).toEqual("");
        });

        it('propertyType.format should return no error when "non-residential" selected', function() {
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
