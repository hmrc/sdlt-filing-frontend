(function() {
    'use strict';

    require("calc-module");

    var mocks = require("angular-mocks-wrapper");

    describe('Current Value Controller', function () {

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

            mockScope.data = {
                holdingType : "leasehold"
            };

            mockDataService = {
                getModel : function() {}
            };

            mockNavigationService = {
            };


            spyOn(mockDataService, 'getModel');


            mockValidationService = {};

            controller = $controller('currentValueController', {
                $scope : mockScope,
                $location : {},
                dataService : mockDataService,
                currentValueValidationService : mockValidationService,
                navigationService : mockNavigationService
            });
        }));

        it('should make 1 call to dataService.getModel', function () {
            expect(mockDataService.getModel.calls.count()).toEqual(1);
        });

        it('should default the state.hasError to ""', function () {
            expect(mockScope.state.hasError()).toEqual('');
        });

        describe('Calling .ftbLimit() on the Current Value Controller when the date is onOrAfter 23/09/2022 but before 01/04/2025', function() {

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

                controller = $controller('currentValueController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    currentValueValidationService : mockValidationService,
                    navigationService : mockNavigationService
                });

                mockScope.data = {
                    holdingType: "leasehold"
                };

            }));
            it('should return 625,000', function () {
                mockScope.data.effectiveDate = new Date(2025,1,23);
                expect(mockScope.ftbLimit()).toEqual("625,000");
            });
        });

        describe('Calling .ftbLimit() on the Current Value Controller when the date is onOrAfter 01/04/2025', function() {

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

                        controller = $controller('currentValueController', {
                            $scope : mockScope,
                            $location : {},
                            dataService : mockDataService,
                            currentValueValidationService : mockValidationService,
                            navigationService : mockNavigationService
                        });

                        mockScope.data = {
                            holdingType: "leasehold"
                        };

                    }));
                    it('should return 500,000', function () {
                        mockScope.data.effectiveDate = new Date(2025,3,1);
                        expect(mockScope.ftbLimit()).toEqual("500,000");
                    });
                });

        describe('Calling .ftbLimit() on the Current Value Controller when the date is before 23/09/2022', function() {

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

                controller = $controller('currentValueController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    currentValueValidationService : mockValidationService,
                    navigationService : mockNavigationService
                });

                mockScope.data = {
                    holdingType : "leasehold"
                };

            }));
            it('should return 500,000', function () {
                mockScope.data.effectiveDate = new Date(2022,8,22);
                expect(mockScope.ftbLimit()).toEqual("500,000");
            });
        });

        describe('Calling .submit() on the Current Value Controller with invalid data', function () {

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

                controller = $controller('currentValueController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    currentValueValidationService : mockValidationService,
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


        describe('Calling .submit() on the Current Value Controller with valid data', function () {

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

                controller = $controller('currentValueController', {
                    $scope : mockScope,
                    $location : {},
                    dataService : mockDataService,
                    currentValueValidationService : mockValidationService,
                    navigationService : mockNavigationService
                });

                mockScope.data = {
                    holdingType : "leasehold"
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
    });
}());
