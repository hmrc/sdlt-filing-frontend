(function() {
    'use strict';

    require("calc-module");

    var mocks = require("angular-mocks-wrapper");

    describe('Result Controller valid data with Ok server response', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockModelValidationService,
            mockDataMarshallingService,
            mockBackend,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope, $httpBackend) {
            
            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};

            mockDataService = { 
                getModel : function() { 
                    return {
                        holdingType : "Freehold",
                        propertyType : "Residential",
                        effectiveDate : new Date(2014, 11, 3)
                    }; 
                },
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

            mockDataMarshallingService = {
                constructCalculationRequest: function(data) {
                  return { requestData : "testRequest" };
                }
            };

            mockBackend = $httpBackend;
            mockBackend.whenPOST('/calculate-stamp-duty-land-tax/calculate').respond(200, {result: "Ok"});

            spyOn(mockDataMarshallingService, 'constructCalculationRequest').and.callThrough();
            spyOn(mockModelValidationService, 'validate').and.callThrough();
            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            spyOn(mockDataService, 'updateModel');
            
            controller = $controller('resultController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
                dataMarshallingService : mockDataMarshallingService
            });
            mockBackend.flush();
        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });

        it('should make 1 call to navigationService.logView', function () {
            expect(mockNavigationService.logView.calls.count()).toEqual(1);
        });

        it('should make 1 call to mockDataMarshallingService.constructCalculationRequest', function () {
            expect(mockDataMarshallingService.constructCalculationRequest.calls.count()).toEqual(1);
        });

        it('should make 1 call to mockModelValidationService.validate', function () {
            expect(mockModelValidationService.validate.calls.count()).toEqual(1);
        });

        it('should make 1 call to dataService.updateModel', function () {
            expect(mockDataService.updateModel.calls.count()).toEqual(1);
        });

        it('should show the response has been received', function () {
            expect(mockScope.responseReceived).toEqual(true);
        });

        it('should show there has been no error response', function () {
            expect(mockScope.errorResponse).toEqual(false);
        });

        it('should set the result data to be that returned from the server', function () {
            expect(mockScope.data.result).toEqual("Ok");
        });
    });

    describe('Result Controller valid data with Invalid server response', function () {

        var controller,
            mockScope,
            mockDataService,
            mockNavigationService,
            mockModelValidationService,
            mockDataMarshallingService,
            mockBackend,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope, $httpBackend) {

            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};

            mockDataService = {
                getModel : function() {
                    return {
                        holdingType : "Freehold",
                        propertyType : "Residential",
                        effectiveDate : new Date(2014, 11, 3)
                    };
                },
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

            mockDataMarshallingService = {
                constructCalculationRequest: function(data) {
                  return { requestData : "testRequest" };
                }
            };

            mockBackend = $httpBackend;
            mockBackend.whenPOST('/calculate-stamp-duty-land-tax/calculate').respond(400, {result: "Error"});

            spyOn(mockDataMarshallingService, 'constructCalculationRequest').and.callThrough();
            spyOn(mockModelValidationService, 'validate').and.callThrough();
            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            spyOn(mockDataService, 'updateModel');

            controller = $controller('resultController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
                dataMarshallingService : mockDataMarshallingService
            });
            mockBackend.flush();
        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });

        it('should make 1 call to navigationService.logView', function () {
            expect(mockNavigationService.logView.calls.count()).toEqual(1);
        });

        it('should make 1 call to mockDataMarshallingService.constructCalculationRequest', function () {
            expect(mockDataMarshallingService.constructCalculationRequest.calls.count()).toEqual(1);
        });

        it('should make 1 call to mockModelValidationService.validate', function () {
            expect(mockModelValidationService.validate.calls.count()).toEqual(1);
        });

        it('should make no call to dataService.updateModel', function () {
            expect(mockDataService.updateModel.calls.count()).toEqual(0);
        });

        it('should show the response has been received', function () {
            expect(mockScope.responseReceived).toEqual(true);
        });

        it('should show there has been an error response', function () {
            expect(mockScope.errorResponse).toEqual(true);
        });

        it('should not update the result model', function () {
            expect(mockScope.data.result).toEqual(null);
        });
    });

    describe('Result Controller with invalid data', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockModelValidationService,
            mockDataMarshallingService,
            mockLocation,
            mockBackend,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope, $location, $httpBackend) {
            
            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};
            
            mockLocation = $location;

            mockDataService = { 
                getModel : function() { 
                    return {
                        holdingType : "Freehold",
                        leaseTerm : "banana"
                    }; 
                }
            };

            mockNavigationService = { 
                logView : function() {}
            };

            mockModelValidationService = {
                validate : function() {
                    return { isModelValid : false };
                }
            };

            mockDataMarshallingService = {
                constructCalculationRequest: function(data) {
                  return { requestData : "testRequest" };
                }
            };

            mockBackend = $httpBackend;

            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            
            controller = $controller('resultController', {
                $scope : mockScope,
                $location : mockLocation,
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
                dataMarshallingService : mockDataMarshallingService
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

    describe('Call to viewDetails()', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockModelValidationService,
            mockDataMarshallingService,
            mockBackend,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope, $httpBackend) {
            
            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};
            
            mockDataService = { 
                getModel : function() { 
                    return {
                        holdingType : "Freehold",
                        propertyType : "Residential",
                        effectiveDate : new Date(2014, 0, 1)
                    }; 
                },
                updateModel : function() { }
            };

            mockNavigationService = { 
                logView : function() {},
                viewDetails : function() {}
            };

            mockModelValidationService = {
                validate : function() {
                    return { isModelValid : true };
                }
            };

            mockDataMarshallingService = {
                constructCalculationRequest: function(data) {
                  return { requestData : "testRequest" };
                }
            };

            mockBackend = $httpBackend;
            mockBackend.whenPOST('/calculate-stamp-duty-land-tax/calculate').respond(200, {result: "Ok"});

            spyOn(mockNavigationService, 'viewDetails');
            
            controller = $controller('resultController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
                dataMarshallingService : mockDataMarshallingService
            });
            mockBackend.flush();

            mockScope.viewDetails(0, 0);
        }));

        it('should make 1 call to navigationService.viewDetails', function() {
            expect(mockNavigationService.viewDetails.calls.count()).toEqual(1);
        });
    });

    describe('Call to printView()', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockModelValidationService,
            mockDataMarshallingService,
            mockBackend,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope, $httpBackend) {
            
            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};
            
            mockDataService = { 
                getModel : function() { 
                    return {
                        holdingType : "Freehold",
                        propertyType : "Residential",
                        effectiveDate : new Date(2014, 0, 1)
                    }; 
                },
                updateModel : function() { }
            };

            mockNavigationService = { 
                logView : function() {},
                printView : function() {}
            };

            mockModelValidationService = {
                validate : function() {
                    return { isModelValid : true };
                }
            };

            mockDataMarshallingService = {
                constructCalculationRequest: function(data) {
                  return { requestData : "testRequest" };
                }
            };

            mockBackend = $httpBackend;
            mockBackend.whenPOST('/calculate-stamp-duty-land-tax/calculate').respond(200, {result: "Ok"});

            spyOn(mockNavigationService, 'printView');
            
            controller = $controller('resultController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
                dataMarshallingService : mockDataMarshallingService
            });
            mockBackend.flush();

            mockScope.printView({});
        }));

        it('should make 1 call to navigationService.printView', function() {
            expect(mockNavigationService.printView.calls.count()).toEqual(1);
        });
    });
}());
