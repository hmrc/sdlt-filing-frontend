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
            mockCalculationService,
            calledServiceGetModel = false;

        beforeEach(mocks.module('sdltc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {
            
            mockScope = $rootScope.$new();

            mockDataService = { 
                getModel : function() { return {}; },
                updateModel : function() { }
            };

            mockNavigationService = { 
                logView : function() {} 
            };

            mockModelValidationService = {
                validate : function() {
                    return { isModelValid : true };
                }
            };

            mockCalculationService = {
                calculateResidentialPremiumSlice: function() {},
                calculateResidentialPremiumSlab: function() {},
                calculateNonResidentialPremiumSlab: function() {},
                calculateResidentialLeaseSlab: function() {},
                calculateNonResidentialLeaseSlab: function() {}
            };

            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            spyOn(mockDataService, 'updateModel');
            
            controller = $controller('resultController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
                calculationService : mockCalculationService,
            });
        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });

        it('should make 1 call to navigationService.logView', function () {
            expect(mockNavigationService.logView.calls.count()).toEqual(1);
        });
        
         it('should make 1 call to dataService.updateModel', function () {
            expect(mockDataService.updateModel.calls.count()).toEqual(1);
        });

    });

    describe('Result Controller with invalid data', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockModelValidationService,
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

            mockCalculationService = {
                calculateResidentialPremiumSlab: function() {}
            };

            spyOn(mockDataService, 'getModel');
            spyOn(mockNavigationService, 'logView');
            spyOn(mockCalculationService, 'calculateResidentialPremiumSlab');
            
            controller = $controller('resultController', {
                $scope : mockScope,
                $location : mockLocation,
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
                calculationService : mockCalculationService,
            });
        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });

        it('should make 1 call to navigationService.logView', function () {
            expect(mockNavigationService.logView.calls.count()).toEqual(1);
        });

        it('should make 0 calls to calculationService.calculateResidentialPremiumSlab', function () {
            expect(mockCalculationService.calculateResidentialPremiumSlab.calls.count()).toEqual(0);
        });

        it('should set the location path to /summary', function() {
            expect(mockLocation.path()).toEqual('/summary');
        });

    });
}());
