(function() {
    'use strict';

    require("calc-module");

    var mocks = require("angular").mock;

    describe('Date Controller', function () {
        
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
            
            mockDataService = { 
                getModel : function() {}
            };

            mockNavigationService = { 
            };

            spyOn(mockDataService, 'getModel');

            mockValidationService = {};

            controller = $controller('dateController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                dateValidationService : mockValidationService,
                navigationService : mockNavigationService
            });
        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });

        it('should default the state.hasError to ""', function () {
            expect(mockScope.state.hasError()).toEqual('');
        });

        it('should define a function named updateEffectiveDate', function () {
            expect(mockScope.updateEffectiveDate).toBeDefined();
        });

        describe('Calling updateEffectiveDate', function () {
            
            beforeEach(mocks.inject(function ($controller, $rootScope) {
                    
                mockScope = $rootScope.$new();
                mockScope.getHelpSetup = function() {return true;};
                
                mockDataService = { 
                    getModel : function() {}
                };

                mockNavigationService = { 
                };

                spyOn(mockDataService, 'getModel');

                mockValidationService = {};

                controller = $controller('dateController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    dateValidationService : mockValidationService,
                    navigationService : mockNavigationService
                });
            }));

            it('with valid date parts should update the effective date', function () {

                mockScope.data = {
                    effectiveDateDay: '23',
                    effectiveDateMonth: '9',
                    effectiveDateYear: '1977'
                };

                mockScope.updateEffectiveDate();
                expect(mockScope.data.effectiveDate).toEqual(new Date(1977, 8, 23));
            });

            it('with invalid date parts should mark effective date as bad', function () {

                mockScope.data = {
                    effectiveDateDay: '23',
                    effectiveDateMonth: 'bob',
                    effectiveDateYear: '1977'
                };

                mockScope.updateEffectiveDate();
                expect(mockScope.data.effectiveDate).toEqual('bad date');
            });

            it('with blank date parts should clear the effective date', function () {

                mockScope.data = {
                    effectiveDateDay: '',
                    effectiveDateMonth: '',
                    effectiveDateYear: ''
                };

                mockScope.updateEffectiveDate();
                expect(mockScope.data.effectiveDate).toEqual('');
            });
        });

        describe('Calling .submit() on the Date Controller with invalid data', function () {
            
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
                
                controller = $controller('dateController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    dateValidationService : mockValidationService,
                    navigationService : mockNavigationService
                });

                mockScope.data = {};

                mockScope.submit();
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

        describe('Calling .submit() on the Date Controller with valid data', function () {
            
            beforeEach(mocks.inject(function ($controller, $rootScope) {
                
                mockScope = $rootScope.$new();
                mockScope.getHelpSetup = function() {return true;};
                
                mockDataService = { 
                    getModel : function() { return {}; },
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

                spyOn(mockDataService, 'updateModel').and.callThrough();
                spyOn(mockNavigationService, 'next');
                spyOn(mockValidationService, 'validate').and.callThrough();
                
                controller = $controller('dateController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    dateValidationService : mockValidationService,
                    navigationService : mockNavigationService
                });

                mockScope.submit();
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
