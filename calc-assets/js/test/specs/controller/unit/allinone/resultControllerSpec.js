(function() {
    'use strict';

    require("calc-module");

    var mocks = require("angular-mocks-wrapper");

    describe('Result Controller with valid data', function () {
        
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

            mockDataService = { 
                getModel : function() { 
                    return {
                        holdingType : "Freehold",
                        leaseTerm : "banana"
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
                calculateResidentialPremiumSlice: function() {},
                calculateResidentialPremiumSlab: function() {},
                calculateNonResidentialPremiumSlab: function() {},
                calculateResidentialLeaseSlice: function() {},
                calculateNonResidentialLeaseSlice: function() {}
            };

            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            spyOn(mockDataService, 'updateModel');
            
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
        
        it('should make 1 call to dataService.updateModel', function () {
            expect(mockDataService.updateModel.calls.count()).toEqual(1);
        });
    });

    describe('Result Controller with valid data - Freehold and Residential', function () {
        
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

            mockDataService = { 
                getModel : function() { 
                    return {
                        holdingType : "Freehold",
                        propertyType : "Residential"
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
                calculateResidentialPremiumSlice: function() {},
                calculateResidentialPremiumSlab: function() {}
            };

            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            spyOn(mockCalculationService, 'calculateResidentialPremiumSlice');
            spyOn(mockCalculationService, 'calculateResidentialPremiumSlab');
            spyOn(mockDataService, 'updateModel');
            
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

        it('should make 1 call to calculationService.calculateResidentialPremiumSlice', function () {
            expect(mockCalculationService.calculateResidentialPremiumSlice.calls.count()).toEqual(1);
        });

        it('should make 1 call to calculationService.calculateResidentialPremiumSlab', function () {
            expect(mockCalculationService.calculateResidentialPremiumSlab.calls.count()).toEqual(1);
        });
        
        it('should make 1 call to dataService.updateModel', function () {
            expect(mockDataService.updateModel.calls.count()).toEqual(1);
        });
    });

    describe('Result Controller with valid data - Freehold and Non-residential', function () {
        
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

            mockDataService = { 
                getModel : function() { 
                    return {
                        holdingType : "Freehold",
                        propertyType : "Non-residential"
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
                calculateNonResidentialPremiumSlab: function() {}
            };

            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            spyOn(mockCalculationService, 'calculateNonResidentialPremiumSlab');
            spyOn(mockDataService, 'updateModel');
            
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

        it('should make 1 call to calculationService.calculateNonResidentialPremiumSlab', function () {
            expect(mockCalculationService.calculateNonResidentialPremiumSlab.calls.count()).toEqual(1);
        });
        
        it('should make 1 call to dataService.updateModel', function () {
            expect(mockDataService.updateModel.calls.count()).toEqual(1);
        });
    });

    describe('Result Controller with valid data - Leasehold and Residential with years set as 1', function () {
        
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

            mockDataService = { 
                getModel : function() { 
                    return {
                        holdingType : "Leasehold",
                        propertyType : "Residential",
                        leaseTerm : {
                            years : 1,
                            days : 0,
                            daysInPartialYear : 0
                        },
                        premium : 10,
                        result : {}
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
                calculateResidentialPremiumSlice: function() {
                    return {
                        totalSDLT : 20
                    };
                },
                calculateResidentialLeaseSlice: function() {
                    return {
                        totalSDLT : 30
                    };
                },
                calculateNPV: function() {
                    return 10;
                }
            };

            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            //spyOn(mockCalculationService, 'calculateResidentialPremiumSlice');
            spyOn(mockDataService, 'updateModel');
            
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
        
        it('should make 1 call to dataService.updateModel', function () {
            expect(mockDataService.updateModel.calls.count()).toEqual(1);
        });

        it('should set the result leasehold residential NPV to 10 (The value returned by stubbed calculateNPV)', function () {
            expect(mockScope.data.result.leasehold.residential.npv).toEqual(10);
        });

        it('should set the result leasehold residential rentTax to 30 (The value returned by stubbed calculateResidentialLeaseSlice)', function () {
            expect(mockScope.data.result.leasehold.residential.rentTax).toEqual(30);
        });

        it('should set the result leasehold residential premiumTax to 20 (The value returned by stubbed calculateResidentialPremiumSlice)', function () {
            expect(mockScope.data.result.leasehold.residential.premiumTax).toEqual(20);
        });   

        it('should set the result leasehold residential totalTax to the sum of the rentTax and premiumTax', function () {
            expect(mockScope.data.result.leasehold.residential.totalTax).toEqual(50);
        });      
    });

    describe('Result Controller with valid data - Leasehold and Residential with years set as 5', function () {
        
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

            mockDataService = { 
                getModel : function() { 
                    return {
                        holdingType : "Leasehold",
                        propertyType : "Residential",
                        leaseTerm : {
                            years : 5,
                            days : 1,
                            daysInPartialYear : 0
                        },
                        year1Rent : 1,
                        year2Rent : 2,
                        year3Rent : 3,
                        year4Rent : 4,
                        year5Rent : 5,
                        premium : 10,
                        result : {}
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
                calculateResidentialPremiumSlice: function() {
                    return {
                        totalSDLT : 20
                    };
                },
                calculateResidentialLeaseSlice: function() {
                    return {
                        totalSDLT : 30
                    };
                },
                calculateNPV: function() {
                    return 10;
                }
            };

            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            //spyOn(mockCalculationService, 'calculateResidentialPremiumSlice');
            spyOn(mockDataService, 'updateModel');
            
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
        
        it('should make 1 call to dataService.updateModel', function () {
            expect(mockDataService.updateModel.calls.count()).toEqual(1);
        });

        it('should set the result leasehold residential NPV to 10 (The value returned by stubbed calculateNPV)', function () {
            expect(mockScope.data.result.leasehold.residential.npv).toEqual(10);
        });

        it('should set the result leasehold residential rentTax to 30 (The value returned by stubbed calculateResidentialLeaseSlice)', function () {
            expect(mockScope.data.result.leasehold.residential.rentTax).toEqual(30);
        });

        it('should set the result leasehold residential premiumTax to 20 (The value returned by stubbed calculateResidentialPremiumSlice)', function () {
            expect(mockScope.data.result.leasehold.residential.premiumTax).toEqual(20);
        });   

        it('should set the result leasehold residential totalTax to the sum of the rentTax and premiumTax', function () {
            expect(mockScope.data.result.leasehold.residential.totalTax).toEqual(50);
        });      
    });

    describe('Result Controller with valid data - Leasehold and Non-residential and relevantRent undefined', function () {
        
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

            mockDataService = { 
                getModel : function() { 
                    return {
                        holdingType : "Leasehold",
                        propertyType : "Non-residential",
                        leaseTerm : {
                            years : 5,
                            days : 1,
                            daysInPartialYear : 0
                        },
                        year1Rent : 1,
                        year2Rent : 2,
                        year3Rent : 3,
                        year4Rent : 4,
                        year5Rent : 5,
                        premium : 10,
                        result : {}
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
                calculateNonResidentialPremiumSlab: function() {
                    return {
                        taxDue : 20
                    };
                },
                calculateNonResidentialLeaseSlice: function() {
                    return {
                        totalSDLT : 30
                    };
                },
                calculateNPV: function() {
                    return 10;
                }
            };

            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            //spyOn(mockCalculationService, 'calculateResidentialPremiumSlice');
            spyOn(mockDataService, 'updateModel');
            
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
        
        it('should make 1 call to dataService.updateModel', function () {
            expect(mockDataService.updateModel.calls.count()).toEqual(1);
        });

        it('should set the result leasehold non-residential NPV to 10 (The value returned by stubbed calculateNPV)', function () {
            expect(mockScope.data.result.leasehold.nonResidential.npv).toEqual(10);
        });

        it('should set the result leasehold non-residential rentTax to 30 (The value returned by stubbed calculateNonResidentialLeaseSlice)', function () {
            expect(mockScope.data.result.leasehold.nonResidential.rentTax).toEqual(30);
        });

        it('should set the result leasehold non-residential premiumTax to 20 (The value returned by stubbed calculateNonResidentialPremiumSlice)', function () {
            expect(mockScope.data.result.leasehold.nonResidential.premiumTax).toEqual(20);
        });   

        it('should set the result leasehold non-residential totalTax to the sum of the rentTax and premiumTax', function () {
            expect(mockScope.data.result.leasehold.nonResidential.totalTax).toEqual(50);
        });      
    });

    describe('Result Controller with valid data - Leasehold and Non-residential and relevantRent defined', function () {
        
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

            mockDataService = { 
                getModel : function() { 
                    return {
                        holdingType : "Leasehold",
                        propertyType : "Non-residential",
                        leaseTerm : {
                            years : 5,
                            days : 1,
                            daysInPartialYear : 0
                        },
                        year1Rent : 1,
                        year2Rent : 2,
                        year3Rent : 3,
                        year4Rent : 4,
                        year5Rent : 5,
                        premium : 10,
                        result : {},
                        relevantRent : 10
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
                calculateNonResidentialPremiumSlab: function() {
                    return {
                        taxDue : 20
                    };
                },
                calculateNonResidentialLeaseSlice: function() {
                    return {
                        totalSDLT : 30
                    };
                },
                calculateNPV: function() {
                    return 10;
                }
            };

            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            //spyOn(mockCalculationService, 'calculateResidentialPremiumSlice');
            spyOn(mockDataService, 'updateModel');
            
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
        
        it('should make 1 call to dataService.updateModel', function () {
            expect(mockDataService.updateModel.calls.count()).toEqual(1);
        });

        it('should set the result leasehold non-residential NPV to 10 (The value returned by stubbed calculateNPV)', function () {
            expect(mockScope.data.result.leasehold.nonResidential.npv).toEqual(10);
        });

        it('should set the result leasehold non-residential rentTax to 30 (The value returned by stubbed calculateNonResidentialLeaseSlice)', function () {
            expect(mockScope.data.result.leasehold.nonResidential.rentTax).toEqual(30);
        });

        it('should set the result leasehold non-residential premiumTax to 20 (The value returned by stubbed calculateNonResidentialPremiumSlice)', function () {
            expect(mockScope.data.result.leasehold.nonResidential.premiumTax).toEqual(20);
        });   

        it('should set the result leasehold non-residential totalTax to the sum of the rentTax and premiumTax', function () {
            expect(mockScope.data.result.leasehold.nonResidential.totalTax).toEqual(50);
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

            mockDataService = { 
                getModel : function() { 
                    return {
                        holdingType : "Freehold",
                        propertyType : "Non-residential"
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
                calculateNonResidentialPremiumSlab: function() {}
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

            mockScope.viewDetails({});
        }));

        it('should make 1 call to navigationService.printView', function() {
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

            mockDataService = { 
                getModel : function() { 
                    return {
                        holdingType : "Freehold",
                        propertyType : "Non-residential"
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
                calculateNonResidentialPremiumSlab: function() {}
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
}());
