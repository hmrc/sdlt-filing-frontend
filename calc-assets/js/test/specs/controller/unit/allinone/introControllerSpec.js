(function() {
    'use strict';

    require("calc-module");

    var mocks = require("angular-mocks-wrapper");

    describe('Intro Controller', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {
            
            mockScope = $rootScope.$new();

            mockDataService = { 
                getModel : function() {},
                updateModel : function() {}
            };

            mockNavigationService = { 
                logView : function() {}
            };

            spyOn(mockDataService, 'getModel');
            spyOn(mockDataService, 'updateModel');
            spyOn(mockNavigationService, 'logView');
                        
            controller = $controller('introController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService
            });
        }));

        it('should make 1 call to navigationService.logView', function () {
            expect(mockNavigationService.logView.calls.count()).toEqual(1);
        });

        describe('Calling .startNow on the introController', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            calledServiceGetModel = false;

        beforeEach(mocks.inject(function ($controller, $rootScope) {
            
            mockScope = $rootScope.$new();

            mockDataService = { 
                getModel : function() {},
                updateModel : function() {}
            };

            mockNavigationService = { 
                logView : function() {},
                startNow : function() {} 
            };

            spyOn(mockDataService, 'getModel');
            spyOn(mockDataService, 'updateModel');
            spyOn(mockNavigationService, 'logView');
            spyOn(mockNavigationService, 'startNow');
                        
            controller = $controller('introController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService
            });
            mockScope.startNow({});
        }));

        it('should make 1 call to navigationService.logView', function () {
            expect(mockNavigationService.logView.calls.count()).toEqual(1);
        });

        it('should make 1 call to dataService.updateModel', function () {
            expect(mockDataService.updateModel.calls.count()).toEqual(1);
        });

        it('should make 1 call to navigationService.startNow', function () {
            expect(mockNavigationService.startNow.calls.count()).toEqual(1);
        });
    });
    });

}());
