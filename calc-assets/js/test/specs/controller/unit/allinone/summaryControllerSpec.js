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

                mockValidationService = {
                    validate : function() {
                        return { isValid : true };
                    }
                };

                spyOn(mockDataService, 'getModel').and.callThrough();
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

                mockValidationService = {
                    validate : function() {
                        return { isValid : true };
                    }
                };

                spyOn(mockDataService, 'getModel').and.callThrough();
                spyOn(mockDataService, 'updateModel').and.callThrough();
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

                mockValidationService = {
                    validate : function() {
                        return { isValid : true };
                    }
                };

                spyOn(mockDataService, 'getModel').and.callThrough();
                spyOn(mockDataService, 'updateModel').and.callThrough();
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

                mockValidationService = {
                    validate : function() {
                        return { isValid : true };
                    }
                };

                spyOn(mockDataService, 'getModel').and.callThrough();
                spyOn(mockDataService, 'updateModel').and.callThrough();
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

                mockValidationService = {
                    validate : function() {
                        return { isValid : true };
                    }
                };

                spyOn(mockDataService, 'getModel').and.callThrough();
                spyOn(mockDataService, 'updateModel').and.callThrough();
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

                mockValidationService = {
                    validate : function() {
                        return { isValid : true };
                    }
                };

                spyOn(mockDataService, 'getModel').and.callThrough();
                spyOn(mockDataService, 'updateModel').and.callThrough();
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
                            leaseTerm : {
                                years : 5
                            },
                            premium : "149999",
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

                mockValidationService = {
                    validate : function() {
                        return { isValid : true };
                    }
                };

                spyOn(mockDataService, 'getModel').and.callThrough();
                spyOn(mockDataService, 'updateModel').and.callThrough();
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
            }));

            // on create
            it('displayRelevantRent should be true when all rents < 2000', function () {
                expect(mockScope.displayRelevantRent()).toEqual(true);
            });

            it('displayRelevantRent should be false when any rent >= 2000', function () {
                mockScope.data.year5Rent = "2000";
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

                mockValidationService = {
                    validate : function() {
                        return { isValid : true };
                    }
                };

                spyOn(mockDataService, 'getModel').and.callThrough();
                spyOn(mockDataService, 'updateModel').and.callThrough();
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
            }));

            it('displayAdditionalProperty should be false when Freehold Residential and no date specified', function () {
                mockScope.data.holdingType = "Freehold";
                mockScope.data.propertyType = "Residential";
                expect(mockScope.displayAdditionalProperty()).toEqual(false);
            });

            it('displayAdditionalProperty should be false when Freehold Residential and date is 31/03/2016', function () {
                mockScope.data.holdingType = "Freehold";
                mockScope.data.propertyType = "Residential";
                mockScope.data.effectiveDate = new Date(2016, 2, 31);
                expect(mockScope.displayAdditionalProperty()).toEqual(false);
            });

            it('displayAdditionalProperty should be true when Freehold Residential and date is 01/04/2016', function () {
                mockScope.data.holdingType = "Freehold";
                mockScope.data.propertyType = "Residential";
                mockScope.data.effectiveDate = new Date(2016, 3, 1);
                expect(mockScope.displayAdditionalProperty()).toEqual(true);
            });

            it('displayAdditionalProperty should be false when Freehold Non-residential and date is 01/04/2016', function () {
                mockScope.data.holdingType = "Freehold";
                mockScope.data.propertyType = "Non-residential";
                mockScope.data.effectiveDate = new Date(2016, 3, 1);
                expect(mockScope.displayAdditionalProperty()).toEqual(false);
            });

            it('displayAdditionalProperty should be false when Leasehold Residential and date is 31/03/2016', function () {
                mockScope.data.holdingType = "Leasehold";
                mockScope.data.propertyType = "Residential";
                mockScope.data.effectiveDate = new Date(2016, 2, 31);
                expect(mockScope.displayAdditionalProperty()).toEqual(false);
            });

            it('displayAdditionalProperty should be true when Leasehold Residential and date is 01/04/2016', function () {
                mockScope.data.holdingType = "Leasehold";
                mockScope.data.propertyType = "Residential";
                mockScope.data.effectiveDate = new Date(2016, 3, 1);
                expect(mockScope.displayAdditionalProperty()).toEqual(true);
            });

            it('displayAdditionalProperty should be false when Leasehold Non-residential and date is 01/04/2016', function () {
                mockScope.data.holdingType = "Leasehold";
                mockScope.data.propertyType = "Non-residential";
                mockScope.data.effectiveDate = new Date(2016, 3, 1);
                expect(mockScope.displayAdditionalProperty()).toEqual(false);
            });


        });

    });

}());
