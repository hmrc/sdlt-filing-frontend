(function() {
    'use strict';

    require("calc-module");

    var mocks = require("angular-mocks");

    describe('Rent Controller', function () {
        
        var controller, 
            mockScope, 
            mockDataService, 
            mockValidationService, 
            mockNavigationService,
            mockLocation,
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
                }
            };

            mockNavigationService = { 
            };

            spyOn(mockDataService, 'getModel').and.callThrough();

            mockValidationService = {};

            controller = $controller('rentController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                rentValidationService : mockValidationService,
                navigationService : mockNavigationService
            });
        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });


        it('should default the state.hasError to ""', function () {
            expect(mockScope.state.hasError()).toEqual('');
        });

        describe('Calling .submit() on the Rent Controller with invalid data', function () {
            
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
                
                controller = $controller('rentController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    rentValidationService : mockValidationService,
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

        describe('Calling .submit() on the Rent Controller with valid data', function () {
            
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

                spyOn(mockDataService, 'updateModel').and.callThrough();    
                spyOn(mockNavigationService, 'next').and.callThrough();
                spyOn(mockValidationService, 'validate').and.callThrough();                
                controller = $controller('rentController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    rentValidationService : mockValidationService,
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
        });

        describe('Calling .submit() on the Rent Controller with missing questions', function () {

            beforeEach(mocks.inject(function ($controller, $rootScope, $location) {
                
                mockScope = $rootScope.$new();
                mockScope.getHelpSetup = function() {return true;};
                                
                mockDataService = { 
                    getModel : function() { 
                        return {
                            holdingType : undefined,
                            leaseTerm : "banana"
                        }; 
                    },
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
                mockLocation = $location;

                spyOn(mockDataService, 'updateModel').and.callThrough();    
                spyOn(mockNavigationService, 'next').and.callThrough();
                spyOn(mockValidationService, 'validate').and.callThrough();

                controller = $controller('rentController', {
                    $scope : mockScope,
                    $location : mockLocation,
                    dataService : mockDataService,
                    rentValidationService : mockValidationService,
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

            it('should set the location path to /summary', function() {
                expect(mockLocation.path()).toEqual('/summary');
            });
        });
    });
}());
