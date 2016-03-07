(function() {
    'use strict';

    require("calc-module");

    var mocks = require("angular-mocks-wrapper");

    describe('Summary Controller', function () {
        
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
                getModel : function() { 
                    return {
                        holdingType : "Leasehold",
                        leaseTerm : "banana"
                    }; 
                },
                updateModel : function(data) {
                    return {};
                }
            };

            mockNavigationService = { 
                logView : function() {} 
            };

            mockModelValidationService = {
                validate : function() {
                    return { isModelValid : true };
                }
            };

            spyOn(mockDataService, 'updateModel');
            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            
            controller = $controller('summaryController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                navigationService : mockNavigationService,
                modelValidationService : mockModelValidationService
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
                mockScope.getHelpSetup = function() {return true;};

                mockDataService = { 
                    getModel : function() { 
                        return {
                            holdingType : "Leasehold",
                            leaseTerm : "banana"
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
                spyOn(mockDataService, 'updateModel');
                spyOn(mockNavigationService, 'logView');
                spyOn(mockNavigationService, 'next');
                spyOn(mockModelValidationService, 'validate').and.callThrough();
                
                controller = $controller('summaryController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    modelValidationService : mockModelValidationService,
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
                expect(mockModelValidationService.validate.calls.count()).toEqual(1);
            });

            // on submit
            it('should make 1 call to dataService.updateModel', function () {
                expect(mockDataService.updateModel.calls.count()).toEqual(1);
            });

            it('should make 1 call to navigationService.next', function () {
                expect(mockNavigationService.next.calls.count()).toEqual(1);
            });
        });

        describe('LeaseTerm is 1 year', function () {
            
            beforeEach(mocks.inject(function ($controller, $rootScope) {
                
                mockScope = $rootScope.$new();
                mockScope.getHelpSetup = function() {return true;};

                mockDataService = { 
                    getModel : function() { 
                        return {
                            holdingType : "Leasehold",
                            leaseTerm : {
                                years : 1
                            },
                            year1Rent : "1",
                            year2Rent : "2",
                            year3Rent : "3",
                            year4Rent : "4",
                            year5Rent : "5"
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
                
                controller = $controller('summaryController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    modelValidationService : mockModelValidationService,
                    navigationService : mockNavigationService
                });
            }));

            // on create
            it('should make 1 call to dataService.getModel', function () {
                expect(mockDataService.getModel.calls.count()).toEqual(1);
            });

            it('should make 1 call to navigationService.logView', function () {
                expect(mockNavigationService.logView.calls.count()).toEqual(1);
            });

            it('should make 1 call to validationService.validate', function () {
                expect(mockModelValidationService.validate.calls.count()).toEqual(1);
            });

            it('should set displayYearOneRent to true', function() {
                expect(mockScope.displayYearOneRent).toEqual(true);
            });

            it('should compare highest to year1Rent when displayYearOneRent is true', function() {
                expect(mockScope.data.year1Rent).toEqual("1");
                expect(mockScope.data.highestRent).toEqual("1");
            });
        });

        describe('LeaseTerm is 2 years', function () {
            
            beforeEach(mocks.inject(function ($controller, $rootScope) {
                
                mockScope = $rootScope.$new();
                mockScope.getHelpSetup = function() {return true;};

                mockDataService = { 
                    getModel : function() { 
                        return {
                            holdingType : "Leasehold",
                            leaseTerm : {
                                years : 2
                            },
                            year1Rent : "1",
                            year2Rent : "2",
                            year3Rent : "3",
                            year4Rent : "4",
                            year5Rent : "5"
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
                
                controller = $controller('summaryController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    modelValidationService : mockModelValidationService,
                    navigationService : mockNavigationService
                });
            }));

            // on create
            it('should make 1 call to dataService.getModel', function () {
                expect(mockDataService.getModel.calls.count()).toEqual(1);
            });

            it('should make 1 call to navigationService.logView', function () {
                expect(mockNavigationService.logView.calls.count()).toEqual(1);
            });

            it('should make 1 call to validationService.validate', function () {
                expect(mockModelValidationService.validate.calls.count()).toEqual(1);
            });

            it('should set displayYearTwoRent to true', function() {
                expect(mockScope.displayYearTwoRent).toEqual(true);
            });

            it('should compare highest to year2Rent when displayYearTwoRent is true', function() {
                expect(mockScope.data.year2Rent).toEqual("2");
                expect(mockScope.data.highestRent).toEqual("2");
            });
        });

        describe('LeaseTerm is 3 years', function () {
            
            beforeEach(mocks.inject(function ($controller, $rootScope) {
                
                mockScope = $rootScope.$new();
                mockScope.getHelpSetup = function() {return true;};

                mockDataService = { 
                    getModel : function() { 
                        return {
                            holdingType : "Leasehold",
                            leaseTerm : {
                                years : 3
                            },
                            year1Rent : "1",
                            year2Rent : "2",
                            year3Rent : "3",
                            year4Rent : "4",
                            year5Rent : "5"
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
                
                controller = $controller('summaryController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    modelValidationService : mockModelValidationService,
                    navigationService : mockNavigationService
                });
            }));

            // on create
            it('should make 1 call to dataService.getModel', function () {
                expect(mockDataService.getModel.calls.count()).toEqual(1);
            });

            it('should make 1 call to navigationService.logView', function () {
                expect(mockNavigationService.logView.calls.count()).toEqual(1);
            });

            it('should make 1 call to validationService.validate', function () {
                expect(mockModelValidationService.validate.calls.count()).toEqual(1);
            });

            it('should set displayYearThreeRent to true', function() {
                expect(mockScope.displayYearThreeRent).toEqual(true);
            });

            it('should compare highest to year3Rent when displayYearThreeRent is true', function() {
                expect(mockScope.data.year3Rent).toEqual("3");
                expect(mockScope.data.highestRent).toEqual("3");
            });
        });

        describe('LeaseTerm is 4 years', function () {
            
            beforeEach(mocks.inject(function ($controller, $rootScope) {
                
                mockScope = $rootScope.$new();
                mockScope.getHelpSetup = function() {return true;};

                mockDataService = { 
                    getModel : function() { 
                        return {
                            holdingType : "Leasehold",
                            leaseTerm : {
                                years : 4
                            },
                            year1Rent : "1",
                            year2Rent : "2",
                            year3Rent : "3",
                            year4Rent : "4",
                            year5Rent : "5"
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
                
                controller = $controller('summaryController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    modelValidationService : mockModelValidationService,
                    navigationService : mockNavigationService
                });
            }));

            // on create
            it('should make 1 call to dataService.getModel', function () {
                expect(mockDataService.getModel.calls.count()).toEqual(1);
            });

            it('should make 1 call to navigationService.logView', function () {
                expect(mockNavigationService.logView.calls.count()).toEqual(1);
            });

            it('should make 1 call to validationService.validate', function () {
                expect(mockModelValidationService.validate.calls.count()).toEqual(1);
            });

            it('should set displayYearFourRent to true', function() {
                expect(mockScope.displayYearFourRent).toEqual(true);
            });

            it('should compare highest to year4Rent when displayYearFourRent is true', function() {
                expect(mockScope.data.year4Rent).toEqual("4");
                expect(mockScope.data.highestRent).toEqual("4");
            });
        });

        describe('LeaseTerm is 5 years', function () {
            
            beforeEach(mocks.inject(function ($controller, $rootScope) {
                
                mockScope = $rootScope.$new();
                mockScope.getHelpSetup = function() {return true;};
                
                mockDataService = { 
                    getModel : function() { 
                        return {
                            holdingType : "Leasehold",
                            leaseTerm : {
                                years : 5
                            },
                            year1Rent : "1",
                            year2Rent : "2",
                            year3Rent : "3",
                            year4Rent : "4",
                            year5Rent : "5"
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
                
                controller = $controller('summaryController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    modelValidationService : mockModelValidationService,
                    navigationService : mockNavigationService
                });
            }));

            // on create
            it('should make 1 call to dataService.getModel', function () {
                expect(mockDataService.getModel.calls.count()).toEqual(1);
            });

            it('should make 1 call to navigationService.logView', function () {
                expect(mockNavigationService.logView.calls.count()).toEqual(1);
            });

            it('should make 1 call to validationService.validate', function () {
                expect(mockModelValidationService.validate.calls.count()).toEqual(1);
            });

            it('should set displayYearFiveRent to true', function() {
                expect(mockScope.displayYearFiveRent).toEqual(true);
            });

            it('should compare highest to year5Rent when displayYearFiveRent is true', function() {
                expect(mockScope.data.year5Rent).toEqual("5");
                expect(mockScope.data.highestRent).toEqual("5");
            });
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
                
                controller = $controller('summaryController', {
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
                
                controller = $controller('summaryController', {
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
                
                controller = $controller('summaryController', {
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
                
                controller = $controller('summaryController', {
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
                
                controller = $controller('summaryController', {
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

}());
