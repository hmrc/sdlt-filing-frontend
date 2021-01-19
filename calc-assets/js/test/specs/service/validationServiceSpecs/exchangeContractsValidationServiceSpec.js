(function() {
    'use strict';

    describe('Contracts Exchanged Validation Service', function () {

        var service;

        beforeEach(angular.mock.module("calc.services"));

        beforeEach(inject(function (_exchangeContractsValidationService_) {
            service = _exchangeContractsValidationService_;
        }));

        it('contractPre201603 should return an error when no data provided', function() {
            var state = service.validate({});
            expect(state.isValid).toEqual(false);
            expect(state.hasError('contractPre201603')).toEqual('form-field--error');
            expect(state.validationMessage('contractPre201603')).toEqual("Select 'Yes' or 'No'");
            expect(state.hasError('contractVariedPost201603')).toEqual('');
            expect(state.validationMessage('contractVariedPost201603')).toEqual("");
        });

        it('should return no error when contractPre201603 = No', function() {
            var state = service.validate({
                contractPre201603 : "No"
            });
            expect(state.isValid).toEqual(true);
            expect(state.hasError('contractPre201603')).toEqual('');
            expect(state.validationMessage('contractPre201603')).toEqual("");
            expect(state.hasError('contractVariedPost201603')).toEqual('');
            expect(state.validationMessage('contractVariedPost201603')).toEqual("");
        });

        it('contractVariedPost201603 should return an error when contractPre201603 = Yes and contractVariedPost201603 not supplied', function() {
            var state = service.validate({
                contractPre201603 : "Yes"
            });
            expect(state.isValid).toEqual(false);
            expect(state.hasError('contractPre201603')).toEqual('');
            expect(state.validationMessage('contractPre201603')).toEqual('');
            expect(state.hasError('contractVariedPost201603')).toEqual('form-field--error');
            expect(state.validationMessage('contractVariedPost201603')).toEqual("Select 'Yes' or 'No'");
        });

        it('should return no error when contractPre201603 = Yes and contractVariedPost201603 = No', function() {
            var state = service.validate({
                contractPre201603 : "Yes",
                contractVariedPost201603 : "No"
            });
            expect(state.isValid).toEqual(true);
            expect(state.hasError('contractPre201603')).toEqual('');
            expect(state.validationMessage('contractPre201603')).toEqual('');
            expect(state.hasError('contractVariedPost201603')).toEqual('');
            expect(state.validationMessage('contractVariedPost201603')).toEqual("");
        });

        it('should return no error when contractPre201603 = Yes and contractVariedPost201603 = Yes', function() {
            var state = service.validate({
                contractPre201603 : "Yes",
                contractVariedPost201603 : "Yes"
            });
            expect(state.isValid).toEqual(true);
            expect(state.hasError('contractPre201603')).toEqual('');
            expect(state.validationMessage('contractPre201603')).toEqual('');
            expect(state.hasError('contractVariedPost201603')).toEqual('');
            expect(state.validationMessage('contractVariedPost201603')).toEqual("");
        });


    });
}());