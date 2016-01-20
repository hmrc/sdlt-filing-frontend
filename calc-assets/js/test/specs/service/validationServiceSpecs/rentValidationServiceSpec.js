(function() {
    'use strict';

    describe('Rent Validation Service', function () {

        var service;

        beforeEach(angular.mock.module("calc.services"));

        beforeEach(inject(function (_rentValidationService_) {
            service = _rentValidationService_;
        }));

        it('year1Rent.mandatory should return an error when no data provided', function() {
            var form = {
                holdingType : "Leasehold",
                leaseTerm : {
                        years : 5,
                        days : 1
                }
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
            expect(state.hasError('year1Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year1Rent')).toEqual("Enter the annual rent for all the years");
        });

        it('year1Rent.mandatory should return an error when Rent is empty', function() {
            var form = {
                holdingType : "Leasehold",
                leaseTerm : {
                        years : 5,
                        days : 1
                },
                year1Rent : ""
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
            expect(state.hasError('year1Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year1Rent')).toEqual("Enter the annual rent for all the years");
        });

        it('year1Rent.format should return an error when NaN', function() {
            var form = {
                holdingType : "Leasehold",
                leaseTerm : {
                        years : 5,
                        days : 1
                },
                year1Rent : "hello"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('year1Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year1Rent')).toEqual("Enter the rent again - don't use any letters or characters including £");
        });

        it('year1Rent.format should return an error when not an integer', function() {
            var form = {
                holdingType : "Leasehold",
                leaseTerm : {
                        years : 5,
                        days : 1
                },
                year1Rent : "1.1.1"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('year1Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year1Rent')).toEqual("Enter the rent again - don't use any letters or characters including £");
        });

        
        it('year2Rent.mandatory should return an error when no data provided', function() {
            var form = {
                holdingType : "Leasehold",
                leaseTerm : {
                        years : 5,
                        days : 1
                }
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
            expect(state.hasError('year2Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year2Rent')).toEqual("Enter the annual rent for all the years");
        });

        it('year2Rent.mandatory should return an error when Rent is empty', function() {
            var form = {
                holdingType : "Leasehold",
                leaseTerm : {
                        years : 5,
                        days : 1
                },
                year2Rent : ""
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
            expect(state.hasError('year2Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year2Rent')).toEqual("Enter the annual rent for all the years");
        });

        it('year2Rent.format should return an error when NaN', function() {
            var form = {
                holdingType : "Leasehold",
                leaseTerm : {
                        years : 5,
                        days : 1
                },
                year2Rent : "hello"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('year2Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year2Rent')).toEqual("Enter the rent again - don't use any letters or characters including £");
        });

        it('year2Rent.format should return an error when not an integer', function() {
            var form = {
                holdingType : "Leasehold",
                leaseTerm : {
                        years : 5,
                        days : 1
                },
                year2Rent : "1.1.1"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('year2Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year2Rent')).toEqual("Enter the rent again - don't use any letters or characters including £");
        });

        it('year3Rent.mandatory should return an error when no data provided', function() {
            var form = {
                holdingType : "Leasehold",
                leaseTerm : {
                        years : 5,
                        days : 1
                }
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
            expect(state.hasError('year3Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year3Rent')).toEqual("Enter the annual rent for all the years");
        });

        it('year3Rent.mandatory should return an error when Rent is empty', function() {
            var form = {
                holdingType : "Leasehold",
                leaseTerm : {
                        years : 5,
                        days : 1
                },
                year3Rent : ""
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
            expect(state.hasError('year3Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year3Rent')).toEqual("Enter the annual rent for all the years");
        });

        it('year3Rent.format should return an error when NaN', function() {
            var form = {
                holdingType : "Leasehold",
                leaseTerm : {
                        years : 5,
                        days : 1
                },
                year3Rent : "hello"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('year3Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year3Rent')).toEqual("Enter the rent again - don't use any letters or characters including £");
        });

        it('year3Rent.format should return an error when not an integer', function() {
            var form = {
                holdingType : "Leasehold",
                leaseTerm : {
                        years : 5,
                        days : 1
                },
                year3Rent : "1.1.1"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('year3Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year3Rent')).toEqual("Enter the rent again - don't use any letters or characters including £");
        });

        it('year4Rent.mandatory should return an error when no data provided', function() {
            var form = {
                holdingType : "Leasehold",
                leaseTerm : {
                        years : 5,
                        days : 1
                }
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
            expect(state.hasError('year4Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year4Rent')).toEqual("Enter the annual rent for all the years");
        });

        it('year4Rent.mandatory should return an error when Rent is empty', function() {
            var form = {
                holdingType : "Leasehold",
                leaseTerm : {
                        years : 5,
                        days : 1
                },
                year4Rent : ""
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
            expect(state.hasError('year4Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year4Rent')).toEqual("Enter the annual rent for all the years");
        });

        it('year4Rent.format should return an error when NaN', function() {
            var form = {
                holdingType : "Leasehold",
                leaseTerm : {
                        years : 5,
                        days : 1
                },
                year4Rent : "hello"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('year4Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year4Rent')).toEqual("Enter the rent again - don't use any letters or characters including £");
        });

        it('year4Rent.format should return an error when not an integer', function() {
            var form = {
                holdingType : "Leasehold",
                leaseTerm : {
                        years : 5,
                        days : 1
                },
                year4Rent : "1.1.1"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('year4Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year4Rent')).toEqual("Enter the rent again - don't use any letters or characters including £");
        });

        it('year5Rent.mandatory should return an error when no data provided', function() {
            var form = {
                holdingType : "Leasehold",
                leaseTerm : {
                        years : 5,
                        days : 1
                }
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
            expect(state.hasError('year5Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year5Rent')).toEqual("Enter the annual rent for all the years");
        });

        it('year5Rent.mandatory should return an error when Rent is empty', function() {
            var form = {
                holdingType : "Leasehold",
                leaseTerm : {
                        years : 5,
                        days : 1
                },
                year5Rent : ""
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(false);
            expect(state.hasError('year5Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year5Rent')).toEqual("Enter the annual rent for all the years");
        });

        it('year5Rent.format should return an error when NaN', function() {
            var form = {
                holdingType : "Leasehold",
                leaseTerm : {
                        years : 5,
                        days : 1
                },
                year5Rent : "hello"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('year5Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year5Rent')).toEqual("Enter the rent again - don't use any letters or characters including £");
        });

        it('year5Rent.format should return an error when not an integer', function() {
            var form = {
                holdingType : "Leasehold",
                leaseTerm : {
                        years : 5,
                        days : 1
                },
                year5Rent : "1.1.1"
            };

            var state = service.validate(form);

            expect(state.isValid).toEqual(false);
            expect(state.hasError('year5Rent')).toEqual('form-field--error');
            expect(state.validationMessage('year5Rent')).toEqual("Enter the rent again - don't use any letters or characters including £");
        });









        it('rent boxes for years 2 to 5 should not be validated if they are not shown', function() {
            var form = {
                holdingType : "Leasehold",
                leaseTerm : {
                        years : 0,
                        days : 1
                },
                year1Rent : "1",
                year2Rent : "this",
                year3Rent : "shouldn't",
                year4Rent : "be",
                year5Rent : "validated"
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(true);
            expect(form.year2Rent).toEqual('this');
            expect(form.year3Rent).toEqual("shouldn't");
            expect(form.year4Rent).toEqual('be');
            expect(form.year5Rent).toEqual('validated');
        });






        it('year 1 rent box should not be validated if it is not shown', function() {
            var form = {
                holdingType : "Leasehold",
                leaseTerm : undefined,
                year1Rent : "invalid"
            };

            var state = service.validate(form);
            expect(state.isValid).toEqual(true);
            expect(form.year1Rent).toEqual("invalid");
        });


    });
}());
