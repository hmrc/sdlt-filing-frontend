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
            mockScope.getHelpSetup = function() {return true;};
            
            mockDataService = { 
                getModel : function() {},
                updateModel : function() {}
            };

            mockNavigationService = { 
            };

            spyOn(mockDataService, 'getModel');
            spyOn(mockDataService, 'updateModel');

            controller = $controller('introController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService
            });
        }));

        describe('Calling .startNow on the introController', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            calledServiceGetModel = false;

        beforeEach(mocks.inject(function ($controller, $rootScope) {
            
            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};
            
            mockDataService = { 
                getModel : function() {},
                updateModel : function() {}
            };

            mockNavigationService = { 
                startNow : function() {}
            };

            spyOn(mockDataService, 'getModel');
            spyOn(mockDataService, 'updateModel');
            spyOn(mockNavigationService, 'startNow');
                        
            controller = $controller('introController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService
            });
            mockScope.startNow({});
        }));

        it('should make 1 call to dataService.updateModel', function () {
            expect(mockDataService.updateModel.calls.count()).toEqual(1);
        });

        it('should make 1 call to navigationService.startNow', function () {
            expect(mockNavigationService.startNow.calls.count()).toEqual(1);
        });
    });
    });

}());
