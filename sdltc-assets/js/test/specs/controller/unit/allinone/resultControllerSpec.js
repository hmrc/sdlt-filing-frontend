(function() {
    'use strict';

    require("sdltc-module");

    var mocks = require("angular-mocks-wrapper");

    describe('Result Controller with valid data', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockModelValidationService,
            mockTransformationService,
            mockCalculationService,
            calledServiceGetModel = false;

        beforeEach(mocks.module('sdltc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {
            
            mockScope = $rootScope.$new();

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

            mockTransformationService = {
                transform : function() {}
            };

            mockCalculationService = {
                calculateTaxCredits: function() {}
            };

            spyOn(mockDataService, 'getModel');
            spyOn(mockNavigationService, 'logView');
            spyOn(mockTransformationService, 'transform');
            spyOn(mockCalculationService, 'calculateTaxCredits');
            
            controller = $controller('resultController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
                transformationService : mockTransformationService,
                calculationService : mockCalculationService,
            });
        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });

        it('should make 1 call to navigationService.logView', function () {
            expect(mockNavigationService.logView.calls.count()).toEqual(1);
        });

        it('should make 1 call to transformationService.transform', function () {
            expect(mockTransformationService.transform.calls.count()).toEqual(1);
        });

        it('should make 1 call to calculationService.calculateTaxCredits', function () {
            expect(mockCalculationService.calculateTaxCredits.calls.count()).toEqual(1);
        });

    });

    describe('Result Controller with invalid data', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockModelValidationService,
            mockTransformationService,
            mockCalculationService,
            mockLocation,
            calledServiceGetModel = false;

        beforeEach(mocks.module('sdltc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope, $location) {
            
            mockScope = $rootScope.$new();

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

            mockTransformationService = {
                transform : function() {}
            };

            mockCalculationService = {
                calculateTaxCredits: function() {}
            };

            spyOn(mockDataService, 'getModel');
            spyOn(mockNavigationService, 'logView');
            spyOn(mockTransformationService, 'transform');
            spyOn(mockCalculationService, 'calculateTaxCredits');
            
            controller = $controller('resultController', {
                $scope : mockScope,
                $location : mockLocation,
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
                transformationService : mockTransformationService,
                calculationService : mockCalculationService,
            });
        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });

        it('should make 1 call to navigationService.logView', function () {
            expect(mockNavigationService.logView.calls.count()).toEqual(1);
        });

        it('should make 0 calls to transformationService.transform', function () {
            expect(mockTransformationService.transform.calls.count()).toEqual(0);
        });

        it('should make 0 calls to calculationService.calculateTaxCredits', function () {
            expect(mockCalculationService.calculateTaxCredits.calls.count()).toEqual(0);
        });

        it('should set the location path to /summary', function() {
            expect(mockLocation.path()).toEqual('/summary');
        });

    });
}());
