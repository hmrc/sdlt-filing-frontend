(function() {
    'use strict';

    require("calc-module");

    var mocks = require("angular-mocks-wrapper");

    describe('Result Controller - Freehold, Residential, before 4 Dec 2014', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockModelValidationService,
            mockCalculationService,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {
            
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

            mockCalculationService = {
                calcFreeResPrem_201203_201412 : function() {},
                calcFreeResPrem_201412_Undef : function() {},
                calcFreeResPremAddProp_201604_Undef : function() {},
                calcFreeNonResPrem_201203_201603 : function() {},
                calcLeaseResPremAndRent_201203_201412 : function() {},
                calcLeaseResPremAndRent_201412_Undef : function() {},
                calcLeaseResPremAndRentAddProp_201604_Undef : function() {},
                calcLeaseNonResPremAndRent_201203_Undef : function() {}
            };

            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            spyOn(mockDataService, 'updateModel');

            spyOn(mockCalculationService, 'calcFreeResPrem_201203_201412');
            spyOn(mockCalculationService, 'calcFreeResPrem_201412_Undef');
            spyOn(mockCalculationService, 'calcFreeResPremAddProp_201604_Undef');
            spyOn(mockCalculationService, 'calcFreeNonResPrem_201203_201603');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRent_201203_201412');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRent_201412_Undef');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRentAddProp_201604_Undef');
            spyOn(mockCalculationService, 'calcLeaseNonResPremAndRent_201203_Undef');
            
            controller = $controller('resultController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
                calculationService : mockCalculationService,
            });
        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });

        it('should make 1 call to navigationService.logView', function () {
            expect(mockNavigationService.logView.calls.count()).toEqual(1);
        });

        it('should make 1 call to calculationService.calcFreeResPrem_201203_201412', function () {
            expect(mockCalculationService.calcFreeResPrem_201203_201412.calls.count()).toEqual(1);
            expect(mockCalculationService.calcFreeResPrem_201412_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeResPremAddProp_201604_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeNonResPrem_201203_201603.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRent_201203_201412.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRent_201412_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRentAddProp_201604_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseNonResPremAndRent_201203_Undef.calls.count()).toEqual(0);
        });

        it('should make 1 call to dataService.updateModel', function () {
            expect(mockDataService.updateModel.calls.count()).toEqual(1);
        });
    });

    describe('Result Controller - Freehold, Residential, from 4 Dec 2014, NOT additional property rates', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockModelValidationService,
            mockCalculationService,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {
            
            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};

            mockDataService = { 
                getModel : function() { 
                    return {
                        holdingType : "Freehold",
                        propertyType : "Residential",
                        effectiveDate : new Date(2014, 11, 4)
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

            mockCalculationService = {
                calcFreeResPrem_201203_201412 : function() {},
                calcFreeResPrem_201412_Undef : function() {},
                calcFreeResPremAddProp_201604_Undef : function() {},
                calcFreeNonResPrem_201203_201603 : function() {},
                calcLeaseResPremAndRent_201203_201412 : function() {},
                calcLeaseResPremAndRent_201412_Undef : function() {},
                calcLeaseResPremAndRentAddProp_201604_Undef : function() {},
                calcLeaseNonResPremAndRent_201203_Undef : function() {}
            };

            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            spyOn(mockDataService, 'updateModel');

            spyOn(mockCalculationService, 'calcFreeResPrem_201203_201412');
            spyOn(mockCalculationService, 'calcFreeResPrem_201412_Undef');
            spyOn(mockCalculationService, 'calcFreeResPremAddProp_201604_Undef');
            spyOn(mockCalculationService, 'calcFreeNonResPrem_201203_201603');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRent_201203_201412');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRent_201412_Undef');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRentAddProp_201604_Undef');
            spyOn(mockCalculationService, 'calcLeaseNonResPremAndRent_201203_Undef');

            
            controller = $controller('resultController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
                calculationService : mockCalculationService,
            });
        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });

        it('should make 1 call to navigationService.logView', function () {
            expect(mockNavigationService.logView.calls.count()).toEqual(1);
        });

        it('should make 1 call to calculationService.calcFreeResPrem_201412_Undef', function () {
            expect(mockCalculationService.calcFreeResPrem_201203_201412.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeResPrem_201412_Undef.calls.count()).toEqual(1);
            expect(mockCalculationService.calcFreeResPremAddProp_201604_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeNonResPrem_201203_201603.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRent_201203_201412.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRent_201412_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRentAddProp_201604_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseNonResPremAndRent_201203_Undef.calls.count()).toEqual(0);
        });

        it('should make 1 call to dataService.updateModel', function () {
            expect(mockDataService.updateModel.calls.count()).toEqual(1);
        });
    });

    describe('Result Controller - Freehold, Residential, from 1 April 2016, NOT additional property rates', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockModelValidationService,
            mockCalculationService,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {
            
            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};

            mockDataService = { 
                getModel : function() { 
                    return {
                        holdingType : "Freehold",
                        propertyType : "Residential",
                        effectiveDate : new Date(2016, 3, 1)
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

            mockCalculationService = {
                calcFreeResPrem_201203_201412 : function() {},
                calcFreeResPrem_201412_Undef : function() {},
                calcFreeResPremAddProp_201604_Undef : function() {},
                calcFreeNonResPrem_201203_201603 : function() {},
                calcLeaseResPremAndRent_201203_201412 : function() {},
                calcLeaseResPremAndRent_201412_Undef : function() {},
                calcLeaseResPremAndRentAddProp_201604_Undef : function() {},
                calcLeaseNonResPremAndRent_201203_Undef : function() {}
            };

            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            spyOn(mockDataService, 'updateModel');

            spyOn(mockCalculationService, 'calcFreeResPrem_201203_201412');
            spyOn(mockCalculationService, 'calcFreeResPrem_201412_Undef');
            spyOn(mockCalculationService, 'calcFreeResPremAddProp_201604_Undef');
            spyOn(mockCalculationService, 'calcFreeNonResPrem_201203_201603');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRent_201203_201412');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRent_201412_Undef');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRentAddProp_201604_Undef');
            spyOn(mockCalculationService, 'calcLeaseNonResPremAndRent_201203_Undef');

            
            controller = $controller('resultController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
                calculationService : mockCalculationService,
            });
        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });

        it('should make 1 call to navigationService.logView', function () {
            expect(mockNavigationService.logView.calls.count()).toEqual(1);
        });

        it('should make 1 call to calculationService.calcFreeResPrem_201412_Undef', function () {
            expect(mockCalculationService.calcFreeResPrem_201203_201412.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeResPrem_201412_Undef.calls.count()).toEqual(1);
            expect(mockCalculationService.calcFreeResPremAddProp_201604_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeNonResPrem_201203_201603.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRent_201203_201412.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRent_201412_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRentAddProp_201604_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseNonResPremAndRent_201203_Undef.calls.count()).toEqual(0);
        });

        it('should make 1 call to dataService.updateModel', function () {
            expect(mockDataService.updateModel.calls.count()).toEqual(1);
        });
    });

    describe('Result Controller - Freehold, Residential, from 1 April 2016, is 2nd prop but NOT additional property rates', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockModelValidationService,
            mockCalculationService,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {
            
            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};

            mockDataService = { 
                getModel : function() { 
                    return {
                        holdingType : "Freehold",
                        propertyType : "Residential",
                        effectiveDate : new Date(2016, 3, 1),
                        twoOrMoreProperties : "Yes",
                        replaceMainResidence : "Yes"
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

            mockCalculationService = {
                calcFreeResPrem_201203_201412 : function() {},
                calcFreeResPrem_201412_Undef : function() {},
                calcFreeResPremAddProp_201604_Undef : function() {},
                calcFreeNonResPrem_201203_201603 : function() {},
                calcLeaseResPremAndRent_201203_201412 : function() {},
                calcLeaseResPremAndRent_201412_Undef : function() {},
                calcLeaseResPremAndRentAddProp_201604_Undef : function() {},
                calcLeaseNonResPremAndRent_201203_Undef : function() {}
            };

            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            spyOn(mockDataService, 'updateModel');

            spyOn(mockCalculationService, 'calcFreeResPrem_201203_201412');
            spyOn(mockCalculationService, 'calcFreeResPrem_201412_Undef');
            spyOn(mockCalculationService, 'calcFreeResPremAddProp_201604_Undef');
            spyOn(mockCalculationService, 'calcFreeNonResPrem_201203_201603');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRent_201203_201412');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRent_201412_Undef');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRentAddProp_201604_Undef');
            spyOn(mockCalculationService, 'calcLeaseNonResPremAndRent_201203_Undef');

            
            controller = $controller('resultController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
                calculationService : mockCalculationService,
            });
        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });

        it('should make 1 call to navigationService.logView', function () {
            expect(mockNavigationService.logView.calls.count()).toEqual(1);
        });

        it('should make 1 call to calculationService.calcFreeResPrem_201412_Undef', function () {
            expect(mockCalculationService.calcFreeResPrem_201203_201412.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeResPrem_201412_Undef.calls.count()).toEqual(1);
            expect(mockCalculationService.calcFreeResPremAddProp_201604_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeNonResPrem_201203_201603.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRent_201203_201412.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRent_201412_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRentAddProp_201604_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseNonResPremAndRent_201203_Undef.calls.count()).toEqual(0);
        });

        it('should make 1 call to dataService.updateModel', function () {
            expect(mockDataService.updateModel.calls.count()).toEqual(1);
        });
    });

    describe('Result Controller - Freehold, Residential, from 1 April 2016, IS additional property rates', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockModelValidationService,
            mockCalculationService,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {
            
            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};

            mockDataService = { 
                getModel : function() { 
                    return {
                        holdingType : "Freehold",
                        propertyType : "Residential",
                        effectiveDate : new Date(2016, 3, 1),
                        twoOrMoreProperties : "Yes",
                        replaceMainResidence : "No"
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

            mockCalculationService = {
                calcFreeResPrem_201203_201412 : function() {},
                calcFreeResPrem_201412_Undef : function() {},
                calcFreeResPremAddProp_201604_Undef : function() {},
                calcFreeNonResPrem_201203_201603 : function() {},
                calcLeaseResPremAndRent_201203_201412 : function() {},
                calcLeaseResPremAndRent_201412_Undef : function() {},
                calcLeaseResPremAndRentAddProp_201604_Undef : function() {},
                calcLeaseNonResPremAndRent_201203_Undef : function() {}
            };

            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            spyOn(mockDataService, 'updateModel');

            spyOn(mockCalculationService, 'calcFreeResPrem_201203_201412');
            spyOn(mockCalculationService, 'calcFreeResPrem_201412_Undef');
            spyOn(mockCalculationService, 'calcFreeResPremAddProp_201604_Undef');
            spyOn(mockCalculationService, 'calcFreeNonResPrem_201203_201603');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRent_201203_201412');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRent_201412_Undef');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRentAddProp_201604_Undef');
            spyOn(mockCalculationService, 'calcLeaseNonResPremAndRent_201203_Undef');

            
            controller = $controller('resultController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
                calculationService : mockCalculationService,
            });
        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });

        it('should make 1 call to navigationService.logView', function () {
            expect(mockNavigationService.logView.calls.count()).toEqual(1);
        });

        it('should make 1 call to calculationService.calcFreeResPremAddProp_201604_Undef', function () {
            expect(mockCalculationService.calcFreeResPrem_201203_201412.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeResPrem_201412_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeResPremAddProp_201604_Undef.calls.count()).toEqual(1);
            expect(mockCalculationService.calcFreeNonResPrem_201203_201603.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRent_201203_201412.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRent_201412_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRentAddProp_201604_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseNonResPremAndRent_201203_Undef.calls.count()).toEqual(0);
        });

        it('should make 1 call to dataService.updateModel', function () {
            expect(mockDataService.updateModel.calls.count()).toEqual(1);
        });
    });

    describe('Result Controller - Freehold, Non-Residential', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockModelValidationService,
            mockCalculationService,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {
            
            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};

            mockDataService = { 
                getModel : function() { 
                    return {
                        holdingType : "Freehold",
                        propertyType : "Non-residential",
                        effectiveDate : new Date(2016, 2, 31)
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

            mockCalculationService = {
                calcFreeResPrem_201203_201412 : function() {},
                calcFreeResPrem_201412_Undef : function() {},
                calcFreeResPremAddProp_201604_Undef : function() {},
                calcFreeNonResPrem_201203_201603 : function() {},
                calcLeaseResPremAndRent_201203_201412 : function() {},
                calcLeaseResPremAndRent_201412_Undef : function() {},
                calcLeaseResPremAndRentAddProp_201604_Undef : function() {},
                calcLeaseNonResPremAndRent_201203_Undef : function() {}
            };

            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            spyOn(mockDataService, 'updateModel');

            spyOn(mockCalculationService, 'calcFreeResPrem_201203_201412');
            spyOn(mockCalculationService, 'calcFreeResPrem_201412_Undef');
            spyOn(mockCalculationService, 'calcFreeResPremAddProp_201604_Undef');
            spyOn(mockCalculationService, 'calcFreeNonResPrem_201203_201603');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRent_201203_201412');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRent_201412_Undef');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRentAddProp_201604_Undef');
            spyOn(mockCalculationService, 'calcLeaseNonResPremAndRent_201203_Undef');

            
            controller = $controller('resultController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
                calculationService : mockCalculationService,
            });
        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });

        it('should make 1 call to navigationService.logView', function () {
            expect(mockNavigationService.logView.calls.count()).toEqual(1);
        });

        it('should make 1 call to calculationService.calcFreeNonResPrem_201203_201603', function () {
            expect(mockCalculationService.calcFreeResPrem_201203_201412.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeResPrem_201412_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeResPremAddProp_201604_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeNonResPrem_201203_201603.calls.count()).toEqual(1);
            expect(mockCalculationService.calcLeaseResPremAndRent_201203_201412.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRent_201412_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRentAddProp_201604_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseNonResPremAndRent_201203_Undef.calls.count()).toEqual(0);
        });

        it('should make 1 call to dataService.updateModel', function () {
            expect(mockDataService.updateModel.calls.count()).toEqual(1);
        });
    });

    describe('Result Controller - Leasehold, Residential, before 4 Dec 2014', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockModelValidationService,
            mockCalculationService,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {
            
            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};

            mockDataService = { 
                getModel : function() { 
                    return {
                        holdingType : "Leasehold",
                        propertyType : "Residential",
                        effectiveDate : new Date(2014, 11, 3),
                        startDate : new Date(2014, 0, 1),
                        endDate : new Date(2014, 11, 31),
                         leaseTerm : {
                             years : 1,
                             days : 0,
                             daysInPartialYear : 0
                         },
                         year1Rent : 1000,
                         premium : 100000
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

            mockCalculationService = {
                calcFreeResPrem_201203_201412 : function() {},
                calcFreeResPrem_201412_Undef : function() {},
                calcFreeResPremAddProp_201604_Undef : function() {},
                calcFreeNonResPrem_201203_201603 : function() {},
                calcLeaseResPremAndRent_201203_201412 : function() {},
                calcLeaseResPremAndRent_201412_Undef : function() {},
                calcLeaseResPremAndRentAddProp_201604_Undef : function() {},
                calcLeaseNonResPremAndRent_201203_Undef : function() {},
                calculateNPV: function() {
                    return 1;
                }

            };

            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            spyOn(mockDataService, 'updateModel');

            spyOn(mockCalculationService, 'calcFreeResPrem_201203_201412');
            spyOn(mockCalculationService, 'calcFreeResPrem_201412_Undef');
            spyOn(mockCalculationService, 'calcFreeResPremAddProp_201604_Undef');
            spyOn(mockCalculationService, 'calcFreeNonResPrem_201203_201603');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRent_201203_201412');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRent_201412_Undef');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRentAddProp_201604_Undef');
            spyOn(mockCalculationService, 'calcLeaseNonResPremAndRent_201203_Undef');
            spyOn(mockCalculationService, 'calculateNPV');

            
            controller = $controller('resultController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
                calculationService : mockCalculationService,
            });
        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });

        it('should make 1 call to navigationService.logView', function () {
            expect(mockNavigationService.logView.calls.count()).toEqual(1);
        });

        it('should make 1 call to calculationService.calculateNPV', function () {
            expect(mockCalculationService.calculateNPV.calls.count()).toEqual(1);
        });

        it('should make 1 call to calculationService.calcLeaseResPremAndRent_201203_201412', function () {
            expect(mockCalculationService.calcFreeResPrem_201203_201412.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeResPrem_201412_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeResPremAddProp_201604_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeNonResPrem_201203_201603.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRent_201203_201412.calls.count()).toEqual(1);
            expect(mockCalculationService.calcLeaseResPremAndRent_201412_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRentAddProp_201604_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseNonResPremAndRent_201203_Undef.calls.count()).toEqual(0);
        });

        it('should make 1 call to dataService.updateModel', function () {
            expect(mockDataService.updateModel.calls.count()).toEqual(1);
        });
    });

    describe('Result Controller - Leasehold, Residential, from 4 Dec 2014', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockModelValidationService,
            mockCalculationService,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {
            
            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};

            mockDataService = { 
                getModel : function() { 
                    return {
                        holdingType : "Leasehold",
                        propertyType : "Residential",
                        effectiveDate : new Date(2014, 11, 4),
                        startDate : new Date(2014, 0, 1),
                        endDate : new Date(2018, 11, 31),
                         leaseTerm : {
                             years : 5,
                             days : 0,
                             daysInPartialYear : 0
                         },
                         year1Rent : 1000,
                         year2Rent : 1000,
                         year3Rent : 1000,
                         year4Rent : 1000,
                         year5Rent : 1000,
                         premium : 100000
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

            mockCalculationService = {
                calcFreeResPrem_201203_201412 : function() {},
                calcFreeResPrem_201412_Undef : function() {},
                calcFreeResPremAddProp_201604_Undef : function() {},
                calcFreeNonResPrem_201203_201603 : function() {},
                calcLeaseResPremAndRent_201203_201412 : function() {},
                calcLeaseResPremAndRent_201412_Undef : function() {},
                calcLeaseResPremAndRentAddProp_201604_Undef : function() {},
                calcLeaseNonResPremAndRent_201203_Undef : function() {},
                calculateNPV: function() {
                    return 1;
                }

            };

            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            spyOn(mockDataService, 'updateModel');

            spyOn(mockCalculationService, 'calcFreeResPrem_201203_201412');
            spyOn(mockCalculationService, 'calcFreeResPrem_201412_Undef');
            spyOn(mockCalculationService, 'calcFreeResPremAddProp_201604_Undef');
            spyOn(mockCalculationService, 'calcFreeNonResPrem_201203_201603');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRent_201203_201412');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRent_201412_Undef');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRentAddProp_201604_Undef');
            spyOn(mockCalculationService, 'calcLeaseNonResPremAndRent_201203_Undef');
            spyOn(mockCalculationService, 'calculateNPV');

            
            controller = $controller('resultController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
                calculationService : mockCalculationService,
            });
        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });

        it('should make 1 call to navigationService.logView', function () {
            expect(mockNavigationService.logView.calls.count()).toEqual(1);
        });

        it('should make 1 call to calculationService.calculateNPV', function () {
            expect(mockCalculationService.calculateNPV.calls.count()).toEqual(1);
        });

        it('should make 1 call to calculationService.calcLeaseResPremAndRent_201412_Undef', function () {
            expect(mockCalculationService.calcFreeResPrem_201203_201412.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeResPrem_201412_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeResPremAddProp_201604_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeNonResPrem_201203_201603.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRent_201203_201412.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRent_201412_Undef.calls.count()).toEqual(1);
            expect(mockCalculationService.calcLeaseResPremAndRentAddProp_201604_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseNonResPremAndRent_201203_Undef.calls.count()).toEqual(0);
        });

        it('should make 1 call to dataService.updateModel', function () {
            expect(mockDataService.updateModel.calls.count()).toEqual(1);
        });
    });

    describe('Result Controller - Leasehold, Residential, from 1 April 2016 but NOT additional property rates', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockModelValidationService,
            mockCalculationService,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {
            
            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};

            mockDataService = { 
                getModel : function() { 
                    return {
                        holdingType : "Leasehold",
                        propertyType : "Residential",
                        effectiveDate : new Date(2016, 3, 1),
                        twoOrMoreProperties : "No",
                        startDate : new Date(2014, 0, 1),
                        endDate : new Date(2018, 11, 31),
                         leaseTerm : {
                             years : 5,
                             days : 0,
                             daysInPartialYear : 0
                         },
                         year1Rent : 1000,
                         year2Rent : 1000,
                         year3Rent : 1000,
                         year4Rent : 1000,
                         year5Rent : 1000,
                         premium : 100000
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

            mockCalculationService = {
                calcFreeResPrem_201203_201412 : function() {},
                calcFreeResPrem_201412_Undef : function() {},
                calcFreeResPremAddProp_201604_Undef : function() {},
                calcFreeNonResPrem_201203_201603 : function() {},
                calcLeaseResPremAndRent_201203_201412 : function() {},
                calcLeaseResPremAndRent_201412_Undef : function() {},
                calcLeaseResPremAndRentAddProp_201604_Undef : function() {},
                calcLeaseNonResPremAndRent_201203_Undef : function() {},
                calculateNPV: function() {
                    return 1;
                }

            };

            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            spyOn(mockDataService, 'updateModel');

            spyOn(mockCalculationService, 'calcFreeResPrem_201203_201412');
            spyOn(mockCalculationService, 'calcFreeResPrem_201412_Undef');
            spyOn(mockCalculationService, 'calcFreeResPremAddProp_201604_Undef');
            spyOn(mockCalculationService, 'calcFreeNonResPrem_201203_201603');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRent_201203_201412');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRent_201412_Undef');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRentAddProp_201604_Undef');
            spyOn(mockCalculationService, 'calcLeaseNonResPremAndRent_201203_Undef');
            spyOn(mockCalculationService, 'calculateNPV');

            
            controller = $controller('resultController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
                calculationService : mockCalculationService,
            });
        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });

        it('should make 1 call to navigationService.logView', function () {
            expect(mockNavigationService.logView.calls.count()).toEqual(1);
        });

        it('should make 1 call to calculationService.calculateNPV', function () {
            expect(mockCalculationService.calculateNPV.calls.count()).toEqual(1);
        });

        it('should make 1 call to calculationService.calcLeaseResPremAndRent_201412_Undef', function () {
            expect(mockCalculationService.calcFreeResPrem_201203_201412.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeResPrem_201412_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeResPremAddProp_201604_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeNonResPrem_201203_201603.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRent_201203_201412.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRent_201412_Undef.calls.count()).toEqual(1);
            expect(mockCalculationService.calcLeaseResPremAndRentAddProp_201604_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseNonResPremAndRent_201203_Undef.calls.count()).toEqual(0);
        });

        it('should make 1 call to dataService.updateModel', function () {
            expect(mockDataService.updateModel.calls.count()).toEqual(1);
        });
    });

    describe('Result Controller - Leasehold, Residential, from 1 April 2016, 2nd prop but NOT additional property rates', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockModelValidationService,
            mockCalculationService,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {
            
            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};

            mockDataService = { 
                getModel : function() { 
                    return {
                        holdingType : "Leasehold",
                        propertyType : "Residential",
                        effectiveDate : new Date(2016, 3, 1),
                        twoOrMoreProperties : "Yes",
                        replaceMainResidence : "Yes",
                        startDate : new Date(2014, 0, 1),
                        endDate : new Date(2018, 11, 31),
                         leaseTerm : {
                             years : 5,
                             days : 0,
                             daysInPartialYear : 0
                         },
                         year1Rent : 1000,
                         year2Rent : 1000,
                         year3Rent : 1000,
                         year4Rent : 1000,
                         year5Rent : 1000,
                         premium : 100000
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

            mockCalculationService = {
                calcFreeResPrem_201203_201412 : function() {},
                calcFreeResPrem_201412_Undef : function() {},
                calcFreeResPremAddProp_201604_Undef : function() {},
                calcFreeNonResPrem_201203_201603 : function() {},
                calcLeaseResPremAndRent_201203_201412 : function() {},
                calcLeaseResPremAndRent_201412_Undef : function() {},
                calcLeaseResPremAndRentAddProp_201604_Undef : function() {},
                calcLeaseNonResPremAndRent_201203_Undef : function() {},
                calculateNPV: function() {
                    return 1;
                }

            };

            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            spyOn(mockDataService, 'updateModel');

            spyOn(mockCalculationService, 'calcFreeResPrem_201203_201412');
            spyOn(mockCalculationService, 'calcFreeResPrem_201412_Undef');
            spyOn(mockCalculationService, 'calcFreeResPremAddProp_201604_Undef');
            spyOn(mockCalculationService, 'calcFreeNonResPrem_201203_201603');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRent_201203_201412');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRent_201412_Undef');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRentAddProp_201604_Undef');
            spyOn(mockCalculationService, 'calcLeaseNonResPremAndRent_201203_Undef');
            spyOn(mockCalculationService, 'calculateNPV');

            
            controller = $controller('resultController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
                calculationService : mockCalculationService,
            });
        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });

        it('should make 1 call to navigationService.logView', function () {
            expect(mockNavigationService.logView.calls.count()).toEqual(1);
        });

        it('should make 1 call to calculationService.calculateNPV', function () {
            expect(mockCalculationService.calculateNPV.calls.count()).toEqual(1);
        });

        it('should make 1 call to calculationService.calcLeaseResPremAndRent_201412_Undef', function () {
            expect(mockCalculationService.calcFreeResPrem_201203_201412.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeResPrem_201412_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeResPremAddProp_201604_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeNonResPrem_201203_201603.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRent_201203_201412.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRent_201412_Undef.calls.count()).toEqual(1);
            expect(mockCalculationService.calcLeaseResPremAndRentAddProp_201604_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseNonResPremAndRent_201203_Undef.calls.count()).toEqual(0);
        });

        it('should make 1 call to dataService.updateModel', function () {
            expect(mockDataService.updateModel.calls.count()).toEqual(1);
        });
    });

    describe('Result Controller - Leasehold, Residential, from 1 April 2016, IS additional property rates', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockModelValidationService,
            mockCalculationService,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {
            
            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};

            mockDataService = { 
                getModel : function() { 
                    return {
                        holdingType : "Leasehold",
                        propertyType : "Residential",
                        effectiveDate : new Date(2016, 3, 1),
                        twoOrMoreProperties : "Yes",
                        replaceMainResidence : "No",
                        startDate : new Date(2014, 0, 1),
                        endDate : new Date(2018, 11, 31),
                         leaseTerm : {
                             years : 5,
                             days : 0,
                             daysInPartialYear : 0
                         },
                         year1Rent : 1000,
                         year2Rent : 1000,
                         year3Rent : 1000,
                         year4Rent : 1000,
                         year5Rent : 1000,
                         premium : 100000
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

            mockCalculationService = {
                calcFreeResPrem_201203_201412 : function() {},
                calcFreeResPrem_201412_Undef : function() {},
                calcFreeResPremAddProp_201604_Undef : function() {},
                calcFreeNonResPrem_201203_201603 : function() {},
                calcLeaseResPremAndRent_201203_201412 : function() {},
                calcLeaseResPremAndRent_201412_Undef : function() {},
                calcLeaseResPremAndRentAddProp_201604_Undef : function() {},
                calcLeaseNonResPremAndRent_201203_Undef : function() {},
                calculateNPV: function() {
                    return 1;
                }

            };

            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            spyOn(mockDataService, 'updateModel');

            spyOn(mockCalculationService, 'calcFreeResPrem_201203_201412');
            spyOn(mockCalculationService, 'calcFreeResPrem_201412_Undef');
            spyOn(mockCalculationService, 'calcFreeResPremAddProp_201604_Undef');
            spyOn(mockCalculationService, 'calcFreeNonResPrem_201203_201603');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRent_201203_201412');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRent_201412_Undef');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRentAddProp_201604_Undef');
            spyOn(mockCalculationService, 'calcLeaseNonResPremAndRent_201203_Undef');
            spyOn(mockCalculationService, 'calculateNPV');

            
            controller = $controller('resultController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
                calculationService : mockCalculationService,
            });
        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });

        it('should make 1 call to navigationService.logView', function () {
            expect(mockNavigationService.logView.calls.count()).toEqual(1);
        });

        it('should make 1 call to calculationService.calculateNPV', function () {
            expect(mockCalculationService.calculateNPV.calls.count()).toEqual(1);
        });

        it('should make 1 call to calculationService.calcLeaseResPremAndRentAddProp_201604_Undef', function () {
            expect(mockCalculationService.calcFreeResPrem_201203_201412.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeResPrem_201412_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeResPremAddProp_201604_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeNonResPrem_201203_201603.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRent_201203_201412.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRent_201412_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRentAddProp_201604_Undef.calls.count()).toEqual(1);
            expect(mockCalculationService.calcLeaseNonResPremAndRent_201203_Undef.calls.count()).toEqual(0);
        });

        it('should make 1 call to dataService.updateModel', function () {
            expect(mockDataService.updateModel.calls.count()).toEqual(1);
        });
    });

    describe('Result Controller - Leasehold, Non-residential, npv < 150k, RR < 1K', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockModelValidationService,
            mockCalculationService,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {
            
            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};

            mockDataService = { 
                getModel : function() { 
                    return {
                        holdingType : "Leasehold",
                        propertyType : "Non-residential",
                        effectiveDate : new Date(2016, 2, 31),
                        startDate : new Date(2014, 0, 1),
                        endDate : new Date(2018, 11, 31),
                         leaseTerm : {
                             years : 5,
                             days : 0,
                             daysInPartialYear : 0
                         },
                         year1Rent : 1000,
                         year2Rent : 1000,
                         year3Rent : 1000,
                         year4Rent : 1000,
                         year5Rent : 1000,
                         premium : 100000,
                         relevantRent : 999
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

            mockCalculationService = {
                calcFreeResPrem_201203_201412 : function() {},
                calcFreeResPrem_201412_Undef : function() {},
                calcFreeResPremAddProp_201604_Undef : function() {},
                calcFreeNonResPrem_201203_201603 : function() {},
                calcLeaseResPremAndRent_201203_201412 : function() {},
                calcLeaseResPremAndRent_201412_Undef : function() {},
                calcLeaseResPremAndRentAddProp_201604_Undef : function() {},
                calcLeaseNonResPremAndRent_201203_Undef : function() {},
                calculateNPV: function() {
                    return 1;
                }

            };

            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            spyOn(mockDataService, 'updateModel');

            spyOn(mockCalculationService, 'calcFreeResPrem_201203_201412');
            spyOn(mockCalculationService, 'calcFreeResPrem_201412_Undef');
            spyOn(mockCalculationService, 'calcFreeResPremAddProp_201604_Undef');
            spyOn(mockCalculationService, 'calcFreeNonResPrem_201203_201603');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRent_201203_201412');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRent_201412_Undef');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRentAddProp_201604_Undef');
            spyOn(mockCalculationService, 'calcLeaseNonResPremAndRent_201203_Undef');
            spyOn(mockCalculationService, 'calculateNPV');

            
            controller = $controller('resultController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
                calculationService : mockCalculationService,
            });
        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });

        it('should make 1 call to navigationService.logView', function () {
            expect(mockNavigationService.logView.calls.count()).toEqual(1);
        });

        it('should make 1 call to calculationService.calculateNPV', function () {
            expect(mockCalculationService.calculateNPV.calls.count()).toEqual(1);
        });

        it('should make 1 call to calculationService.calcLeaseNonResPremAndRent_201203_Undef', function () {
            expect(mockCalculationService.calcFreeResPrem_201203_201412.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeResPrem_201412_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeResPremAddProp_201604_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeNonResPrem_201203_201603.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRent_201203_201412.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRent_201412_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRentAddProp_201604_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseNonResPremAndRent_201203_Undef.calls.count()).toEqual(1);
        });

        it('should make 1 call to dataService.updateModel', function () {
            expect(mockDataService.updateModel.calls.count()).toEqual(1);
        });
    });

    describe('Result Controller - Leasehold, Non-residential, npv < 150k, RR > 1K', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockModelValidationService,
            mockCalculationService,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {
            
            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};

            mockDataService = { 
                getModel : function() { 
                    return {
                        holdingType : "Leasehold",
                        propertyType : "Non-residential",
                        effectiveDate : new Date(2016, 2, 31),
                        startDate : new Date(2014, 0, 1),
                        endDate : new Date(2018, 11, 31),
                         leaseTerm : {
                             years : 5,
                             days : 0,
                             daysInPartialYear : 0
                         },
                         year1Rent : 3000,
                         year2Rent : 1000,
                         year3Rent : 1000,
                         year4Rent : 1000,
                         year5Rent : 1000,
                         premium : 100000,
                         relevantRent : 1001
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

            mockCalculationService = {
                calcFreeResPrem_201203_201412 : function() {},
                calcFreeResPrem_201412_Undef : function() {},
                calcFreeResPremAddProp_201604_Undef : function() {},
                calcFreeNonResPrem_201203_201603 : function() {},
                calcLeaseResPremAndRent_201203_201412 : function() {},
                calcLeaseResPremAndRent_201412_Undef : function() {},
                calcLeaseResPremAndRentAddProp_201604_Undef : function() {},
                calcLeaseNonResPremAndRent_201203_Undef : function() {},
                calculateNPV: function() {
                    return 1;
                }

            };

            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            spyOn(mockDataService, 'updateModel');

            spyOn(mockCalculationService, 'calcFreeResPrem_201203_201412');
            spyOn(mockCalculationService, 'calcFreeResPrem_201412_Undef');
            spyOn(mockCalculationService, 'calcFreeResPremAddProp_201604_Undef');
            spyOn(mockCalculationService, 'calcFreeNonResPrem_201203_201603');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRent_201203_201412');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRent_201412_Undef');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRentAddProp_201604_Undef');
            spyOn(mockCalculationService, 'calcLeaseNonResPremAndRent_201203_Undef');
            spyOn(mockCalculationService, 'calculateNPV');

            
            controller = $controller('resultController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
                calculationService : mockCalculationService,
            });
        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });

        it('should make 1 call to navigationService.logView', function () {
            expect(mockNavigationService.logView.calls.count()).toEqual(1);
        });

        it('should make 1 call to calculationService.calculateNPV', function () {
            expect(mockCalculationService.calculateNPV.calls.count()).toEqual(1);
        });

        it('should make 1 call to calculationService.calcLeaseNonResPremAndRent_201203_Undef', function () {
            expect(mockCalculationService.calcFreeResPrem_201203_201412.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeResPrem_201412_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeResPremAddProp_201604_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeNonResPrem_201203_201603.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRent_201203_201412.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRent_201412_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRentAddProp_201604_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseNonResPremAndRent_201203_Undef.calls.count()).toEqual(1);
        });

        it('should make 1 call to dataService.updateModel', function () {
            expect(mockDataService.updateModel.calls.count()).toEqual(1);
        });
    });

    describe('Result Controller - Leasehold, Non-residential, npv < 150k, RR < 1K, one rent > 2000', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockModelValidationService,
            mockCalculationService,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {
            
            mockScope = $rootScope.$new();
            mockScope.getHelpSetup = function() {return true;};

            mockDataService = { 
                getModel : function() { 
                    return {
                        holdingType : "Leasehold",
                        propertyType : "Non-residential",
                        effectiveDate : new Date(2016, 2, 31),
                        startDate : new Date(2014, 0, 1),
                        endDate : new Date(2018, 11, 31),
                         leaseTerm : {
                             years : 5,
                             days : 0,
                             daysInPartialYear : 0
                         },
                         year1Rent : 3000,
                         year2Rent : 1000,
                         year3Rent : 1000,
                         year4Rent : 1000,
                         year5Rent : 1000,
                         premium : 100000,
                         relevantRent : 999
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

            mockCalculationService = {
                calcFreeResPrem_201203_201412 : function() {},
                calcFreeResPrem_201412_Undef : function() {},
                calcFreeResPremAddProp_201604_Undef : function() {},
                calcFreeNonResPrem_201203_201603 : function() {},
                calcLeaseResPremAndRent_201203_201412 : function() {},
                calcLeaseResPremAndRent_201412_Undef : function() {},
                calcLeaseResPremAndRentAddProp_201604_Undef : function() {},
                calcLeaseNonResPremAndRent_201203_Undef : function() {},
                calculateNPV: function() {
                    return 1;
                }

            };

            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            spyOn(mockDataService, 'updateModel');

            spyOn(mockCalculationService, 'calcFreeResPrem_201203_201412');
            spyOn(mockCalculationService, 'calcFreeResPrem_201412_Undef');
            spyOn(mockCalculationService, 'calcFreeResPremAddProp_201604_Undef');
            spyOn(mockCalculationService, 'calcFreeNonResPrem_201203_201603');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRent_201203_201412');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRent_201412_Undef');
            spyOn(mockCalculationService, 'calcLeaseResPremAndRentAddProp_201604_Undef');
            spyOn(mockCalculationService, 'calcLeaseNonResPremAndRent_201203_Undef');
            spyOn(mockCalculationService, 'calculateNPV');

            
            controller = $controller('resultController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
                calculationService : mockCalculationService,
            });
        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });

        it('should make 1 call to navigationService.logView', function () {
            expect(mockNavigationService.logView.calls.count()).toEqual(1);
        });

        it('should make 1 call to calculationService.calculateNPV', function () {
            expect(mockCalculationService.calculateNPV.calls.count()).toEqual(1);
        });

        it('should make 1 call to calculationService.calcLeaseNonResPremAndRent_201203_Undef', function () {
            expect(mockCalculationService.calcFreeResPrem_201203_201412.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeResPrem_201412_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeResPremAddProp_201604_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcFreeNonResPrem_201203_201603.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRent_201203_201412.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRent_201412_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseResPremAndRentAddProp_201604_Undef.calls.count()).toEqual(0);
            expect(mockCalculationService.calcLeaseNonResPremAndRent_201203_Undef.calls.count()).toEqual(1);
        });

        it('should make 1 call to dataService.updateModel', function () {
            expect(mockDataService.updateModel.calls.count()).toEqual(1);
        });
    });


    describe('Result Controller with invalid data', function () {
        
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

            mockCalculationService = {
                calculateResidentialPremiumSlab: function() {}
            };

            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            spyOn(mockCalculationService, 'calculateResidentialPremiumSlab');
            
            controller = $controller('resultController', {
                $scope : mockScope,
                $location : mockLocation,
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
                calculationService : mockCalculationService,
            });
        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });

        it('should make 1 call to navigationService.logView', function () {
            expect(mockNavigationService.logView.calls.count()).toEqual(1);
        });

        it('should make 0 calls to calculationService.calculateResidentialPremiumSlab', function () {
            expect(mockCalculationService.calculateResidentialPremiumSlab.calls.count()).toEqual(0);
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
            mockCalculationService,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {
            
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

            mockCalculationService = {
                calcFreeResPrem_201203_201412: function() {}
            };
            
            spyOn(mockNavigationService, 'viewDetails');
            
            controller = $controller('resultController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
                calculationService : mockCalculationService,
            });

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
            mockCalculationService,
            calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {
            
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

            mockCalculationService = {
                calcFreeResPrem_201203_201412: function() {}
            };

            spyOn(mockNavigationService, 'printView');
            
            controller = $controller('resultController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService,
                calculationService : mockCalculationService,
            });

            mockScope.printView({});
        }));

        it('should make 1 call to navigationService.printView', function() {
            expect(mockNavigationService.printView.calls.count()).toEqual(1);
        });
    });
}());
