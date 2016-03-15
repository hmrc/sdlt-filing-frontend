(function() {
    'use strict';

    describe('Summary Helper', function () {

        var summaryHelper = require("../../../src/utilities/summaryHelper.js");

        describe('Summary for freehold', function(){
            
            var data = {};
            var scope = {};
            var mockModelValidationService = {};

            beforeEach(function(){
                data = {
                    holdingType    : "Freehold",
                    propertyType   : "Residential",
                    effectiveDate  : undefined,
                    premium        : 1000000,
                    twoOrMoreProperties : undefined,
                    replaceMainResidence : undefined
                };
                scope = {
                    data : data
                };

            });

            it('should return 4 elements with date 16/3/2016', function(){
                scope.data.effectiveDate = new Date(2016,2,16);
                var result = summaryHelper.summaryHelper(scope, mockModelValidationService);
                expect(result.length).toEqual(4);
            });

            it('should return 6 elements with date 1/4/2016 - twoOrMoreProperties : "Yes", replaceMainResidence : "No"', function(){
                scope.data.effectiveDate = new Date(2016,3,1);
                scope.data.twoOrMoreProperties = "Yes";
                scope.data.replaceMainResidence = "No";
                var result = summaryHelper.summaryHelper(scope, mockModelValidationService);
                expect(result.length).toEqual(6);
            });

            it('should return 5 elements with date 1/4/2016 - twoOrMoreProperties : "No", replaceMainResidence : undefined', function(){
                scope.data.effectiveDate = new Date(2016,3,1);
                scope.data.twoOrMoreProperties = "No";
                var result = summaryHelper.summaryHelper(scope, mockModelValidationService);
                expect(result.length).toEqual(5);
            });

            it('should return 4 elements with holding type as Non-residential', function() {
                scope.data.propertyType = "Non-residential";
                var result = summaryHelper.summaryHelper(scope, mockModelValidationService);
                expect(result.length).toEqual(4);
            });
        });

        describe('Summary for Leasehold', function(){
            var data = {};
            var scope = {};
            var mockModelValidationService = {};

            beforeEach(function(){
                data = {
                    holdingType              : "Leasehold",
                    propertyType             : "Residential",
                    effectiveDate            : undefined,
                    leaseStartDate           : undefined,
                    leaseEndDate             : undefined,
                    leaseTerm                : {
                        years : 0,
                        days : 1
                    },
                    premium                  : 100000,
                    twoOrMoreProperties      : undefined,
                    replaceMainResidence     : undefined,
                    year1Rent                : 1999,
                    year2Rent                : undefined,
                    year3Rent                : undefined,
                    year4Rent                : undefined,
                    year5Rent                : undefined,
                    highestRent              : 1999,
                    relevantRent             : undefined,
                    contractPre201603        : undefined,
                    contractVariedPost201603 : undefined
                };
                scope = {
                    data : data
                };

            });

            it('should return 9 elements with date 16/3/2016 and 1 year 0 days lease term', function(){
                scope.data.effectiveDate = new Date(2016,2,16);
                scope.data.leaseStartDate = new Date(2016,2,16);
                scope.data.leaseEndDate = new Date(2017,2,15);
                scope.data.leaseTerm.years = 1;
                scope.data.leaseTerm.days = 0;
                scope.displayYearOneRent = function() {return true;};
                var result = summaryHelper.summaryHelper(scope, mockModelValidationService);
                expect(result.length).toEqual(9);
            });

            it('should return 10 elements with date 16/3/2016 and 2 years 0 days lease term', function(){
                scope.data.effectiveDate = new Date(2016,2,16);
                scope.data.leaseStartDate = new Date(2016,2,16);
                scope.data.leaseEndDate = new Date(2018,2,15);
                scope.data.year2Rent = 1999;
                scope.data.leaseTerm.years = 2;
                scope.data.leaseTerm.days = 0;
                scope.displayYearOneRent = function() {return true;};
                scope.displayYearTwoRent = function() {return true;};
                var result = summaryHelper.summaryHelper(scope, mockModelValidationService);
                expect(result.length).toEqual(10);
            });

            it('should return 11 elements with date 16/3/2016 and 3 years 0 days lease term', function(){
                scope.data.effectiveDate = new Date(2016,2,16);
                scope.data.leaseStartDate = new Date(2016,2,16);
                scope.data.leaseEndDate = new Date(2019,2,15);
                scope.data.year2Rent = 1999;
                scope.data.year3Rent = 1999;
                scope.data.leaseTerm.years = 3;
                scope.data.leaseTerm.days = 0;
                scope.displayYearOneRent = function() {return true;};
                scope.displayYearTwoRent = function() {return true;};
                scope.displayYearThreeRent = function() {return true;};
                var result = summaryHelper.summaryHelper(scope, mockModelValidationService);
                expect(result.length).toEqual(11);
            });

            it('should return 12 elements with date 16/3/2016 and 4 years 0 days lease term', function(){
                scope.data.effectiveDate = new Date(2016,2,16);
                scope.data.leaseStartDate = new Date(2016,2,16);
                scope.data.leaseEndDate = new Date(2020,2,15);
                scope.data.year2Rent = 1999;
                scope.data.year3Rent = 1999;
                scope.data.year4Rent = 1999;
                scope.data.leaseTerm.years = 4;
                scope.data.leaseTerm.days = 0;
                scope.displayYearOneRent = function() {return true;};
                scope.displayYearTwoRent = function() {return true;};
                scope.displayYearThreeRent = function() {return true;};
                scope.displayYearFourRent = function() {return true;};
                var result = summaryHelper.summaryHelper(scope, mockModelValidationService);
                expect(result.length).toEqual(12);
            });

            it('should return 13 elements with date 16/3/2016 and 5 years 0 days lease term', function(){
                scope.data.effectiveDate = new Date(2016,2,16);
                scope.data.leaseStartDate = new Date(2016,2,16);
                scope.data.leaseEndDate = new Date(2021,2,15);
                scope.data.year2Rent = 1999;
                scope.data.year3Rent = 1999;
                scope.data.year4Rent = 1999;
                scope.data.year5Rent = 1999;
                scope.data.leaseTerm.years = 5;
                scope.data.leaseTerm.days = 0;
                scope.displayYearOneRent = function() {return true;};
                scope.displayYearTwoRent = function() {return true;};
                scope.displayYearThreeRent = function() {return true;};
                scope.displayYearFourRent = function() {return true;};
                scope.displayYearFiveRent = function() {return true;};
                var result = summaryHelper.summaryHelper(scope, mockModelValidationService);
                expect(result.length).toEqual(13);
            });

            it('should return 10 elements with Non-residential property, date 1/4/2016 and 1 year 0 days lease term', function(){
                scope.data.propertyType = "Non-residential";
                scope.data.effectiveDate = new Date(2016,3,1);
                scope.data.leaseStartDate = new Date(2016,3,1);
                scope.data.leaseEndDate = new Date(2017,2,31);
                scope.data.contractPre201603 = "No";
                scope.data.leaseTerm.years = 1;
                scope.data.leaseTerm.days = 0;
                scope.displayYearOneRent = function() {return true;};
                var result = summaryHelper.summaryHelper(scope, mockModelValidationService);
                expect(result.length).toEqual(10);
            });

            it('should return 11 elements with Non-residential property, date 1/4/2016 and 1 year 0 days lease term', function(){
                scope.data.propertyType = "Non-residential";
                scope.data.effectiveDate = new Date(2016,3,1);
                scope.data.leaseStartDate = new Date(2016,3,1);
                scope.data.leaseEndDate = new Date(2017,2,31);
                scope.data.contractPre201603 = "Yes";
                scope.data.contractVariedPost201603 = "Yes";
                scope.data.leaseTerm.years = 1;
                scope.data.leaseTerm.days = 0;
                scope.displayYearOneRent = function() {return true;};
                var result = summaryHelper.summaryHelper(scope, mockModelValidationService);
                expect(result.length).toEqual(11);
            });

            it('should return 16 elements with Non-residential property, date 1/4/2016 and 5 years and 1 day lease term', function() {
                scope.data.propertyType = "Non-residential";
                scope.data.effectiveDate = new Date(2016,3,1);
                scope.data.leaseStartDate = new Date(2016,3,1);
                scope.data.leaseEndDate = new Date(2021,3,1);
                scope.data.year2Rent = 1999;
                scope.data.year3Rent = 1999;
                scope.data.year4Rent = 1999;
                scope.data.year5Rent = 1999;
                scope.data.contractPre201603 = "Yes";
                scope.data.contractVariedPost201603 = "No";
                scope.data.leaseTerm.years = 5;
                scope.data.leaseTerm.days = 1;
                scope.data.relevantRent = 100;
                scope.displayYearOneRent = function() {return true;};
                scope.displayYearTwoRent = function() {return true;};
                scope.displayYearThreeRent = function() {return true;};
                scope.displayYearFourRent = function() {return true;};
                scope.displayYearFiveRent = function() {return true;};
                var result = summaryHelper.summaryHelper(scope, mockModelValidationService);
                expect(result.length).toEqual(16);
            });
            
            it('should return 14 elements with Non-residential property, date 16/3/2016 and 5 years and 1 day lease term', function() {
                scope.data.propertyType = "Non-residential";
                scope.data.effectiveDate = new Date(2016,2,16);
                scope.data.leaseStartDate = new Date(2016,2,16);
                scope.data.leaseEndDate = new Date(2021,2,16);
                scope.data.year2Rent = 1999;
                scope.data.year3Rent = 1999;
                scope.data.year4Rent = 1999;
                scope.data.year5Rent = 1999;
                scope.data.leaseTerm.years = 5;
                scope.data.leaseTerm.days = 1;
                scope.data.relevantRent = 100;
                scope.displayYearOneRent = function() {return true;};
                scope.displayYearTwoRent = function() {return true;};
                scope.displayYearThreeRent = function() {return true;};
                scope.displayYearFourRent = function() {return true;};
                scope.displayYearFiveRent = function() {return true;};
                var result = summaryHelper.summaryHelper(scope, mockModelValidationService);
                expect(result.length).toEqual(14);
            });

        });

        describe('Summary page with errors', function(){

            var data = {};
            var scope = {};
            var mockModelValidationService = {};

            beforeEach(function(){
                data = {
                    holdingType              : "Leasehold",
                    propertyType             : "Residential",
                    effectiveDate            : undefined,
                    leaseStartDate           : undefined,
                    leaseEndDate             : undefined,
                    leaseTerm                : undefined,
                    premium                  : undefined,
                    twoOrMoreProperties      : undefined,
                    replaceMainResidence     : undefined,
                    year1Rent                : undefined,
                    year2Rent                : undefined,
                    year3Rent                : undefined,
                    year4Rent                : undefined,
                    year5Rent                : undefined,
                    highestRent              : undefined,
                    relevantRent             : undefined,
                    contractPre201603        : undefined,
                    contractVariedPost201603 : undefined
                };
                scope = {
                    data : data
                };

            });

            it('should return 3 default error messages if scope.data is undefined', function(){
                scope.data = undefined;
                var validatedModel = {};
                validatedModel.isHoldingValid = 'form-field--error';
                validatedModel.isPropertyValid = 'form-field--error';
                validatedModel.isEffectiveDateValid = 'form-field--error';
                var result = summaryHelper.summaryHelper(scope, validatedModel);
                expect(result[0].isValid).toEqual('form-field--error');
                expect(result.length).toEqual(3);
            });

            it('should return 7 error messages if LN and only relevant rent defined', function(){
                var validatedModel = {};
                scope.data.propertyType = "Non-residential";
                scope.data.relevantRent = 1;
                validatedModel.isEffectiveDateValid = 'form-field--error';
                validatedModel.isStartDateValid = 'form-field--error';
                validatedModel.isEndDateValid = 'form-field--error';
                validatedModel.isPremiumValid = 'form-field--error';
                var result = summaryHelper.summaryHelper(scope, validatedModel);
                expect(result[2].isValid).toEqual('form-field--error'); // Effective has error
                expect(result[3].isValid).toEqual('form-field--error'); // Start date has error
                expect(result[4].isValid).toEqual('form-field--error'); // End date has error
                expect(result[5].isValid).toEqual('form-field--error'); // Premium has eror
                expect(result.length).toEqual(7);
            });

        });

    });
}());
