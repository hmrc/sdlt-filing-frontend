(function() {
    'use strict';

    describe('Additional property Validation Service', function () {

        var service;

        beforeEach(angular.mock.module("calc.services"));

        beforeEach(inject(function (_additionalPropertyValidationService_) {
            service = _additionalPropertyValidationService_;
        }));

        it('twoOrMoreProperties.mandatory should return an error when no data provided', function() {
            var state = service.validate({});
            expect(state.isValid).toEqual(false);
            expect(state.hasError('twoOrMoreProperties')).toEqual('form-field--error');
            expect(state.validationMessage('twoOrMoreProperties')).toEqual("Select 'Yes' or 'No'");
        });

        it('twoOrMoreProperties.mandatory should return no error when "two or more properties - Yes" selected', function() {
            var state = service.validate({
                twoOrMoreProperties : "Yes"
            });
            expect(state.isValid).toEqual(false);
            expect(state.hasError('twoOrMoreProperties')).toEqual('');
            expect(state.validationMessage('twoOrMoreProperties')).toEqual("");
        });

        it('twoOrMoreProperties.mandatory should return no error when "two or more properties - No" selected', function() {
            var state = service.validate({
                twoOrMoreProperties : "No"
            });
            expect(state.isValid).toEqual(true);
            expect(state.hasError('twoOrMoreProperties')).toEqual('');
            expect(state.validationMessage('twoOrMoreProperties')).toEqual("");
        });


        it('replaceMainResidence.mandatory should return an error when "two or more properties - Yes" selected and no "replace main residence" selected', function() {
            var state = service.validate({
                twoOrMoreProperties : "Yes"
            });
            expect(state.isValid).toEqual(false);
            expect(state.hasError('replaceMainResidence')).toEqual('form-field--error');
            expect(state.validationMessage('replaceMainResidence')).toEqual("Select 'Yes' or 'No'");
        });


        it('replaceMainResidence.mandatory should return no error when "two or more properties - No" selected and no "replace main residence" selected', function() {
            var state = service.validate({
                twoOrMoreProperties : "No"
            });
            expect(state.isValid).toEqual(true);
            expect(state.hasError('twoOrMoreProperties')).toEqual('');
            expect(state.validationMessage('twoOrMoreProperties')).toEqual("");
        });


        it('replaceMainResidence.mandatory should return no error when "two or more properties - Yes" selected and no "replace main residence - Yes" selected', function() {
            var state = service.validate({
                twoOrMoreProperties : "No",
                replaceMainResidence : "Yes"
            });
            expect(state.isValid).toEqual(true);
            expect(state.hasError('twoOrMoreProperties')).toEqual('');
            expect(state.validationMessage('twoOrMoreProperties')).toEqual("");
        });


        it('replaceMainResidence.mandatory should return no error when "two or more properties - Yes" selected and no "replace main residence - No" selected', function() {
            var state = service.validate({
                twoOrMoreProperties : "No",
                replaceMainResidence : "No"
            });
            expect(state.isValid).toEqual(true);
            expect(state.hasError('twoOrMoreProperties')).toEqual('');
            expect(state.validationMessage('twoOrMoreProperties')).toEqual("");
        });


        it('twoOrMoreProperties.format should return no error when "Yes" selected', function() {
            var form = {
                twoOrMoreProperties : "Yes"
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
            expect(state.hasError('twoOrMoreProperties')).toEqual('');
            expect(state.validationMessage('twoOrMoreProperties')).toEqual("");
        });

        it('twoOrMoreProperties.format should return no error when "No" selected', function() {
            var form = {
                twoOrMoreProperties : "No"
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(true);
            expect(state.hasError('twoOrMoreProperties')).toEqual('');
            expect(state.validationMessage('twoOrMoreProperties')).toEqual("");
        });


        it('replaceMainResidence.format should return no error when "Yes" selected', function() {
            var form = {
                twoOrMoreProperties : "Yes",
                replaceMainResidence : "Yes"
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(true);
            expect(state.hasError('replaceMainResidence')).toEqual('');
            expect(state.validationMessage('replaceMainResidence')).toEqual("");
        });

        it('replaceMainResidence.format should return no error when "No" selected', function() {
            var form = {
                twoOrMoreProperties : "Yes",
                replaceMainResidence : "No"
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(true);
            expect(state.hasError('replaceMainResidence')).toEqual('');
            expect(state.validationMessage('replaceMainResidence')).toEqual("");
        });
    });
}());