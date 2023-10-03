(function() {
    'use strict';

    require("calc-module");

    var mocks = require("angular").mock;

    describe('Purchaser Controller', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockValidationService, 
            mockNavigationService,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {
            
            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};
            
            mockScope.data = {
                individual : "Yes"
            };

            mockDataService = { 
                getModel : function() {}
            };

            mockNavigationService = { 
            };


            spyOn(mockDataService, 'getModel');

            
            mockValidationService = {};

            controller = $controller('purchaserController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                purchaserValidationService : mockValidationService,
                navigationService : mockNavigationService
            });
        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });


        it('should default the state.hasError to ""', function () {
            expect(mockScope.state.hasError()).toEqual('');
        });

        describe('Calling .submit() on the Purchaser Controller with invalid data', function () {
            
            beforeEach(mocks.inject(function ($controller, $rootScope) {
                
                mockScope = $rootScope.$new();
                mockScope.getHelpSetup = function() {return true;};
                
                mockDataService = { 
                    getModel : function() {},
                    updateModel : function() {}
                };

                mockNavigationService = { 
                    next : function() {}
                };

                mockValidationService = {
                    validate : function() {
                        return { isValid : false };
                    }
                };


                spyOn(mockDataService, 'updateModel');
                spyOn(mockNavigationService, 'next');
                spyOn(mockValidationService, 'validate').and.callThrough();

                controller = $controller('purchaserController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    purchaserValidationService : mockValidationService,
                    navigationService : mockNavigationService
                });

                mockScope.submit({});
            }));

            it('should call the validation service once', function () {
                expect(mockValidationService.validate.calls.count()).toEqual(1);
            });

            it('should not call dataService.updateModel', function () {
                expect(mockDataService.updateModel.calls.count()).toEqual(0);
            });


            it('should not call to navigationService.next', function () {
                expect(mockNavigationService.next.calls.count()).toEqual(0);
            });
        });

        describe('Calling .submit() on the Purchaser Controller with valid data', function () {
            
            beforeEach(mocks.inject(function ($controller, $rootScope) {
                
                mockScope = $rootScope.$new();
                mockScope.getHelpSetup = function() {return true;};
                
                mockDataService = { 
                    getModel : function() {},
                    updateModel : function() {}
                };

                mockNavigationService = { 
                    next : function() {}
                };

                mockValidationService = {
                    validate : function() {
                        return { isValid : true };
                    }
                };


                spyOn(mockDataService, 'updateModel');
                spyOn(mockNavigationService, 'next');
                spyOn(mockValidationService, 'validate').and.callThrough();

                controller = $controller('purchaserController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    purchaserValidationService : mockValidationService,
                    navigationService : mockNavigationService
                });

                mockScope.data = {
                    individual : "Yes"
                };

                mockScope.submit({});
            }));

            it('should call the validation service once', function () {
                expect(mockValidationService.validate.calls.count()).toEqual(1);
            });

            it('should call dataService.updateModel once', function () {
                expect(mockDataService.updateModel.calls.count()).toEqual(1);
            });

            it('should call to navigationService.next once', function () {
                expect(mockNavigationService.next.calls.count()).toEqual(1);
            });


        });
    });
}());
