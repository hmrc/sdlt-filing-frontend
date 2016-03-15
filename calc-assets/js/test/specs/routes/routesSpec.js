(function() {
    'use strict';

    describe('Testing Routes', function() {

        // load the controller's module
        beforeEach(angular.mock.module("calc.routes"));

        var location, route, rootScope;

        beforeEach(inject(function(_$location_, _$route_, _$rootScope_) {
            location = _$location_;
            route = _$route_;
            rootScope = _$rootScope_;
        }));

        describe('intro route', function() {
            beforeEach(inject(
                function($httpBackend) {
                    $httpBackend.expectGET('intro.html')
                        .respond(200);
                }));

            it('should load the intro page on successful load of /intro', function() {
                location.path('/intro');
                rootScope.$digest();
                expect(route.current.controller).toBe('introController');
                expect(route.current.title).toBe('Calculate Stamp Duty Land Tax (SDLT)');                
            });
        });

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
                expect(route.current.title).toBe('Freehold or leasehold');
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
                expect(route.current.title).toBe('Residential or non-residential');
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
                expect(route.current.title).toBe('Effective date of transaction');
            });
        });

        describe('additional property route', function() {
            beforeEach(inject(
                function($httpBackend) {
                    $httpBackend.expectGET('additional-property.html')
                        .respond(200);
                }));

            it('should load the additional property page on successful load of /additional-property', function() {
                location.path('/additional-property');
                rootScope.$digest();
                expect(route.current.controller).toBe('additionalPropertyController');
                expect(route.current.title).toBe('Additional residential properties');
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
                expect(route.current.title).toBe('Purchase price');
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
                expect(route.current.title).toBe('Lease dates');
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
                expect(route.current.title).toBe('Lease premium');
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
                expect(route.current.title).toBe('Rent');
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
                expect(route.current.title).toBe('Relevant rental figure');
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
                expect(route.current.title).toBe('Check your answers');
            });
        });

        describe('result route', function() {
            beforeEach(inject(
                function($httpBackend) {
                    $httpBackend.expectGET('result.html')
                        .respond(200);
                }));

            it('should load the result page on load of /result', function() {
                location.path('/result');
                rootScope.$digest();
                expect(route.current.controller).toBe('resultController');
                expect(route.current.title).toBe('Result');
            });
        });

        describe('detail route', function() {
            beforeEach(inject(
                function($httpBackend) {
                    $httpBackend.expectGET('detail.html')
                        .respond(200);
                }));

            it('should load the detail page on load of /detail with no data', function() {
                location.path('/detail');
                rootScope.$digest();
                expect(route.current.controller).toBe('detailController');
                expect(route.current.title).toBe('Detailed calculation');
            });
        });

        describe('print route', function() {
            beforeEach(inject(
                function($httpBackend) {
                    $httpBackend.expectGET('print.html')
                        .respond(200);
                }));

            it('should load the print page on load of /print with no data', function() {
                location.path('/print');
                rootScope.$digest();
                expect(route.current.controller).toBe('printController');
                expect(route.current.title).toBe('Summary');
            });
        });

        describe('not found route', function() {
            beforeEach(inject(
                function($httpBackend) {
                    $httpBackend.expectGET('intro.html')
                        .respond(200);
                }));

            it('should load the intro page on successful load of /intro', function() {
                location.path('/test');
                rootScope.$digest();
                expect(route.current.controller).toBe('introController');
                expect(route.current.title).toBe('Calculate Stamp Duty Land Tax (SDLT)');
            });
        });

        describe('route changed successfully', function(){
            beforeEach(inject(
                function($httpBackend) {
                    $httpBackend.expectGET('intro.html')
                        .respond(200);
                }));
            it('should change the title of the rootScope to the title of the current route', function() {
                location.path('/intro');
                rootScope.$digest();
                rootScope.$broadcast('$routeChangeSuccess', route.current);
                expect(rootScope.title).toBe('Calculate Stamp Duty Land Tax (SDLT)');
            });
        });
    });
}());