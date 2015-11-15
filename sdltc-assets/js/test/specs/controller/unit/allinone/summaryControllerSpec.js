(function() {
    'use strict';

    require("sdltc-module");

    var mocks = require("angular-mocks-wrapper");

    describe('Summary Controller', function () {
        
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
            
            controller = $controller('summaryController', {
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

        it('calling getDisplayValue() with no value should return "-"', function () {
            expect(mockScope.getDisplayValue()).toEqual('-');
        });

        it('calling getDisplayValue() with "undefined" should return "-"', function () {
            expect(mockScope.getDisplayValue('undefined')).toEqual('-');
        });

        it('calling getDisplayValue() with "" should return "-"', function () {
            expect(mockScope.getDisplayValue('')).toEqual('-');
        });

        it('calling getDisplayValue() with "something else" should return "something else"', function () {
            expect(mockScope.getDisplayValue('something else')).toEqual('something else');
        });

        describe('Calling .submit() on the Summary Controller', function () {
            
            beforeEach(mocks.inject(function ($controller, $rootScope) {
                
                mockScope = $rootScope.$new();

                mockDataService = { 
                    getModel : function() {},
                    updateModel : function() {}
                };

                mockNavigationService = { 
                    logView : function() {},
                    next : function() {}
                };

                mockValidationService = {
                    validate : function() {
                        return { isValid : true };
                    }
                };

                spyOn(mockDataService, 'getModel');
                spyOn(mockDataService, 'updateModel');
                spyOn(mockNavigationService, 'logView');
                spyOn(mockNavigationService, 'next');
                spyOn(mockValidationService, 'validate').and.callThrough();
                
                controller = $controller('summaryController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    modelValidationService : mockValidationService,
                    navigationService : mockNavigationService
                });

                mockScope.submit({});
            }));

            // on create
            it('should make 1 call to dataService.getModel', function () {
                expect(mockDataService.getModel.calls.count()).toEqual(1);
            });

            it('should make 1 call to navigationService.logView', function () {
                expect(mockNavigationService.logView.calls.count()).toEqual(1);
            });

            it('should make 1 call to validationService.validate', function () {
                expect(mockValidationService.validate.calls.count()).toEqual(1);
            });

            // on submit
            it('should NOT call dataService.updateModel', function () {
                expect(mockDataService.updateModel.calls.count()).toEqual(0);
            });

            it('should make 1 call to navigationService.next', function () {
                expect(mockNavigationService.next.calls.count()).toEqual(1);
            });
        });

    });

}());
