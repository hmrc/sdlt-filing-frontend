(function() {
    'use strict';

    require("calc-module");

    var mocks = require("angular-mocks-wrapper");

    describe('Detail Controller with valid data', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockModelValidationService,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {
            
            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};
            
            mockDataService = { 
                getModel : function() {}
            };

            mockNavigationService = { 
            };

            mockModelValidationService = {
                validate : function() {
                    return { isModelValid : true };
                }
            };


            spyOn(mockDataService, 'getModel');

            controller = $controller('detailController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
            });
        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });

    });

    describe('Call to printView()', function () {

        var controller,
            mockScope,
            mockDataService,
            mockNavigationService,
            mockModelValidationService,
            mockCalculationService,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {

            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};
            
            mockDataService = {
                getModel : function() {
                    return {
                        holdingType : "Freehold",
                        propertyType : "Non-residential"
                    };
                },
                updateModel : function() { }
            };

            mockNavigationService = {
                printView : function() {}
            };

            mockModelValidationService = {
                validate : function() {
                    return { isModelValid : true };
                }
            };

            mockCalculationService = {
                calculateNonResidentialPremiumSlab: function() {}
            };

            spyOn(mockNavigationService, 'printView');

            controller = $controller('detailController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
                calculationService : mockCalculationService,
            });

            mockScope.printView({});
        }));

        it('should make 1 call to navigationService.printView', function() {
            expect(mockNavigationService.printView.calls.count()).toEqual(1);
        });
    });

    describe('Calling isAdditionalProperty()', function () {

        var controller,
            mockScope,
            mockDataService,
            mockNavigationService,
            mockModelValidationService,
            mockCalculationService;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {

            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};
            
            mockDataService = {
                getModel : function() {
                    return {
                        holdingType : "",
                        propertyType : "",
                        effectiveDate : undefined,
                        twoOrMoreProperties : "",
                        replaceMainResidence : ""
                    };
                },
                updateModel : function() { }
            };

            mockNavigationService = {
                printView : function() {}
            };

            mockModelValidationService = {
                validate : function() {
                    return { isModelValid : true };
                }
            };

            mockCalculationService = {
                calculateNonResidentialPremiumSlab: function() {}
            };

            spyOn(mockNavigationService, 'printView');

            controller = $controller('detailController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
                calculationService : mockCalculationService,
            });

            mockScope.printView({});
        }));

        it('should return true for Res, > 1/4/2016, 2orMore is Yes, replMainRes is No', function() {
            mockScope.data.propertyType = "Residential";
            mockScope.data.effectiveDate = new Date(2016,3,1);
            mockScope.data.twoOrMoreProperties = "Yes";
            mockScope.data.replaceMainResidence = "No";
            expect(mockScope.isAdditionalProperty()).toEqual(true);
        });

        it('should return false for Res, > 1/4/2016, 2orMore is Yes, replMainRes is Yes', function() {
            mockScope.data.propertyType = "Residential";
            mockScope.data.effectiveDate = new Date(2016,3,1);
            mockScope.data.twoOrMoreProperties = "Yes";
            mockScope.data.replaceMainResidence = "Yes";
            expect(mockScope.isAdditionalProperty()).toEqual(false);
        });
    });

    describe('Detail Controller with invalid data', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockModelValidationService,
            mockCalculationService,
            mockLocation,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope, $location) {
            
            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};
            
            mockLocation = $location;

            mockDataService = { 
                getModel : function() {}
            };

            mockNavigationService = { 
            };

            mockModelValidationService = {
                validate : function() {
                    return { isModelValid : false };
                }
            };

            spyOn(mockDataService, 'getModel');

            controller = $controller('detailController', {
                $scope : mockScope,
                $location : mockLocation,
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
            });
        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });

        it('should set the location path to /summary', function() {
            expect(mockLocation.path()).toEqual('/summary');
        });

    });
}());
