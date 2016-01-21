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
                logView : function() {} 
            };

            mockModelValidationService = {
                validate : function() {
                    return { isModelValid : true };
                }
            };


            spyOn(mockDataService, 'getModel');
            spyOn(mockNavigationService, 'logView');
                        
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

        it('should make 1 call to navigationService.logView', function () {
            expect(mockNavigationService.logView.calls.count()).toEqual(1);
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
                logView : function() {},
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
                logView : function() {} 
            };

            mockModelValidationService = {
                validate : function() {
                    return { isModelValid : false };
                }
            };

            spyOn(mockDataService, 'getModel');
            spyOn(mockNavigationService, 'logView');
            
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

        it('should make 1 call to navigationService.logView', function () {
            expect(mockNavigationService.logView.calls.count()).toEqual(1);
        });

        it('should set the location path to /summary', function() {
            expect(mockLocation.path()).toEqual('/summary');
        });

    });
}());
