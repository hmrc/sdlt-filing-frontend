(function() {
    'use strict';

    describe('Testing Routes', function() {

        // load the controller's module
        beforeEach(angular.mock.module("sdltc.routes"));

        var location, route, rootScope;

        beforeEach(inject(function(_$location_, _$route_, _$rootScope_) {
            location = _$location_;
            route = _$route_;
            rootScope = _$rootScope_;
        }));

        describe('holding route', function() {
            beforeEach(inject(
                function($httpBackend) {
                    $httpBackend.expectGET('holding.html')
                        .respond(200);
                }));

            it('should load the holding page on successful load of /holding', function() {
                location.path('/holding');
                rootScope.$digest();
                expect(route.current.controller).toBe('holdingController');
            });
        });

        describe('property route', function() {
            beforeEach(inject(
                function($httpBackend) {
                    $httpBackend.expectGET('property.html')
                        .respond(200);
                }));

            it('should load the property page on successful load of /property', function() {
                location.path('/property');
                rootScope.$digest();
                expect(route.current.controller).toBe('propertyController');
            });
        });

        describe('date route', function() {
            beforeEach(inject(
                function($httpBackend) {
                    $httpBackend.expectGET('date.html')
                        .respond(200);
                }));

            it('should load the date page on successful load of /date', function() {
                location.path('/date');
                rootScope.$digest();
                expect(route.current.controller).toBe('dateController');
            });
        });

        describe('purchase-price route', function() {
            beforeEach(inject(
                function($httpBackend) {
                    $httpBackend.expectGET('purchase-price.html')
                        .respond(200);
                }));

            it('should load the purchase-price page on successful load of /purchase-price', function() {
                location.path('/purchase-price');
                rootScope.$digest();
                expect(route.current.controller).toBe('purchasePriceController');
            });
        });

        describe('lease-dates route', function() {
            beforeEach(inject(
                function($httpBackend) {
                    $httpBackend.expectGET('lease-dates.html')
                        .respond(200);
                }));

            it('should load the lease-dates page on successful load of /lease-dates', function() {
                location.path('/lease-dates');
                rootScope.$digest();
                expect(route.current.controller).toBe('leaseDatesController');
            });
        });

        describe('premium route', function() {
            beforeEach(inject(
                function($httpBackend) {
                    $httpBackend.expectGET('premium.html')
                        .respond(200);
                }));

            it('should load the premium page on successful load of /premium', function() {
                location.path('/premium');
                rootScope.$digest();
                expect(route.current.controller).toBe('premiumController');
            });
        });

        describe('rent route', function() {
            beforeEach(inject(
                function($httpBackend) {
                    $httpBackend.expectGET('rent.html')
                        .respond(200);
                }));

            it('should load the rent page on successful load of /rent', function() {
                location.path('/rent');
                rootScope.$digest();
                expect(route.current.controller).toBe('rentController');
            });
        });

        describe('relevant-rent route', function() {
            beforeEach(inject(
                function($httpBackend) {
                    $httpBackend.expectGET('relevant-rent.html')
                        .respond(200);
                }));

            it('should load the relevant-rent page on successful load of /relevant-rent', function() {
                location.path('/relevant-rent');
                rootScope.$digest();
                expect(route.current.controller).toBe('relevantRentController');
            });
        });

        describe('summary route', function() {
            beforeEach(inject(
                function($httpBackend) {
                    $httpBackend.expectGET('summary.html')
                        .respond(200);
                }));

            it('should load the summary page on successful load of /summary', function() {
                location.path('/summary');
                rootScope.$digest();
                expect(route.current.controller).toBe('summaryController');
            });
        });

        describe('result route', function() {
            beforeEach(inject(
                function($httpBackend) {
                    $httpBackend.expectGET('summary.html')
                        .respond(200);
                }));

            it('should load the summary page on load of /result with no data', function() {
                location.path('/summary');
                rootScope.$digest();
                expect(route.current.controller).toBe('summaryController');
            });
        });

        describe('not found route', function() {
            beforeEach(inject(
                function($httpBackend) {
                    $httpBackend.expectGET('holding.html')
                        .respond(200);
                }));

            it('should load the holding page on successful load of /holding', function() {
                location.path('/test');
                rootScope.$digest();
                expect(route.current.controller).toBe('holdingController');
            });
        });

    });
}());