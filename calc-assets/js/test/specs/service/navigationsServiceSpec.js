(function () {
    describe('Navigation Service', function () {

        var service;

        beforeEach(angular.mock.module("sdltc.services"));
        beforeEach(inject(function(_navigationService_) {
            service = _navigationService_;
        }));

        it('should be registered with angular', function () {
            expect(service).not.toBeUndefined();
        });

        describe("Calling .logView('start')", function() {
            
            var testPage = 'start',
                gaCalls = [],
                currentView = '';

            beforeEach(function() {
                ga = function(arg1, arg2, arg3) {
                    var call = [arg1, arg2, arg3];
                    gaCalls.push(call);
                };

                currentView = service.logView(testPage);
            });

            it('should call google analytics (GA) twice', function() {
                expect(gaCalls.length).toEqual(2);
            });

            it('should first call GA with the command "set"', function() {
                var call = gaCalls[0];
                var command = call[0];
                expect(command).toEqual('set');
            });

            it('should first call GA with the property "page"', function() {
                var call = gaCalls[0];
                var property = call[1];
                expect(property).toEqual('page');
            });

            it('should prefix the page name with the context URL before calling GA', function() {
                var call = gaCalls[0];
                var pageName = call[2];
                expect(pageName).toEqual('/calculate-stamp-duty-land-tax/' + testPage);
            });

            it('should make a second GA call with the command "send"', function() {
                var call = gaCalls[1];
                var command = call[0];
                expect(command).toEqual('send');
            });

            it('should make a second call to GA with the property "pageView"', function() {
                var call = gaCalls[1];
                var property = call[1];
                expect(property).toEqual('pageview');
            });

            it('should instruct GA to anonymise the IP address', function() {
                var call = gaCalls[1];
                var instruction = call[2];
                expect(instruction.anonymizeIp).toEqual(true);
            });

            it('should return a current view with the value "start"', function() {
                expect(currentView).toEqual('start');
            });
        });

        
        describe('Calling .next() from the holding view', function() {
            var mockLocation,
                currentView = 'holding',
                data = {};

            beforeEach(inject(function($location) {
                mockLocation = $location;
                service.next(currentView, data, mockLocation);
            }));

           it('should set the location path to /property', function() {
                expect(mockLocation.path()).toEqual('/property');
            });
        });


        describe('Calling .next() from the property view', function() {
            var mockLocation,
                currentView = 'property',
                data = {};

            beforeEach(inject(function($location) {
                mockLocation = $location;
                service.next(currentView, data, mockLocation);
            }));

           it('should set the location path to /date', function() {
                expect(mockLocation.path()).toEqual('/date');
            });
        });

        describe('Calling .next() from the date view', function() {
            var mockLocation,
                currentView = 'date';
                
            beforeEach(inject(function($location) {
                mockLocation = $location;
                spyOn(mockLocation, 'path').and.callThrough();
            }));

            it('should set the location path to /summary when holdingType has not been answered', function() {
                service.next(currentView, {}, mockLocation);
                expect(mockLocation.path()).toEqual('/summary');
            });

            it('should set the location path to /purchse-price when holdingType is "freehold"', function() {
                data = { holdingType : 'Freehold' };
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/purchase-price');
            });

            it('should set the location path to /summary when holdingType is "no"', function() {
                data = { holdingType : 'Leasehold' };
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/lease-dates');
            });
        });

        describe('Calling .next() from the purchase-price view', function() {
            var mockLocation,
                currentView = 'purchase-price',
                data = {};

            beforeEach(inject(function($location) {
                mockLocation = $location;
                service.next(currentView, data, mockLocation);
            }));

           it('should set the location path to /summary', function() {
                expect(mockLocation.path()).toEqual('/summary');
            });
        });

        describe('Calling .next() from the lease-dates view', function() {
            var mockLocation,
                currentView = 'lease-dates',
                data = {};

            beforeEach(inject(function($location) {
                mockLocation = $location;
                service.next(currentView, data, mockLocation);
            }));

           it('should set the location path to /premium', function() {
                expect(mockLocation.path()).toEqual('/premium');
            });
        });

        describe('Calling .next() from the premium view', function() {
            var mockLocation,
                currentView = 'premium',
                data = {};

            beforeEach(inject(function($location) {
                mockLocation = $location;
                service.next(currentView, data, mockLocation);
            }));

           it('should set the location path to /rent', function() {
                expect(mockLocation.path()).toEqual('/rent');
            });
        });

        describe('Calling .next() from the rent view', function() {
            var mockLocation,
                currentView = 'rent',
                data = {};

            beforeEach(inject(function($location) {
                mockLocation = $location;
                service.next(currentView, data, mockLocation);
            }));

            it('should set the location path to /summary', function() {
                expect(mockLocation.path()).toEqual('/summary');
            });

            it('should set the location path to /relevant-rent when propertyType is "Non-residential" and premium < "150000"', function() {
                data = { propertyType : 'Non-residential' , premium : '149000' };
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/relevant-rent');
            });
            
        });

        describe('Calling .next() from the relevant-rent view', function() {
            var mockLocation,
                currentView = 'relevant-rent',
                data = {};

            beforeEach(inject(function($location) {
                mockLocation = $location;
                service.next(currentView, data, mockLocation);
            }));

           it('should set the location path to /summary', function() {
                expect(mockLocation.path()).toEqual('/summary');
            });
        });

        describe('Calling .next() from the summary view', function() {
            var mockLocation,
                currentView = 'summary',
                data = {};

            beforeEach(inject(function($location) {
                mockLocation = $location;
                service.next(currentView, data, mockLocation);
            }));

            it('should set the location path to /result', function() {
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/result');
            });
        });
        
    });
}());
