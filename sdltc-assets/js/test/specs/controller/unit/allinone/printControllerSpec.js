(function() {
    'use strict';

    require("sdltc-module");

    var mocks = require("angular-mocks-wrapper");

    describe('Print Controller with valid data', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockModelValidationService,
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


            spyOn(mockDataService, 'getModel');
            spyOn(mockNavigationService, 'logView');
                        
            controller = $controller('printController', {
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

    describe('Print Controller with invalid data', function () {
        
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

            spyOn(mockDataService, 'getModel');
            spyOn(mockNavigationService, 'logView');
            
            controller = $controller('printController', {
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
