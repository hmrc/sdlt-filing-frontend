(function() {
    'use strict';

    require("calc-module");

    var mocks = require("angular-mocks-wrapper");

    describe('Premium Controller', function () {
        
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
                getModel : function() {
                     return {
                        propertyType : "",
                        effectiveDate : ""
                    };
                }
            };

            mockNavigationService = { 
                logView : function() {} 
            };

            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            
            mockValidationService = {};

            controller = $controller('premiumController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                premiumValidationService : mockValidationService,
                navigationService : mockNavigationService
            });


        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });


        it('should default the state.hasError to ""', function () {
            expect(mockScope.state.hasError()).toEqual('');
        });

        it('should set showPremiumHelp to false', function () {
            expect(mockScope.showPremiumHelp).toEqual(false);
        });

        describe('Calling .submit() on the Premium Controller with invalid data', function () {
            
            beforeEach(mocks.inject(function ($controller, $rootScope) {
                
                mockScope = $rootScope.$new();
                mockScope.getHelpSetup = function() {return true;};
                
                mockDataService = {
                    getModel : function() {
                         return {
                            propertyType : undefined,
                            effectiveDate : undefined
                        };
                    },
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
                
                controller = $controller('premiumController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    premiumValidationService : mockValidationService,
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

            it('should set showPremiumHelp to false', function () {
                expect(mockScope.showPremiumHelp).toEqual(false);
            });
        });

        describe('Calling .submit() on the Premium Controller with valid data', function () {
            
            beforeEach(mocks.inject(function ($controller, $rootScope) {
                
                mockScope = $rootScope.$new();
                mockScope.getHelpSetup = function() {return true;};
                
                mockDataService = {
                    getModel : function() {
                         return {
                            propertyType : "",
                            effectiveDate : ""
                        };
                    },
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
                
                controller = $controller('premiumController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    premiumValidationService : mockValidationService,
                    navigationService : mockNavigationService
                });

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

            it('should set showPremiumHelp to false', function () {
                expect(mockScope.showPremiumHelp).toEqual(false);
            });
        });

        describe('showPremiumHelp should be true for Residential properties', function () {
            
            beforeEach(mocks.inject(function ($controller, $rootScope) {
                
                mockScope = $rootScope.$new();
                mockScope.getHelpSetup = function() {return true;};
                
                mockDataService = {
                    getModel : function() {
                         return {
                            propertyType : "Residential",
                            effectiveDate : ""
                        };
                    },
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
                
                controller = $controller('premiumController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    premiumValidationService : mockValidationService,
                    navigationService : mockNavigationService
                });

            }));

            it('should set showPremiumHelp to true', function () {
                expect(mockScope.showPremiumHelp).toEqual(true);
            });
        });

        describe('showPremiumHelp should be true for Non-residential properties after 16 March 2016', function () {
            
            beforeEach(mocks.inject(function ($controller, $rootScope) {
                
                mockScope = $rootScope.$new();
                mockScope.getHelpSetup = function() {return true;};
                
                mockDataService = {
                    getModel : function() {
                         return {
                            propertyType : "Non-residential",
                            effectiveDate : new Date(2016,2,17)
                        };
                    },
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
                
                controller = $controller('premiumController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    premiumValidationService : mockValidationService,
                    navigationService : mockNavigationService
                });

            }));

            it('should set showPremiumHelp to true', function () {
                expect(mockScope.showPremiumHelp).toEqual(true);
            });
        });


        describe('showPremiumHelp should be false for Non-residential properties before 17 March 2016', function () {
            
            beforeEach(mocks.inject(function ($controller, $rootScope) {
                
                mockScope = $rootScope.$new();
                mockScope.getHelpSetup = function() {return true;};
                
                mockDataService = {
                    getModel : function() {
                         return {
                            propertyType : "Non-residential",
                            effectiveDate : new Date(2016,2,16)
                        };
                    },
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
                
                controller = $controller('premiumController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    premiumValidationService : mockValidationService,
                    navigationService : mockNavigationService
                });

            }));

            it('should set showPremiumHelp to false', function () {
                expect(mockScope.showPremiumHelp).toEqual(false);
            });
        });

    });
}());
