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
            mockScope.getHelpSetup = function() {return true;};

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
            mockScope.getHelpSetup = function() {return true;};

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

    describe('Result Controller with valid data - Freehold and Residential - Additional property', function () {
        
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
                calculateResidentialPremiumSlice: function() {},
                calculate201604SecondHomeSlice: function() {},
                calculateResidentialPremiumSlab: function() {}
            };

            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            spyOn(mockCalculationService, 'calculateResidentialPremiumSlice');
            spyOn(mockCalculationService, 'calculate201604SecondHomeSlice');
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

        it('should make 1 call to calculationService.calculate201604SecondHomeSlice', function () {
            expect(mockCalculationService.calculate201604SecondHomeSlice.calls.count()).toEqual(1);
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
            mockScope.getHelpSetup = function() {return true;};

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
            mockScope.getHelpSetup = function() {return true;};

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
                calculateResidentialPremiumSlab: function() {
                    return {
                        taxDue : 40
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

        it('should set the result leasehold before December residential premiumTax to 40 (The value returned by stubbed calculateResidentialPremiumSlab)', function () {
            expect(mockScope.data.result.leasehold.residential.before.premiumTax).toEqual(40);
        });   

        it('should set the result leasehold after December residential premiumTax to 20 (The value returned by stubbed calculateResidentialPremiumSlice)', function () {
            expect(mockScope.data.result.leasehold.residential.from.premiumTax).toEqual(20);
        });  

        it('should set the result leasehold before December residential totalTax to the sum of the rentTax and premiumTax', function () {
            expect(mockScope.data.result.leasehold.residential.before.totalTax).toEqual(70);
        });      

        it('should set the result leasehold after December residential totalTax to the sum of the rentTax and premiumTax', function () {
            expect(mockScope.data.result.leasehold.residential.from.totalTax).toEqual(50);
        });      
    });

    describe('Result Controller with valid data - Leasehold and Residential with years set as 1 - Additional property', function () {
        
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
                        twoOrMoreProperties : "Yes",
                        replaceMainResidence : "No",
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
                calculate201604SecondHomeSlice: function() {
                    return {
                        totalSDLT: 60
                    };
                },
                calculateResidentialPremiumSlice: function() {
                    return {
                        totalSDLT : 20
                    };
                },
                calculateResidentialPremiumSlab: function() {
                    return {
                        taxDue : 40
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

        it('should set the result leasehold before April residential premiumTax to 40 (The value returned by stubbed calculateResidentialPremiumSlab)', function () {
            expect(mockScope.data.result.leasehold.residential.before.premiumTax).toEqual(40);
        });   

        it('should set the result leasehold after April residential premiumTax to 60 (The value returned by stubbed calculate201604SecondHomeSlice)', function () {
            expect(mockScope.data.result.leasehold.residential.from.premiumTax).toEqual(60);
        });  

        it('should set the result leasehold before April residential totalTax to the sum of the rentTax and premiumTax', function () {
            expect(mockScope.data.result.leasehold.residential.before.totalTax).toEqual(70);
        });      

        it('should set the result leasehold after April residential totalTax to the sum of the rentTax and premiumTax', function () {
            expect(mockScope.data.result.leasehold.residential.from.totalTax).toEqual(90);
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
            mockScope.getHelpSetup = function() {return true;};

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
                calculateResidentialPremiumSlab: function() {
                    return {
                        taxDue : 40
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

        it('should set the result leasehold residential before December premiumTax to 40 (The value returned by stubbed calculateResidentialPremiumSlab)', function () {
            expect(mockScope.data.result.leasehold.residential.before.premiumTax).toEqual(40);
        });  

        it('should set the result leasehold residential after December premiumTax to 20 (The value returned by stubbed calculateResidentialPremiumSlice)', function () {
            expect(mockScope.data.result.leasehold.residential.from.premiumTax).toEqual(20);
        });   

        it('should set the result leasehold before December residential totalTax to the sum of the rentTax and premiumTax', function () {
            expect(mockScope.data.result.leasehold.residential.before.totalTax).toEqual(70);
        });      

        it('should set the result leasehold after December residential totalTax to the sum of the rentTax and premiumTax', function () {
            expect(mockScope.data.result.leasehold.residential.from.totalTax).toEqual(50);
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
            mockScope.getHelpSetup = function() {return true;};

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
            mockScope.getHelpSetup = function() {return true;};

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










describe('Result Controller with valid data - Leasehold and Non-residential and relevantRent defined - looks for year2Rent<2000', function () {
        
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
                        leaseTerm : {
                            years : 5,
                            days : 1,
                            daysInPartialYear : 0
                        },
                        year1Rent : 3000,
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









describe('Result Controller with valid data - Leasehold and Non-residential and relevantRent defined - looks for year3Rent<2000', function () {
        
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
                        leaseTerm : {
                            years : 5,
                            days : 1,
                            daysInPartialYear : 0
                        },
                        year1Rent : 3000,
                        year2Rent : 3000,
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





describe('Result Controller with valid data - Leasehold and Non-residential and relevantRent defined - looks for year4Rent<2000', function () {
        
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
                        leaseTerm : {
                            years : 5,
                            days : 1,
                            daysInPartialYear : 0
                        },
                        year1Rent : 3000,
                        year2Rent : 3000,
                        year3Rent : 3000,
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





describe('Result Controller with valid data - Leasehold and Non-residential and relevantRent defined - looks for year5Rent<2000', function () {
        
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
                        leaseTerm : {
                            years : 5,
                            days : 1,
                            daysInPartialYear : 0
                        },
                        year1Rent : 3000,
                        year2Rent : 3000,
                        year3Rent : 3000,
                        year4Rent : 3000,
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





    describe('Result Controller with valid data - Leasehold and Non-residential and relevantRent defined, but not required', function () {
        
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
                        leaseTerm : {
                            years : 5,
                            days : 0,
                            daysInPartialYear : 365
                        },
                        year1Rent : 3000,
                        year2Rent : 3000,
                        year3Rent : 3000,
                        year4Rent : 3000,
                        year5Rent : 3000,
                        premium : 200000,
                        result : {},
                        relevantRent : 2000
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
                calculateNonResidentialPremiumSlab: function(scope, rr) {
                    return { taxDue : 0 };
                },
                calculateNonResidentialLeaseSlice: function() {
                    return { totalSDLT : 30 };
                },
                calculateNPV: function() {
                    return 10;
                }
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

        it('should set the result leasehold non-residential NPV to 10 (The value returned by stubbed calculateNPV)', function () {
            expect(mockScope.data.result.leasehold.nonResidential.npv).toEqual(10);
        });

        it('should set the result leasehold non-residential rentTax to 30 (The value returned by stubbed calculateNonResidentialLeaseSlice)', function () {
            expect(mockScope.data.result.leasehold.nonResidential.rentTax).toEqual(30);
        });

        it('should set the result leasehold non-residential premiumTax to 0 (The value of rr returned by stubbed calculateNonResidentialPremiumSlab)', function () {
            expect(mockScope.data.result.leasehold.nonResidential.premiumTax).toEqual(0);
        });   

        it('should set the result leasehold non-residential totalTax to the sum of the rentTax and premiumTax', function () {
            expect(mockScope.data.result.leasehold.nonResidential.totalTax).toEqual(30);
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
            mockScope.getHelpSetup = function() {return true;};
            
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


    describe('calling effDateAfterCutoff and getHeading with date after cut-off date', function () {

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
                        leaseTerm : "banana",
                        effectiveDate : new Date("December 6, 2014")
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

        it('should return true for effectiveDateAfterCutOff() if Effective Date is 06/12/2014', function () {
            expect(mockScope.effDateAfterCutOff()).toEqual(true);
        });

        it('should return correct text for getHeading() if Effective Date is 06/12/2014', function () {
            expect(mockScope.getHeading()).toEqual("Results based on SDLT rules before 4 December 2014");
        });
    });

    describe('calling effDateAfterAprilCutoff with date after April cut-off date', function () {

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
                        leaseTerm : "banana",
                        effectiveDate : new Date("April 4, 2016")
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

        it('should return true for effectiveDateAfterCutOff() if Effective Date is 04/04/2016', function () {
            expect(mockScope.effDateAfterAprilCutOff()).toEqual(true);
        });

    });



    describe('calling effDateAfterCutoff and getHeading with date on cut-off date', function () {

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
                        leaseTerm : "banana",
                        effectiveDate : new Date("December 4, 2014")
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
                        leaseTerm : "banana",
                        effectiveDate : new Date("April 1, 2016")
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

        it('should return true for effectiveDateAfterCutOff() if Effective Date is 01/04/2016', function () {
            expect(mockScope.effDateAfterCutOff()).toEqual(true);
        });

    });


    describe('calling effDateAfterCutoff and getHeading with date before cut-off date', function () {

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
                        propertyType : "Residential",
                        leaseTerm : "banana",
                        effectiveDate : new Date("December 3, 2014")
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

        it('should return false for effectiveDateAfterCutOff() if Effective Date is 03/12/2014', function () {
            expect(mockScope.effDateAfterCutOff()).toEqual(false);
        });

        it('should return correct text for getHeading() if Effective Date is 03/12/2014', function () {
            expect(mockScope.getHeading()).toEqual(undefined);
        });
    });

    describe('Result Controller with invalid data for holding type', function () {
        
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
                        holdingType : "banana",
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
                calculateNonResidentialPremiumSlab: function(scope, rr) {
                    return { taxDue : rr };
                },
                calculateNonResidentialLeaseSlice: function() {
                    return { totalSDLT : 30 };
                },
                calculateNPV: function() {
                    return 10;
                }
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

        it('should set the result leasehold non-residential NPV to 10 (The value returned by stubbed calculateNPV)', function () {
            expect(mockScope.data.result.leasehold.nonResidential.npv).toEqual(-1);
        });

        it('should set the result leasehold non-residential rentTax to 30 (The value returned by stubbed calculateNonResidentialLeaseSlice)', function () {
            expect(mockScope.data.result.leasehold.nonResidential.rentTax).toEqual(-1);
        });

        it('should set the result leasehold non-residential premiumTax to 0 (The value of rr returned by stubbed calculateNonResidentialPremiumSlab)', function () {
            expect(mockScope.data.result.leasehold.nonResidential.premiumTax).toEqual(-1);
        });   

        it('should set the result leasehold non-residential totalTax to the sum of the rentTax and premiumTax', function () {
            expect(mockScope.data.result.leasehold.nonResidential.totalTax).toEqual(-1);
        });      
    });

    describe('Result Controller with invalid data for property type', function () {
        
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
                        propertyType : "banana",
                        leaseTerm : {
                            years : 1,
                            days : 0,
                            daysInPartialYear : 365
                        },
                        year1Rent : 3000,
                        premium : 200000,
                        result : {},
                        relevantRent : 2000
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
                calculateNonResidentialPremiumSlab: function(scope, rr) {
                    return { taxDue : rr };
                },
                calculateNonResidentialLeaseSlice: function() {
                    return { totalSDLT : 30 };
                },
                calculateNPV: function() {
                    return 10;
                }
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

        it('should set the result leasehold non-residential NPV to 10 (The value returned by stubbed calculateNPV)', function () {
            expect(mockScope.data.result.leasehold.nonResidential.npv).toEqual(-1);
        });

        it('should set the result leasehold non-residential rentTax to 30 (The value returned by stubbed calculateNonResidentialLeaseSlice)', function () {
            expect(mockScope.data.result.leasehold.nonResidential.rentTax).toEqual(-1);
        });

        it('should set the result leasehold non-residential premiumTax to 0 (The value of rr returned by stubbed calculateNonResidentialPremiumSlab)', function () {
            expect(mockScope.data.result.leasehold.nonResidential.premiumTax).toEqual(-1);
        });   

        it('should set the result leasehold non-residential totalTax to the sum of the rentTax and premiumTax', function () {
            expect(mockScope.data.result.leasehold.nonResidential.totalTax).toEqual(-1);
        });      
    });



    describe('Result Controller with relevant rent = 1000', function () {
        
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
                        leaseTerm : {
                            years : 1,
                            days : 0,
                            daysInPartialYear : 365
                        },
                        year1Rent : 1999,
                        premium : 149999,
                        result : {},
                        relevantRent : 1000
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
                calculateNonResidentialPremiumSlab: function(scope, zr) {
                    return { taxDue : 15 };
                },
                calculateNonResidentialLeaseSlice: function() {
                    return { totalSDLT : 30 };
                },
                calculateNPV: function() {
                    return 10;
                }
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

        it('should set the result leasehold non-residential NPV to 10 (The value returned by stubbed calculateNPV)', function () {
            expect(mockScope.data.result.leasehold.nonResidential.npv).toEqual(10);
        });

        it('should set the result leasehold non-residential rentTax to 30 (The value returned by stubbed calculateNonResidentialLeaseSlice)', function () {
            expect(mockScope.data.result.leasehold.nonResidential.rentTax).toEqual(30);
        });

        it('should set the result leasehold non-residential premiumTax to 0 (The value of rr returned by stubbed calculateNonResidentialPremiumSlab)', function () {
            expect(mockScope.data.result.leasehold.nonResidential.premiumTax).toEqual(15);
        });   

        it('should set the result leasehold non-residential totalTax to the sum of the rentTax and premiumTax', function () {
            expect(mockScope.data.result.leasehold.nonResidential.totalTax).toEqual(45);
        });      
    });

}());
