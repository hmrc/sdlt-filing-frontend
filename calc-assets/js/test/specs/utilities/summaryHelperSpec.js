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
                    individual     : undefined,
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
                expect(result[2].answer).toEqual(scope.data.effectiveDate);
            });

            it('should return 7 elements with date 1/4/2016 - twoOrMoreProperties : "Yes", replaceMainResidence : "No"', function(){
                scope.data.effectiveDate = new Date(2016,3,1);
                scope.data.individual = "Yes";
                scope.data.twoOrMoreProperties = "Yes";
                scope.data.replaceMainResidence = "No";
                var result = summaryHelper.summaryHelper(scope, mockModelValidationService);
                expect(result.length).toEqual(7);
                expect(result[3].answer).toEqual('Yes');
                expect(result[4].answer).toEqual('Yes');
                expect(result[5].answer).toEqual('No');
            });

            it('should return 6 elements with date 1/4/2016 - twoOrMoreProperties : "No", replaceMainResidence : undefined', function(){
                scope.data.effectiveDate = new Date(2016,3,1);
                scope.data.individual = "Yes";
                scope.data.twoOrMoreProperties = "No";
                var result = summaryHelper.summaryHelper(scope, mockModelValidationService);
                expect(result.length).toEqual(6);
            });

            it('should return 4 elements with holding type as Non-residential', function() {
                scope.data.propertyType = "Non-residential";
                var result = summaryHelper.summaryHelper(scope, mockModelValidationService);
                expect(result.length).toEqual(4);
                expect(result[1].answer).toEqual('Non-residential');
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
                    startDate           : undefined,
                    endDate             : undefined,
                    leaseTerm                : {
                        years : 0,
                        days : 1
                    },
                    mainResidence            : undefined,
                    sharedOwnership          : undefined,
                    currentValue             : undefined,
                    ownedOtherProperties     : undefined,
                    paySDLT                  : undefined,
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
                scope.data.startDate = new Date(2016,2,16);
                scope.data.endDate = new Date(2017,2,15);
                scope.data.leaseTerm.years = 1;
                scope.data.leaseTerm.days = 0;
                scope.displayYearOneRent = function() {return true;};
                var result = summaryHelper.summaryHelper(scope, mockModelValidationService);
                expect(result.length).toEqual(9);
                expect(result[3].answer).toEqual(new Date(2016,2,16));
                expect(result[4].answer).toEqual(new Date(2017,2,15));
                expect(result[5].answer).toEqual('1 years 0 days');
                expect(result[7].answer).toEqual(1999);
            });

            it('should return 10 elements with date 16/3/2016 and 2 years 0 days lease term', function(){
                scope.data.effectiveDate = new Date(2016,2,16);
                scope.data.startDate = new Date(2016,2,16);
                scope.data.endDate = new Date(2018,2,15);
                scope.data.year2Rent = 1999;
                scope.data.leaseTerm.years = 2;
                scope.data.leaseTerm.days = 0;
                scope.displayYearOneRent = function() {return true;};
                scope.displayYearTwoRent = function() {return true;};
                var result = summaryHelper.summaryHelper(scope, mockModelValidationService);
                expect(result.length).toEqual(10);
                expect(result[5].answer).toEqual('2 years 0 days');
                expect(result[8].answer).toEqual(1999);
            });

            it('should return 11 elements with date 16/3/2016 and 3 years 0 days lease term', function(){
                scope.data.effectiveDate = new Date(2016,2,16);
                scope.data.startDate = new Date(2016,2,16);
                scope.data.endDate = new Date(2019,2,15);
                scope.data.year2Rent = 1999;
                scope.data.year3Rent = 1999;
                scope.data.leaseTerm.years = 3;
                scope.data.leaseTerm.days = 0;
                scope.displayYearOneRent = function() {return true;};
                scope.displayYearTwoRent = function() {return true;};
                scope.displayYearThreeRent = function() {return true;};
                var result = summaryHelper.summaryHelper(scope, mockModelValidationService);
                expect(result.length).toEqual(11);
                expect(result[5].answer).toEqual('3 years 0 days');
                expect(result[9].answer).toEqual(1999);
            });

            it('should return 12 elements with date 16/3/2016 and 4 years 0 days lease term', function(){
                scope.data.effectiveDate = new Date(2016,2,16);
                scope.data.startDate = new Date(2016,2,16);
                scope.data.endDate = new Date(2020,2,15);
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
                expect(result[5].answer).toEqual('4 years 0 days');
                expect(result[10].answer).toEqual(1999);
            });

            it('should return 13 elements with date 16/3/2016 and 5 years 0 days lease term', function(){
                scope.data.effectiveDate = new Date(2016,2,16);
                scope.data.startDate = new Date(2016,2,16);
                scope.data.endDate = new Date(2021,2,15);
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
                expect(result[5].answer).toEqual('5 years 0 days');
                expect(result[11].answer).toEqual(1999);
            });

            it('should return 10 elements with Non-residential property, date 1/4/2016 and 1 year 0 days lease term', function(){
                scope.data.propertyType = "Non-residential";
                scope.data.effectiveDate = new Date(2016,3,1);
                scope.data.startDate = new Date(2016,3,1);
                scope.data.endDate = new Date(2017,2,31);
                scope.data.contractPre201603 = "No";
                scope.data.leaseTerm.years = 1;
                scope.data.leaseTerm.days = 0;
                scope.displayYearOneRent = function() {return true;};
                var result = summaryHelper.summaryHelper(scope, mockModelValidationService);
                expect(result.length).toEqual(10);
                expect(result[9].answer).toEqual('No');
            });

            it('should return 11 elements with Non-residential property, date 1/4/2016 and 1 year 0 days lease term', function(){
                scope.data.propertyType = "Non-residential";
                scope.data.effectiveDate = new Date(2016,3,1);
                scope.data.startDate = new Date(2016,3,1);
                scope.data.endDate = new Date(2017,2,31);
                scope.data.contractPre201603 = "Yes";
                scope.data.contractVariedPost201603 = "Yes";
                scope.data.leaseTerm.years = 1;
                scope.data.leaseTerm.days = 0;
                scope.displayYearOneRent = function() {return true;};
                var result = summaryHelper.summaryHelper(scope, mockModelValidationService);
                expect(result.length).toEqual(11);
                expect(result[9].answer).toEqual('Yes');
                expect(result[10].answer).toEqual('Yes');
            });

            it('should return 16 elements with Non-residential property, date 1/4/2016 and 5 years and 1 day lease term', function() {
                scope.data.propertyType = "Non-residential";
                scope.data.effectiveDate = new Date(2016,3,1);
                scope.data.startDate = new Date(2016,3,1);
                scope.data.endDate = new Date(2021,3,1);
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
                expect(result[13].answer).toEqual('Yes');
                expect(result[14].answer).toEqual('No');
            });
            
            it('should return 14 elements with Non-residential property, date 16/3/2016 and 5 years and 1 day lease term', function() {
                scope.data.propertyType = "Non-residential";
                scope.data.effectiveDate = new Date(2016,2,16);
                scope.data.startDate = new Date(2016,2,16);
                scope.data.endDate = new Date(2021,2,16);
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

            it('should return 14 elements with Non-residential property, date 16/3/2016 and 5 years and 1 day lease term', function() {
                scope.data.propertyType = "Non-residential";
                scope.data.effectiveDate = new Date(2016,2,16);
                scope.data.startDate = new Date(2016,2,16);
                scope.data.endDate = new Date(2021,2,16);
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

            it('should return 17 elements with Residential property, date 22/11/2017 and 1 years and 1 day lease term and Shared Ownership', function() {
                scope.data.effectiveDate = new Date(2017,11,22);
                scope.data.startDate = new Date(2017,11,22);
                scope.data.endDate = new Date(2018,11,22);
                scope.data.year2Rent = 1999;
                scope.data.individual = "Yes";
                scope.data.mainResidence = "Yes";
                scope.data.sharedOwnership = "Yes";
                scope.data.currentValue = "£500,000 or less";
                scope.data.ownedOtherProperties = "No";
                scope.data.twoOrMoreProperties = "No";
                scope.data.leaseTerm.years = 1;
                scope.data.leaseTerm.days = 1;
                scope.data.paySDLT = "Using market value election";
                scope.displayYearOneRent = function() {return true;};
                scope.displayYearTwoRent = function() {return true;};
                var result = summaryHelper.summaryHelper(scope, mockModelValidationService);
                expect(result.length).toEqual(17);
            });

            it('should return 17 elements with Residential property, date 22/11/2017 and 1 years and 1 day lease term and Shared Ownership', function() {
                scope.data.effectiveDate = new Date(2017,11,22);
                scope.data.startDate = new Date(2017,11,22);
                scope.data.endDate = new Date(2018,11,22);
                scope.data.year2Rent = 1999;
                scope.data.individual = "Yes";
                scope.data.mainResidence = "Yes";
                scope.data.sharedOwnership = "Yes";
                scope.data.currentValue = "£500,000 or less";
                scope.data.ownedOtherProperties = "No";
                scope.data.twoOrMoreProperties = "No";
                scope.data.leaseTerm.years = 1;
                scope.data.leaseTerm.days = 1;
                scope.data.paySDLT = "Stages";
                scope.displayYearOneRent = function() {return true;};
                scope.displayYearTwoRent = function() {return true;};
                var result = summaryHelper.summaryHelper(scope, mockModelValidationService);
                expect(result.length).toEqual(17);
            });
            it('should return 15 elements with Residential property, date 22/11/2017 and 1 years and 1 day lease term and Shared Ownership is "No"', function() {
                scope.data.effectiveDate = new Date(2017,11,22);
                scope.data.startDate = new Date(2017,11,22);
                scope.data.endDate = new Date(2018,11,22);
                scope.data.year2Rent = 1999;
                scope.data.individual = "Yes";
                scope.data.mainResidence = "Yes";
                scope.data.sharedOwnership = "No";
                scope.data.ownedOtherProperties = "No";
                scope.data.twoOrMoreProperties = "No";
                scope.data.leaseTerm.years = 1;
                scope.data.leaseTerm.days = 1;
                scope.displayYearOneRent = function() {return true;};
                scope.displayYearTwoRent = function() {return true;};
                var result = summaryHelper.summaryHelper(scope, mockModelValidationService);
                expect(result.length).toEqual(15);
            });

            it('should return 16 elements with Residential property, date 22/11/2017 and 1 years and 1 day lease term and Shared Ownership and Current value is "No"', function() {
                scope.data.effectiveDate = new Date(2017,11,22);
                scope.data.startDate = new Date(2017,11,22);
                scope.data.endDate = new Date(2018,11,22);
                scope.data.year2Rent = 1999;
                scope.data.individual = "Yes";
                scope.data.mainResidence = "Yes";
                scope.data.sharedOwnership = "Yes";
                scope.data.currentValue = "More than £500,000";
                scope.data.ownedOtherProperties = "No";
                scope.data.twoOrMoreProperties = "No";
                scope.data.leaseTerm.years = 1;
                scope.data.leaseTerm.days = 1;
                scope.displayYearOneRent = function() {return true;};
                scope.displayYearTwoRent = function() {return true;};
                var result = summaryHelper.summaryHelper(scope, mockModelValidationService);
                expect(result.length).toEqual(16);
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
                    startDate                : undefined,
                    endDate                  : undefined,
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

            it('should return 1 error messages if FR flow ignores Purchaser page', function(){
                scope.data.holdingType = "Freehold";
                scope.data.effectiveDate = new Date(2016,3,1);
                scope.data.premium = 180000;
                var validatedModel = {};
                validatedModel.isIndividualValid = 'form-field--error';
                var result = summaryHelper.summaryHelper(scope, validatedModel);
                expect(result[3].isValid).toEqual('form-field--error'); // Individual has error
                expect(result.length).toEqual(5);
            });

            it('should return 1 error messages if FR flow ignores Additional Property page', function(){
                scope.data.holdingType = "Freehold";
                scope.data.effectiveDate = new Date(2016,3,1);
                scope.data.individual = "Yes";
                scope.data.premium = 180000;
                var validatedModel = {};
                validatedModel.isTwoOrMorePropertiesValid = 'form-field--error';
                var result = summaryHelper.summaryHelper(scope, validatedModel);
                expect(result[4].isValid).toEqual('form-field--error'); // Additional properties has error
                expect(result.length).toEqual(6);
            });
        });

        describe('Summary for first time buyer', function(){

            var data = {};
            var scope = {};
            var emptyValidatedModel = {};

            beforeEach(function(){
                data = {
                    holdingType    : "Freehold",
                    propertyType   : "Residential",
                    effectiveDate  : undefined,
                    individual     : undefined,
                    premium        : 1000000,
                    twoOrMoreProperties : undefined,
                    replaceMainResidence : undefined
                };
                scope = {
                    data : data
                };

            });

            it('should return 8 elements with date 22/11/2017 - twoOrMoreProperties : "No", ownedOtherProperties : "No", mainResidence : "Yes"', function(){
                scope.data.effectiveDate = new Date(2017,10,22);
                scope.data.individual = "Yes";
                scope.data.twoOrMoreProperties = "No";
                scope.data.ownedOtherProperties = "No";
                scope.data.mainResidence = "Yes";
                var result = summaryHelper.summaryHelper(scope, emptyValidatedModel);
                expect(result.length).toEqual(8);
                expect(result[3].answer).toEqual('Yes');
                expect(result[4].answer).toEqual('No');
                expect(result[5].answer).toEqual('No');
                expect(result[6].answer).toEqual('Yes');
            });

            it('should return 7 elements with date 22/11/2017 - twoOrMoreProperties : "No", ownedOtherProperties : "Yes"', function(){
                scope.data.effectiveDate = new Date(2017,10,22);
                scope.data.individual = "Yes";
                scope.data.twoOrMoreProperties = "No";
                scope.data.ownedOtherProperties = "Yes";
                var result = summaryHelper.summaryHelper(scope, emptyValidatedModel);
                expect(result.length).toEqual(7);
                expect(result[3].answer).toEqual('Yes');
                expect(result[4].answer).toEqual('No');
                expect(result[5].answer).toEqual('Yes');
            });

            it('should return 7 elements with ownedOtherProperties invalid with date 22/11/2017 - twoOrMoreProperties : "No", ownedOtherProperties : undefined', function(){
                scope.data.effectiveDate = new Date(2017,10,22);
                scope.data.individual = "Yes";
                scope.data.twoOrMoreProperties = "No";
                scope.data.ownedOtherProperties = undefined;
                var validatedModel = {};
                validatedModel.isOwnedOtherPropertiesValid = 'form-field--error';
                var result = summaryHelper.summaryHelper(scope, validatedModel);
                expect(result.length).toEqual(7);
                expect(result[3].answer).toEqual('Yes');
                expect(result[4].answer).toEqual('No');
                expect(result[5].isValid).toEqual('form-field--error'); // Owned other properties has error
            });

            it('should return 8 elements with mainResidence invalid with date 22/11/2017 - twoOrMoreProperties : "No", ownedOtherProperties : "No", mainResidence : undefined', function(){
                scope.data.effectiveDate = new Date(2017,10,22);
                scope.data.individual = "Yes";
                scope.data.twoOrMoreProperties = "No";
                scope.data.ownedOtherProperties = "No";
                var validatedModel = {};
                validatedModel.isMainResidenceValid = 'form-field--error';
                var result = summaryHelper.summaryHelper(scope, validatedModel);
                expect(result.length).toEqual(8);
                expect(result[3].answer).toEqual('Yes');
                expect(result[4].answer).toEqual('No');
                expect(result[5].answer).toEqual('No');
                expect(result[6].isValid).toEqual('form-field--error'); // Main residence has error
            });
        });

    });
}());
