(function() {
    'use strict';

    describe('Rent Validation Service', function () {

        var service;

        beforeEach(angular.mock.module("sdltc.services"));

        beforeEach(inject(function (_rentValidationService_) {
            service = _rentValidationService_;
        }));

        it('year1Rent.mandatory should return an error when no data provided', function() {
            var state = service.validate({});
            expect(state.isValid).toEqual(false);
            expect(state.hasError('year1Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year1Rent')).toEqual("You must complete this box. Enter rent");
        });

        it('year1Rent.mandatory should return an error when Rent is empty', function() {
            var form = {
                year1Rent : ""
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
            expect(state.hasError('year1Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year1Rent')).toEqual("You must complete this box. Enter rent");
        });

        it('year1Rent.format should return an error when NaN', function() {
            var form = {
                year1Rent : "hello"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('year1Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year1Rent')).toEqual("You have entered an incorrect rent, check your entry and correct it");
        });

        it('year1Rent.format should return an error when not an integer', function() {
            var form = {
                year1Rent : "1.1"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('year1Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year1Rent')).toEqual("You have entered an incorrect rent, check your entry and correct it");
        });

        
        it('year2Rent.mandatory should return an error when no data provided', function() {
            var state = service.validate({});
            expect(state.isValid).toEqual(false);
            expect(state.hasError('year2Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year2Rent')).toEqual("You must complete this box. Enter rent");
        });

        it('year2Rent.mandatory should return an error when Rent is empty', function() {
            var form = {
                year2Rent : ""
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
            expect(state.hasError('year2Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year2Rent')).toEqual("You must complete this box. Enter rent");
        });

        it('year2Rent.format should return an error when NaN', function() {
            var form = {
                year2Rent : "hello"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('year2Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year2Rent')).toEqual("You have entered an incorrect rent, check your entry and correct it");
        });

        it('year2Rent.format should return an error when not an integer', function() {
            var form = {
                year2Rent : "1.1"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('year2Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year2Rent')).toEqual("You have entered an incorrect rent, check your entry and correct it");
        });

        it('year3Rent.mandatory should return an error when no data provided', function() {
            var state = service.validate({});
            expect(state.isValid).toEqual(false);
            expect(state.hasError('year3Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year3Rent')).toEqual("You must complete this box. Enter rent");
        });

        it('year3Rent.mandatory should return an error when Rent is empty', function() {
            var form = {
                year3Rent : ""
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
            expect(state.hasError('year3Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year3Rent')).toEqual("You must complete this box. Enter rent");
        });

        it('year3Rent.format should return an error when NaN', function() {
            var form = {
                year3Rent : "hello"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('year3Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year3Rent')).toEqual("You have entered an incorrect rent, check your entry and correct it");
        });

        it('year3Rent.format should return an error when not an integer', function() {
            var form = {
                year3Rent : "1.1"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('year3Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year3Rent')).toEqual("You have entered an incorrect rent, check your entry and correct it");
        });

        it('year4Rent.mandatory should return an error when no data provided', function() {
            var state = service.validate({});
            expect(state.isValid).toEqual(false);
            expect(state.hasError('year4Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year4Rent')).toEqual("You must complete this box. Enter rent");
        });

        it('year4Rent.mandatory should return an error when Rent is empty', function() {
            var form = {
                year4Rent : ""
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
            expect(state.hasError('year4Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year4Rent')).toEqual("You must complete this box. Enter rent");
        });

        it('year4Rent.format should return an error when NaN', function() {
            var form = {
                year4Rent : "hello"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('year4Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year4Rent')).toEqual("You have entered an incorrect rent, check your entry and correct it");
        });

        it('year4Rent.format should return an error when not an integer', function() {
            var form = {
                year4Rent : "1.1"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('year4Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year4Rent')).toEqual("You have entered an incorrect rent, check your entry and correct it");
        });

        it('year5Rent.mandatory should return an error when no data provided', function() {
            var state = service.validate({});
            expect(state.isValid).toEqual(false);
            expect(state.hasError('year5Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year5Rent')).toEqual("You must complete this box. Enter rent");
        });

        it('year5Rent.mandatory should return an error when Rent is empty', function() {
            var form = {
                year5Rent : ""
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
            expect(state.hasError('year5Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year5Rent')).toEqual("You must complete this box. Enter rent");
        });

        it('year5Rent.format should return an error when NaN', function() {
            var form = {
                year5Rent : "hello"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('year5Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year5Rent')).toEqual("You have entered an incorrect rent, check your entry and correct it");
        });

        it('year5Rent.format should return an error when not an integer', function() {
            var form = {
                year5Rent : "1.1"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('year5Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year5Rent')).toEqual("You have entered an incorrect rent, check your entry and correct it");
        });



    });
}());
