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


        describe('test for displayRelevantRent', function () {

            beforeEach(mocks.inject(function ($controller, $rootScope) {
                
                mockScope = $rootScope.$new();
                mockScope.getHelpSetup = function() {return true;};
                
                mockDataService = { 
                    getModel : function() { 
                        return {
                            holdingType : "Leasehold",
                            propertyType : "Non-residential",
                            effectiveDate : new Date(2016, 2, 16),
                            premium : 149000,
                            leaseTerm : {
                                years : 5
                            },
                            year1Rent : "1",
                            year2Rent : "2",
                            year3Rent : "3",
                            year4Rent : "4",
                            year5Rent : "5",
                            contractPre201603 : undefined,
                            contractVariedPost201603 : undefined
                        }; 
                    },
                    updateModel : function(data) {
                        return {};
                    }                
                };

                mockNavigationService = { 
                    logView : function() {},
                    next : function() {}
                };

                mockModelValidationService = {
                    validate : function() {
                        return { isModelValid : true };
                    }
                };

                spyOn(mockDataService, 'getModel').and.callThrough();
                spyOn(mockDataService, 'updateModel').and.callThrough();
                spyOn(mockNavigationService, 'logView');
                spyOn(mockNavigationService, 'next');
                spyOn(mockModelValidationService, 'validate').and.callThrough();
                
                controller = $controller('printController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    modelValidationService : mockModelValidationService,
                    navigationService : mockNavigationService
                });
            }));

            it('displayRelevantRent should be true when common checks true and eff date < 17/3/16', function () {
                expect(mockScope.displayRelevantRent()).toEqual(true);
            });

            it('displayRelevantRent should be true when common checks true, eff date = 17/3/16, contract date pre 17/3/16 and contract not varied', function () {
                mockScope.data.effectiveDate = new Date(2016, 2, 17);
                mockScope.data.contractPre201603 = 'Yes';
                mockScope.data.contractVariedPost201603 = 'No';
                expect(mockScope.displayRelevantRent()).toEqual(true);
            });

            it('displayRelevantRent should be false when Freehold', function () {
                mockScope.data.holdingType = "Freehold";
                expect(mockScope.displayRelevantRent()).toEqual(false);
            });
            it('displayRelevantRent should be false when Residential', function () {
                mockScope.data.propertyType = "Residential";
                expect(mockScope.displayRelevantRent()).toEqual(false);
            });
            it('displayRelevantRent should be false when Premium 150K', function () {
                mockScope.data.premium = 150000;
                expect(mockScope.displayRelevantRent()).toEqual(false);
            });
            it('displayRelevantRent should be false when Rent 2K', function () {
                mockScope.data.year1Rent = 2000;
                expect(mockScope.displayRelevantRent()).toEqual(false);
            });
            it('displayRelevantRent should be false when Eff date 17/3/16 and Contract pre 17/03/16', function () {
                mockScope.data.effectiveDate = new Date(2016, 2, 17);
                mockScope.data.contractPre201603 = 'No';
                expect(mockScope.displayRelevantRent()).toEqual(false);
            });
            it('displayRelevantRent should be false when Eff date 17/3/16 and Contract pre 17/03/16', function () {
                mockScope.data.effectiveDate = new Date(2016, 2, 17);
                mockScope.data.contractPre201603 = 'Yes';
                mockScope.data.contractVariedPost201603 = 'Yes';
                expect(mockScope.displayRelevantRent()).toEqual(false);
            });
        });

        describe('test for displayAdditionalProperty', function () {
            

            beforeEach(mocks.inject(function ($controller, $rootScope) {
                
                mockScope = $rootScope.$new();
                mockScope.getHelpSetup = function() {return true;};
                
                mockDataService = { 
                    getModel : function() { 
                        return {
                            holdingType : "",
                            propertyType : "",
                            effectiveDate : undefined
                        }; 
                    },
                    updateModel : function(data) {
                        return {};
                    }                
                };

                mockNavigationService = { 
                    logView : function() {},
                    next : function() {}
                };

                mockModelValidationService = {
                    validate : function() {
                        return { isModelValid : true };
                    }
                };

                spyOn(mockDataService, 'getModel').and.callThrough();
                spyOn(mockDataService, 'updateModel').and.callThrough();
                spyOn(mockNavigationService, 'logView');
                spyOn(mockNavigationService, 'next');
                spyOn(mockModelValidationService, 'validate').and.callThrough();
                
                controller = $controller('printController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    modelValidationService : mockModelValidationService,
                    navigationService : mockNavigationService
                });
            }));

            it('displayAdditionalProperty should be false when no data supplied', function () {
                mockScope.data.holdingType = undefined;
                mockScope.data.propertyType = undefined;
                mockScope.data.effectiveDate = undefined;
                expect(mockScope.displayAdditionalProperty()).toEqual(false);
            });

            it('displayAdditionalProperty should be false for Freehold Residential when date is 31/03/2016', function () {
                mockScope.data.holdingType = "Freehold";
                mockScope.data.propertyType = "Residential";
                mockScope.data.effectiveDate = new Date(2016,2,31);
                expect(mockScope.displayAdditionalProperty()).toEqual(false);
            });

            it('displayAdditionalProperty should be true for Freehold Residential when date is 01/04/2016', function () {
                mockScope.data.holdingType = "Freehold";
                mockScope.data.propertyType = "Residential";
                mockScope.data.effectiveDate = new Date(2016,3,1);
                expect(mockScope.displayAdditionalProperty()).toEqual(true);
            });

            it('displayAdditionalProperty should be false for Freehold Non-residential when date is 01/04/2016', function () {
                mockScope.data.holdingType = "Freehold";
                mockScope.data.propertyType = "Non-residential";
                mockScope.data.effectiveDate = new Date(2016,3,1);
                expect(mockScope.displayAdditionalProperty()).toEqual(false);
            });

            it('displayAdditionalProperty should be false for Leasehold Residential when date is 31/03/2016', function () {
                mockScope.data.holdingType = "Leasehold";
                mockScope.data.propertyType = "Residential";
                mockScope.data.effectiveDate = new Date(2016,2,31);
                expect(mockScope.displayAdditionalProperty()).toEqual(false);
            });

            it('displayAdditionalProperty should be true for Leasehold Residential when date is 01/04/2016', function () {
                mockScope.data.holdingType = "Leasehold";
                mockScope.data.propertyType = "Residential";
                mockScope.data.effectiveDate = new Date(2016,3,1);
                expect(mockScope.displayAdditionalProperty()).toEqual(true);
            });

            it('displayAdditionalProperty should be false for Leasehold Non-residential when date is 01/04/2016', function () {
                mockScope.data.holdingType = "Leasehold";
                mockScope.data.propertyType = "Non-residential";
                mockScope.data.effectiveDate = new Date(2016,3,1);
                expect(mockScope.displayAdditionalProperty()).toEqual(false);
            });

        });

        describe('test for displayReplaceMainResidence', function () {

            beforeEach(mocks.inject(function ($controller, $rootScope) {
                
                mockScope = $rootScope.$new();
                mockScope.getHelpSetup = function() {return true;};
                
                mockDataService = { 
                    getModel : function() { 
                        return {
                            holdingType : "",
                            propertyType : "",
                            effectiveDate : undefined,
                            twoOrMoreProperties : undefined,
                            replaceMainResidence : undefined
                        }; 
                    },
                    updateModel : function(data) {
                        return {};
                    }                
                };

                mockNavigationService = { 
                    logView : function() {},
                    next : function() {}
                };

                mockModelValidationService = {
                    validate : function() {
                        return { isModelValid : true };
                    }
                };

                spyOn(mockDataService, 'getModel').and.callThrough();
                spyOn(mockDataService, 'updateModel').and.callThrough();
                spyOn(mockNavigationService, 'logView');
                spyOn(mockNavigationService, 'next');
                spyOn(mockModelValidationService, 'validate').and.callThrough();
                
                controller = $controller('printController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    modelValidationService : mockModelValidationService,
                    navigationService : mockNavigationService
                });
            }));

            it('displayReplaceMainResidence should be false when no data supplied', function () {
                mockScope.data.holdingType = undefined;
                mockScope.data.propertyType = undefined;
                mockScope.data.effectiveDate = undefined;
                expect(mockScope.displayReplaceMainResidence()).toEqual(false);
            });

            it('displayReplaceMainResidence should be false when before 1/4/16', function () {
                mockScope.data.holdingType = "Freehold";
                mockScope.data.propertyType = "Residential";
                mockScope.data.effectiveDate = new Date(2016, 2, 31);
                expect(mockScope.displayReplaceMainResidence()).toEqual(false);
            });

            it('displayReplaceMainResidence should be false when not 2nd property (FH)', function () {
                mockScope.data.holdingType = "Freehold";
                mockScope.data.propertyType = "Residential";
                mockScope.data.effectiveDate = new Date(2016, 3, 1);
                mockScope.data.twoOrMoreProperties = "No";
                expect(mockScope.displayReplaceMainResidence()).toEqual(false);
            });

            it('displayReplaceMainResidence should be false when not 2nd property (LH)', function () {
                mockScope.data.holdingType = "Leasehold";
                mockScope.data.propertyType = "Residential";
                mockScope.data.effectiveDate = new Date(2016, 3, 1);
                mockScope.data.twoOrMoreProperties = "No";
                expect(mockScope.displayReplaceMainResidence()).toEqual(false);
            });

            it('displayReplaceMainResidence should be true when 2nd property (FH)', function () {
                mockScope.data.holdingType = "Freehold";
                mockScope.data.propertyType = "Residential";
                mockScope.data.effectiveDate = new Date(2016, 3, 1);
                mockScope.data.twoOrMoreProperties = "Yes";
                expect(mockScope.displayReplaceMainResidence()).toEqual(true);
            });

            it('displayReplaceMainResidence should be true when 2nd property (LH)', function () {
                mockScope.data.holdingType = "Leasehold";
                mockScope.data.propertyType = "Residential";
                mockScope.data.effectiveDate = new Date(2016, 3, 1);
                mockScope.data.twoOrMoreProperties = "Yes";
                expect(mockScope.displayReplaceMainResidence()).toEqual(true);
            });

        });

        describe('test for displayExchangeContracts', function () {
            

            beforeEach(mocks.inject(function ($controller, $rootScope) {
                
                mockScope = $rootScope.$new();
                mockScope.getHelpSetup = function() {return true;};
                
                mockDataService = { 
                    getModel : function() { 
                        return {
                            holdingType : "Leasehold",
                            propertyType : "Non-residential",
                            effectiveDate : new Date(2016, 2, 17),
                            premium : 149000,
                            year1Rent : 1999,
                            year2Rent : 1999,
                            year3Rent : 1999,
                            year4Rent : 1999,
                            year5Rent : 1999
                        }; 
                    },
                    updateModel : function(data) {
                        return {};
                    }                
                };

                mockNavigationService = { 
                    logView : function() {},
                    next : function() {}
                };

                mockModelValidationService = {
                    validate : function() {
                        return { isModelValid : true };
                    }
                };

                spyOn(mockDataService, 'getModel').and.callThrough();
                spyOn(mockDataService, 'updateModel').and.callThrough();
                spyOn(mockNavigationService, 'logView');
                spyOn(mockNavigationService, 'next');
                spyOn(mockModelValidationService, 'validate').and.callThrough();
                
                controller = $controller('printController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    modelValidationService : mockModelValidationService,
                    navigationService : mockNavigationService
                });
            }));

            it('displayExchangeContracts should be true for Leasehold, Non-residential, Premium 149000, Eff date17/03/2016 and all rents < 2000', function () {
                expect(mockScope.displayExchangeContracts()).toEqual(true);
            });


            it('displayExchangeContracts should be false when no data supplied', function () {
                mockScope.data.holdingType = undefined;
                mockScope.data.propertyType = undefined;
                mockScope.data.effectiveDate = undefined;
                expect(mockScope.displayExchangeContracts()).toEqual(false);
            });

            it('displayExchangeContracts should be false for Freehold', function () {
                mockScope.data.holdingType = "Freehold";
                expect(mockScope.displayExchangeContracts()).toEqual(false);
            });

            it('displayExchangeContracts should be false for Residential', function () {
                mockScope.data.propertyType = "Residential";
                expect(mockScope.displayExchangeContracts()).toEqual(false);
            });

            it('displayExchangeContracts should be false for Eff date < 17/03/2016', function () {
                mockScope.data.effectiveDate = new Date(2016,2,16);
                expect(mockScope.displayExchangeContracts()).toEqual(false);
            });

            it('displayExchangeContracts should be false for Premium 150k', function () {
                mockScope.data.premium = 150000;
                expect(mockScope.displayExchangeContracts()).toEqual(false);
            });

            it('displayExchangeContracts should be false for rent 1 £2k', function () {
                mockScope.data.year1Rent = 2000;
                expect(mockScope.displayExchangeContracts()).toEqual(false);
            });

            it('displayExchangeContracts should be false for rent 2 £2k', function () {
                mockScope.data.year2Rent = 2000;
                expect(mockScope.displayExchangeContracts()).toEqual(false);
            });

            it('displayExchangeContracts should be false for rent 3 £2k', function () {
                mockScope.data.year3Rent = 2000;
                expect(mockScope.displayExchangeContracts()).toEqual(false);
            });

            it('displayExchangeContracts should be false for rent 4 £2k', function () {
                mockScope.data.year4Rent = 2000;
                expect(mockScope.displayExchangeContracts()).toEqual(false);
            });

            it('displayExchangeContracts should be false for rent 5 £2k', function () {
                mockScope.data.year5Rent = 2000;
                expect(mockScope.displayExchangeContracts()).toEqual(false);
            });

        });

        describe('test for displayContractVaried', function () {

            beforeEach(mocks.inject(function ($controller, $rootScope) {
                
                mockScope = $rootScope.$new();
                mockScope.getHelpSetup = function() {return true;};
                
                mockDataService = { 
                    getModel : function() { 
                        return {
                            holdingType : "Leasehold",
                            propertyType : "Non-residential",
                            effectiveDate : new Date(2016, 2, 17),
                            premium : 149000,
                            year1Rent : 1999,
                            year2Rent : 1999,
                            year3Rent : 1999,
                            year4Rent : 1999,
                            year5Rent : 1999,
                            contractPre201603 : 'Yes'
                        }; 
                    },
                    updateModel : function(data) {
                        return {};
                    }                
                };

                mockNavigationService = { 
                    logView : function() {},
                    next : function() {}
                };

                mockModelValidationService = {
                    validate : function() {
                        return { isModelValid : true };
                    }
                };

                spyOn(mockDataService, 'getModel').and.callThrough();
                spyOn(mockDataService, 'updateModel').and.callThrough();
                spyOn(mockNavigationService, 'logView');
                spyOn(mockNavigationService, 'next');
                spyOn(mockModelValidationService, 'validate').and.callThrough();
                
                controller = $controller('printController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    modelValidationService : mockModelValidationService,
                    navigationService : mockNavigationService
                });
            }));

            it('displayContractVaried should be true for Leasehold, Non-residential, Premium 149000, Eff date17/03/2016, all rents < 2000 and contract date Pre-201603', function () {
                expect(mockScope.displayContractVaried()).toEqual(true);
            });

            it('displayContractVaried should be false for Leasehold, Non-residential, Premium 149000, Eff date17/03/2016, all rents < 2000 and contract date Post-201603', function () {
                mockScope.data.contractPre201603 = 'No';
                expect(mockScope.displayContractVaried()).toEqual(false);
            });

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
                modelValidationService : mockModelValidationService
            });
        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });


        it('should set the location path to /summary', function() {
            expect(mockLocation.path()).toEqual('/summary');
        });

    });

}());
