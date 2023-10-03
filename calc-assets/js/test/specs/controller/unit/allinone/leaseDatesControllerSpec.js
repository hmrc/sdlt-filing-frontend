(function() {
    'use strict';

    require("calc-module");

    var mocks = require("angular").mock;

    describe('Lease Dates Controller', function () {
        
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
                mockScope.getHelpSetup = function() {return true;};
                
                mockDataService = { 
                    getModel : function() {}
                };

                mockNavigationService = { 
                };

                spyOn(mockDataService, 'getModel');

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
                mockScope.getHelpSetup = function() {return true;};
                
                mockDataService = { 
                    getModel : function() {}
                };

                mockNavigationService = { 
                };

                spyOn(mockDataService, 'getModel');

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

    describe('our test', function () {
            
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

                controller = $controller('leaseDatesController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    leaseDatesValidationService : mockValidationService,
                    navigationService : mockNavigationService
                });
            }));

            it('should set correct years to undefined when end date is updated', function () {

                mockScope.data = {
                    effectiveDate : new Date(2015, 2, 1),
                    startDate : new Date(2015, 2, 1),
                    endDate : new Date(2016, 7, 1),
                    year1Rent : 2000,
                    year2Rent : 2000,
                    year3Rent : 2000,
                    year4Rent : 2000,
                    year5Rent : 2000,
                    holdingType : 'Leasehold',
                    leaseTerm : 'banana'
                };

                mockScope.beforeUpdateModel();

                expect(mockScope.data.year1Rent).toEqual(2000);
                expect(mockScope.data.year2Rent).toEqual(2000);
                expect(mockScope.data.year3Rent).toEqual(undefined);
                expect(mockScope.data.year4Rent).toEqual(undefined);
                expect(mockScope.data.year5Rent).toEqual(undefined);
            });

            it('should leave all years the same when end date is not changed', function () {

                mockScope.data = {
                    effectiveDate : new Date(2015, 2, 1),
                    startDate : new Date(2015, 2, 1),
                    endDate : new Date(2019, 7, 1),
                    year1Rent : 2000,
                    year2Rent : 2001,
                    year3Rent : 2002,
                    year4Rent : 2003,
                    year5Rent : 2004,
                    holdingType : 'Leasehold',
                    leaseTerm : 'banana'
                };

                mockScope.beforeUpdateModel();

                expect(mockScope.data.year1Rent).toEqual(2000);
                expect(mockScope.data.year2Rent).toEqual(2001);
                expect(mockScope.data.year3Rent).toEqual(2002);
                expect(mockScope.data.year4Rent).toEqual(2003);
                expect(mockScope.data.year5Rent).toEqual(2004);
            });

        });
        
    });
}());
