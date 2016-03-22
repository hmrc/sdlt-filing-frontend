(function() {
    'use strict';

    describe('Purchaser Validation Service', function () {

        var service;

        beforeEach(angular.mock.module("calc.services"));

        beforeEach(inject(function (_purchaserValidationService_) {
            service = _purchaserValidationService_;
        }));

        it('purchaser.mandatory should return an error when no data provided', function() {
            var state = service.validate({});
            expect(state.isValid).toEqual(false);
            expect(state.hasError('individual')).toEqual('form-field--error');
            expect(state.validationMessage('individual')).toEqual("Provide an answer to continue. Select 'Yes' or 'No'");
        });

        it('individual.format should return no error when "Yes" selected', function() {
            var form = {
                individual : "Yes"
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(true);
            expect(state.hasError('individual')).toEqual('');
            expect(state.validationMessage('individual')).toEqual("");
        });

        it('individual.format should return no error when "No" selected', function() {
            var form = {
                individual : "No"
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(true);
            expect(state.hasError('individual')).toEqual('');
            expect(state.validationMessage('individual')).toEqual("");
        });
    });
}());
