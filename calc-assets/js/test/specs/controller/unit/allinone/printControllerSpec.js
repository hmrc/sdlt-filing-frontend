(function() {
    'use strict';

    require("calc-module");

    var mocks = require("angular-mocks-wrapper");

    describe('Print Controller with valid data', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockModelValidationService,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {
            
            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};
            
            mockDataService = { 
                getModel : function() { return {}; }
            };

            mockNavigationService = { 
                logView : function() {} 
            };

            mockModelValidationService = {
                validate : function() {
                    return { isModelValid : true };
                }
            };


            spyOn(mockDataService, 'getModel').and.callThrough();
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

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope, $location) {
            
            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};
            
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

    describe('calling effDateAfterCutoff and getHeading with date before cut-off date', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockModelValidationService,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {
            
            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};
            
            mockDataService = { 
                getModel : function() { return {
                    effectiveDate : new Date("December 3, 2014")
                }; }
            };

            mockNavigationService = { 
                logView : function() {} 
            };

            mockModelValidationService = {
                validate : function() {
                    return { isModelValid : true };
                }
            };


            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
                        
            controller = $controller('printController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
            });
        }));

        it('should return false for effectiveDateAfterCutOff() if Effective Date is 03/12/2014', function () {
            expect(mockScope.effDateAfterCutOff()).toEqual(false);
        });

        it('should return correct text for getHeading() if Effective Date is 03/12/2014', function () {
            expect(mockScope.getHeading()).toEqual("Result");
        });

    });

    describe('calling effDateAfterCutoff and getHeading with date on cut-off date', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockModelValidationService,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {
            
            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};
            
            mockDataService = { 
                getModel : function() { return {
                    effectiveDate : new Date("December 4, 2014")
                }; }
            };

            mockNavigationService = { 
                logView : function() {} 
            };

            mockModelValidationService = {
                validate : function() {
                    return { isModelValid : true };
                }
            };


            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
                        
            controller = $controller('printController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
            });
        }));

        it('should return true for effectiveDateAfterCutOff() if Effective Date is 04/12/2014', function () {
            expect(mockScope.effDateAfterCutOff()).toEqual(true);
        });

        it('should return correct text for getHeading() if Effective Date is 04/12/2014', function () {
            expect(mockScope.getHeading()).toEqual("Results based on SDLT rules before 4 December 2014");
        });

    });


    describe('calling effDateAfterCutoff with date on April cut-off date', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockModelValidationService,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {
            
            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};
            
            mockDataService = { 
                getModel : function() { return {
                    effectiveDate : new Date("April 1, 2016"),
                    twoOrMoreProperties : "Yes",
                    replaceMainResidence : "No"
                }; }
            };

            mockNavigationService = { 
                logView : function() {} 
            };

            mockModelValidationService = {
                validate : function() {
                    return { isModelValid : true };
                }
            };


            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
                        
            controller = $controller('printController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
            });
        }));

        it('should return true for effectiveDateAfterCutOff() if Effective Date is 01/04/2016', function () {
            expect(mockScope.effDateAfterAprilCutOff()).toEqual(true);
        });

        it('should return true for isAdditionalProperty() if Yes No', function() {
            expect(mockScope.isAdditionalProperty()).toEqual(true);
        });

    });


    describe('calling effDateAfterCutoff and getHeading with date after cut-off date', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockModelValidationService,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {
            
            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};
            
            mockDataService = { 
                getModel : function() { return {
                    effectiveDate : new Date("December 6, 2014")
                }; }
            };

            mockNavigationService = { 
                logView : function() {} 
            };

            mockModelValidationService = {
                validate : function() {
                    return { isModelValid : true };
                }
            };


            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
                        
            controller = $controller('printController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
            });
        }));

        it('should return true for effectiveDateAfterCutOff() if Effective Date is 06/12/2014', function () {
            expect(mockScope.effDateAfterCutOff()).toEqual(true);
        });

        it('should return correct text for getHeading() if Effective Date is 06/12/2014', function () {
            expect(mockScope.getHeading()).toEqual("Results based on SDLT rules before 4 December 2014");
        });

    });
}());
