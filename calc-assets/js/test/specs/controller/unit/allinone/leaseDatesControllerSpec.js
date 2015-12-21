(function() {
    'use strict';

    require("sdltc-module");

    var mocks = require("angular-mocks-wrapper");

    describe('Lease Dates Controller', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockValidationService, 
            mockNavigationService,
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

            spyOn(mockDataService, 'getModel');
            spyOn(mockNavigationService, 'logView');
            
            mockValidationService = {};

            controller = $controller('leaseDatesController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                leaseDatesValidationService : mockValidationService,
                navigationService : mockNavigationService
            });
        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });

        it('should make 1 call to navigationService.logView', function () {
            expect(mockNavigationService.logView.calls.count()).toEqual(1);
        });

        it('should default the state.hasError to ""', function () {
            expect(mockScope.state.hasError()).toEqual('');
        });

        it('should define a function named updateStartDate', function () {
            expect(mockScope.updateStartDate).toBeDefined();
        });

        it('should define a function named updateEndDate', function () {
            expect(mockScope.updateEndDate).toBeDefined();
        });

        describe('Calling updateStartDate', function () {
            
            beforeEach(mocks.inject(function ($controller, $rootScope) {
                    
                mockScope = $rootScope.$new();

                mockDataService = { 
                    getModel : function() {}
                };

                mockNavigationService = { 
                    logView : function() {} 
                };

                spyOn(mockDataService, 'getModel');
                spyOn(mockNavigationService, 'logView');
                
                mockValidationService = {};

                controller = $controller('leaseDatesController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    leaseDatesValidationService : mockValidationService,
                    navigationService : mockNavigationService
                });
            }));

            it('with valid date parts should update the start date', function () {

                mockScope.data = {
                    startDateDay: '23',
                    startDateMonth: '9',
                    startDateYear: '1977'
                };

                mockScope.updateStartDate();
                expect(mockScope.data.startDate).toEqual(new Date(1977, 8, 23));
            });

            it('with invalid date parts should mark start date as bad', function () {

                mockScope.data = {
                    startDateDay: '23',
                    startDateMonth: 'bob',
                    startDateYear: '1977'
                };

                mockScope.updateStartDate();
                expect(mockScope.data.startDate).toEqual('bad date');
            });

            it('with blank date parts should clear the start date', function () {

                mockScope.data = {
                    startDateDay: '',
                    startDateMonth: '',
                    startDateYear: ''
                };

                mockScope.updateStartDate();
                expect(mockScope.data.startDate).toEqual('');
            });
        });

        describe('Calling updateEndDate', function () {
            
            beforeEach(mocks.inject(function ($controller, $rootScope) {
                    
                mockScope = $rootScope.$new();

                mockDataService = { 
                    getModel : function() {}
                };

                mockNavigationService = { 
                    logView : function() {} 
                };

                spyOn(mockDataService, 'getModel');
                spyOn(mockNavigationService, 'logView');
                
                mockValidationService = {};

                controller = $controller('leaseDatesController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    leaseDatesValidationService : mockValidationService,
                    navigationService : mockNavigationService
                });
            }));

            it('with valid date parts should update the end date', function () {

                mockScope.data = {
                    endDateDay: '23',
                    endDateMonth: '9',
                    endDateYear: '1977'
                };

                mockScope.updateEndDate();
                expect(mockScope.data.endDate).toEqual(new Date(1977, 8, 23));
            });

            it('with invalid date parts should mark end date as bad', function () {

                mockScope.data = {
                    endDateDay: '23',
                    endDateMonth: 'bob',
                    endDateYear: '1977'
                };

                mockScope.updateEndDate();
                expect(mockScope.data.endDate).toEqual('bad date');
            });

            it('with blank date parts should clear the end date', function () {

                mockScope.data = {
                    endDateDay: '',
                    endDateMonth: '',
                    endDateYear: ''
                };

                mockScope.updateEndDate();
                expect(mockScope.data.endDate).toEqual('');
            });
        });

        describe('Calling .submit() on the Lease Dates Controller with invalid data', function () {
            
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
                        return { isValid : false };
                    }
                };

                spyOn(mockDataService, 'updateModel');
                spyOn(mockNavigationService, 'next');
                spyOn(mockValidationService, 'validate').and.callThrough();
                
                controller = $controller('leaseDatesController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    leaseDatesValidationService : mockValidationService,
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

        describe('Calling .submit() on the Lease Dates Controller with valid data', function () {
            
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

                spyOn(mockDataService, 'updateModel');
                spyOn(mockNavigationService, 'next');
                spyOn(mockValidationService, 'validate').and.callThrough();
                
                controller = $controller('leaseDatesController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    leaseDatesValidationService : mockValidationService,
                    navigationService : mockNavigationService
                });

                mockScope.data = {
                    startDate: new Date(2015, 0, 1),
                    endDate: new Date(2017, 11, 30)
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
