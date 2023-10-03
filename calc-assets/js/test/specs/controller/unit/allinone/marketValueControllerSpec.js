(function() {
    'use strict';

    require("calc-module");

    var mocks = require("angular").mock;

    describe('Market Value Controller', function () {

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
                getModel : function() {}
            };

            mockNavigationService = {
            };

            spyOn(mockDataService, 'getModel');

            mockValidationService = {};

            controller = $controller('marketValueController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                marketValueValidationService : mockValidationService,
                navigationService : mockNavigationService
            });
        }));


        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });


        it('should default the state.hasError to ""', function () {
            expect(mockScope.state.hasError()).toEqual('');
        });

        describe('Calling .submit() on the Market Value Controller with invalid data', function () {

            beforeEach(mocks.inject(function ($controller, $rootScope) {

                mockScope = $rootScope.$new();
                mockScope.getHelpSetup = function() {return true;};

                mockDataService = {
                    getModel : function() {},
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

                controller = $controller('marketValueController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    marketValueValidationService : mockValidationService,
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

        describe('Calling .submit() on the Market Value Controller with invalid Using market value election data', function () {

            beforeEach(mocks.inject(function ($controller, $rootScope) {

                mockScope = $rootScope.$new();
                mockScope.getHelpSetup = function() {return true;};

                mockDataService = {
                    getModel : function() {},
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

                controller = $controller('marketValueController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    marketValueValidationService : mockValidationService,
                    navigationService : mockNavigationService
                });

                mockScope.data = {
                    paySDLT : "Using market value election"
                };

                mockScope.submit({});
            }));

            it('should call the validation service once', function () {
                expect(mockValidationService.validate.calls.count()).toEqual(1);
            });

            it('should call dataService.updateModel once', function () {
                expect(mockDataService.updateModel.calls.count()).toEqual(0);
            });

            it('should call to navigationService.next once', function () {
                expect(mockNavigationService.next.calls.count()).toEqual(0);
            });
        });

        describe('Calling .submit() on the Market Value Controller with valid Using market value election data', function () {

            beforeEach(mocks.inject(function ($controller, $rootScope) {

                mockScope = $rootScope.$new();
                mockScope.getHelpSetup = function() {return true;};

                mockDataService = {
                    getModel : function() {},
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


                spyOn(mockDataService, 'updateModel');
                spyOn(mockNavigationService, 'next');
                spyOn(mockValidationService, 'validate').and.callThrough();

                controller = $controller('marketValueController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    marketValueValidationService : mockValidationService,
                    navigationService : mockNavigationService
                });

                mockScope.data = {
                    paySDLT : "Using market value election",
                    marketPropValue: "250000"
                };

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

        describe('Calling .submit() on the Market Value Controller with invalid marketPropValue data', function () {

            beforeEach(mocks.inject(function ($controller, $rootScope) {

                mockScope = $rootScope.$new();
                mockScope.getHelpSetup = function() {return true;};

                mockDataService = {
                    getModel : function() {},
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

                controller = $controller('marketValueController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    marketValueValidationService : mockValidationService,
                    navigationService : mockNavigationService
                });

                mockScope.data = {
                    marketPropValue: "100000"
                };

                mockScope.submit({});
            }));

            it('should call the validation service once', function () {
                expect(mockValidationService.validate.calls.count()).toEqual(1);
            });

            it('should call dataService.updateModel once', function () {
                expect(mockDataService.updateModel.calls.count()).toEqual(0);
            });

            it('should call to navigationService.next once', function () {
                expect(mockNavigationService.next.calls.count()).toEqual(0);
            });
        });

        describe('Calling .submit() on the Market Value Controller with invalid Stages data', function () {

            beforeEach(mocks.inject(function ($controller, $rootScope) {

                mockScope = $rootScope.$new();
                mockScope.getHelpSetup = function() {return true;};

                mockDataService = {
                    getModel : function() {},
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

                controller = $controller('marketValueController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    marketValueValidationService : mockValidationService,
                    navigationService : mockNavigationService
                });

                mockScope.data = {
                    paySDLT: "Stages"
                };

                mockScope.submit({});
            }));

            it('should call the validation service once', function () {
                expect(mockValidationService.validate.calls.count()).toEqual(1);
            });

            it('should call dataService.updateModel once', function () {
                expect(mockDataService.updateModel.calls.count()).toEqual(0);
            });

            it('should call to navigationService.next once', function () {
                expect(mockNavigationService.next.calls.count()).toEqual(0);
            });
        });

        describe('Calling .submit() on the Market Value Controller with valid Stages data', function () {

            beforeEach(mocks.inject(function ($controller, $rootScope) {

                mockScope = $rootScope.$new();
                mockScope.getHelpSetup = function() {return true;};

                mockDataService = {
                    getModel : function() {},
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


                spyOn(mockDataService, 'updateModel');
                spyOn(mockNavigationService, 'next');
                spyOn(mockValidationService, 'validate').and.callThrough();

                controller = $controller('marketValueController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    marketValueValidationService : mockValidationService,
                    navigationService : mockNavigationService
                });

                mockScope.data = {
                    paySDLT : "Stages",
                    stagePropValue: "50000"
                };

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

        describe('Calling .submit() on the Market Value Controller with invalid StagePropValue data', function () {

            beforeEach(mocks.inject(function ($controller, $rootScope) {

                mockScope = $rootScope.$new();
                mockScope.getHelpSetup = function() {return true;};

                mockDataService = {
                    getModel : function() {},
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

                controller = $controller('marketValueController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    marketValueValidationService : mockValidationService,
                    navigationService : mockNavigationService
                });

                mockScope.data = {
                    stagePropValue: "100000"
                };

                mockScope.submit({});
            }));

            it('should call the validation service once', function () {
                expect(mockValidationService.validate.calls.count()).toEqual(1);
            });

            it('should call dataService.updateModel once', function () {
                expect(mockDataService.updateModel.calls.count()).toEqual(0);
            });

            it('should call to navigationService.next once', function () {
                expect(mockNavigationService.next.calls.count()).toEqual(0);
            });
        });
    });
}());

