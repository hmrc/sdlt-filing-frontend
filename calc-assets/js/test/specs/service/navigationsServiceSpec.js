(function () {
    describe('Navigation Service', function () {

        var service;

        beforeEach(angular.mock.module("calc.services"));
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

            it('should set the location path to /summary when holdingType has not been answered nd effective date is 31/03/2016', function() {
                data = { effectiveDateDay   : "31",
                         effectiveDateMonth : "03",
                         effectiveDateYear  : "2016"};
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/summary');
            });

            it('should set the location path to /purchaser when propertyType is "Residential" and holdingType is "Leasehold" and effective date is 01/04/2016', function() {
                data = { propertyType       : 'Residential',
                         holdingType        : 'Leasehold',
                         effectiveDateDay   : "01",
                         effectiveDateMonth : "04",
                         effectiveDateYear  : "2016"};
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/purchaser');
            });

            it('should set the location path to /purchaser when propertyType is "Residential" and holdingType is "Freehold" and effective date is 01/04/2016', function() {
                data = { propertyType       : 'Residential',
                         holdingType        : 'Freehold',
                         effectiveDateDay   : "01",
                         effectiveDateMonth : "04",
                         effectiveDateYear  : "2016"};
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/purchaser');
            });

            it('should set the location path to /lease-dates when propertyType is "Residential" and holdingType is "Leasehold" and effective date is 31/03/2016', function() {
                data = { propertyType       : 'Residential',
                         holdingType        : 'Leasehold',
                         effectiveDateDay   : "31",
                         effectiveDateMonth : "03",
                         effectiveDateYear  : "2016"};
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/lease-dates');
            });

            it('should set the location path to /purchase-price when propertyType is "Residential" and holdingType is "Freehold" and effective date is 31/03/2016', function() {
                data = { propertyType       : 'Residential',
                         holdingType        : 'Freehold',
                         effectiveDateDay   : "31",
                         effectiveDateMonth : "03",
                         effectiveDateYear  : "2016"};
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/purchase-price');
            });



            it('should set the location path to /lease-dates when propertyType is "Non-residential" and holdingType is "Leasehold" and effective date is 01/04/2016', function() {
                data = { propertyType       : 'Non-residential',
                         holdingType        : 'Leasehold',
                         effectiveDateDay   : "01",
                         effectiveDateMonth : "04",
                         effectiveDateYear  : "2016"};
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/lease-dates');
            });

            it('should set the location path to /purchase-price when propertyType is "Non-residential" and holdingType is "Freehold" and effective date is 01/04/2016', function() {
                data = { propertyType       : 'Non-residential',
                         holdingType        : 'Freehold',
                         effectiveDateDay   : "01",
                         effectiveDateMonth : "04",
                         effectiveDateYear  : "2016"};
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/purchase-price');
            });

            it('should set the location path to /lease-dates when propertyType is "Non-residential" and holdingType is "Leasehold" and effective date is 31/03/2016', function() {
                data = { propertyType       : 'Non-residential',
                         holdingType        : 'Leasehold',
                         effectiveDateDay   : "31",
                         effectiveDateMonth : "03",
                         effectiveDateYear  : "2016"};
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/lease-dates');
            });

            it('should set the location path to /purchase-price when propertyType is "Non-residential" and holdingType is "Freehold" and effective date is 31/03/2016', function() {
                data = { propertyType       : 'Non-residential',
                         holdingType        : 'Freehold',
                         effectiveDateDay   : "31",
                         effectiveDateMonth : "03",
                         effectiveDateYear  : "2016"};
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/purchase-price');
            });
        });



        describe('Calling .next() from the purchaser view', function() {
            var mockLocation,
                currentView = 'purchaser';
                
            beforeEach(inject(function($location) {
                mockLocation = $location;
                spyOn(mockLocation, 'path').and.callThrough();
            }));

            it('should set the location path to /additional-property when individual is Yes', function() {
                data = { holdingType : "Freehold",
                         individual : "Yes"};
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/additional-property');
            });

            it('should set the location path to /purchase-price when individual is No and property type is Freehold', function() {
                data = { holdingType : "Freehold",
                         individual : "No"};
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/purchase-price');
            });

            it('should set the location path to /lease-dates when individual is No and property type is Leasehold', function() {
                data = { holdingType : "Leasehold",
                         individual : "No"};
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/lease-dates');
            });
        });

        describe('Calling .next() from the additional-property view', function() {
            var mockLocation,
                currentView = 'additional-property';
                
            beforeEach(inject(function($location) {
                mockLocation = $location;
                spyOn(mockLocation, 'path').and.callThrough();
            }));

            it('should set the location path to /summary when holdingType has not been answered', function() {
                service.next(currentView, {}, mockLocation);
                expect(mockLocation.path()).toEqual('/summary');
            });

            it('should set the location path to /purchase-price when holdingType is "Freehold"', function() {
                data = { holdingType : 'Freehold' };
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/purchase-price');
            });

            it('should set the location path to /lease-dates when holdingType is "Leasehold"', function() {
                data = { holdingType : 'Leasehold' };
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/lease-dates');
            });

            it('should redirect based on holding type when property type is non-residential', function() {
                data = {
                  holdingType : 'Freehold',
                  propertyType : 'Non-residential',
                  effectiveDate : new Date('November 22, 2017'),
                  individual : "Yes",
                  twoOrMoreProperties : "No"
                };
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/purchase-price');
            });

            it('should redirect based on holding type when effective date is before 22/11/2017', function() {
                data = {
                  holdingType : 'Freehold',
                  propertyType : 'Residential',
                  effectiveDate : new Date('November 21, 2017'),
                  individual : "Yes",
                  twoOrMoreProperties : "No"
                };
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/purchase-price');
            });

            it('should redirect based on holding type when effective date is after 30/11/2019', function() {
                data = {
                  holdingType : 'Freehold',
                  propertyType : 'Residential',
                  effectiveDate : new Date('December 1, 2019'),
                  individual : "Yes",
                  twoOrMoreProperties : "No"
                };
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/purchase-price');
            });

            it('should redirect based on holding type when purchaser is not an individual', function() {
                data = {
                  holdingType : 'Freehold',
                  propertyType : 'Residential',
                  effectiveDate : new Date('November 22, 2017'),
                  individual : "No",
                  twoOrMoreProperties : "No"
                };
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/purchase-price');
            });

            it('should redirect based on holding type when purchase results in two or more properties', function() {
                data = {
                  holdingType : 'Freehold',
                  propertyType : 'Residential',
                  effectiveDate : new Date('November 22, 2017'),
                  individual : "Yes",
                  twoOrMoreProperties : "Yes"
                };
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/purchase-price');
            });

            it('should redirect to fist-time-buyer page when all ftb criteria are met (earliest date)', function() {
                data = {
                  holdingType : 'Freehold',
                  propertyType : 'Residential',
                  effectiveDate : new Date('November 22, 2017'),
                  individual : "Yes",
                  twoOrMoreProperties : "No"
                };
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/first-time-buyer');
            });

            it('should redirect to fist-time-buyer page when all ftb criteria are met (latest date)', function() {
                data = {
                  holdingType : 'Freehold',
                  propertyType : 'Residential',
                  effectiveDate : new Date('November 30, 2019'),
                  individual : "Yes",
                  twoOrMoreProperties : "No"
                };
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/first-time-buyer');
            });
        });

        describe('Calling .next() from the first-time-buyer view', function() {
            var mockLocation,
                currentView = 'first-time-buyer',
                data = {};

            beforeEach(inject(function($location) {
                mockLocation = $location;
                service.next(currentView, data, mockLocation);
            }));

            it('should set the location path to /summary when holdingType has not been answered', function() {
                service.next(currentView, {}, mockLocation);
                expect(mockLocation.path()).toEqual('/summary');
            });

            it('should set the location path to /purchase-price when holdingType is "Freehold"', function() {
                data = { holdingType : 'Freehold' };
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/purchase-price');
            });

            it('should set the location path to /lease-dates when holdingType is "Leasehold"', function() {
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

            it('should set the location path to /summary when propertyType is "Residential"', function() {
                data = { propertyType : 'Residential'};
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/summary');
            });

            it('should set the location path to /summary when propertyType is "Non-residential" and premium = "150000" and any rent < "2000"', function() {
                data = { propertyType : 'Non-residential' , premium : '150000', year1Rent: '1999' };
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/summary');
            });

            it('should set the location path to /summary when propertyType is "Non-residential" and premium < "150000" and any rent = "2000"', function() {
                data = { propertyType : 'Non-residential' , premium : '149999', year1Rent: '2000' };
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/summary');
            });

            it('should set the location path to /relevant-rent when propertyType is "Non-residential" and premium < "150000" and all rents < "2000"', function() {
                data = { propertyType : 'Non-residential' , premium : '149999', year1Rent: '1999' };
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/relevant-rent');
            });

            it('should set the location path to /relevant-rent when propertyType is "Non-residential" and premium < "150000" and all rents < "2000"', function() {
                data = { propertyType : 'Non-residential' , premium : '149999', year1Rent: '1999' , year2Rent: '1999' , year3Rent: '1999' , year4Rent: '1999' , year5Rent: '1999' };
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/relevant-rent');
            });

            it('should set the location path to /relevant-rent when propertyType is "Non-residential" and premium < "150000" and all rents < "2000"', function() {
                var effectiveDate = new Date(2016,2,17);
                data = { propertyType : 'Non-residential' , premium : '149999', year1Rent: '1999' , year2Rent: '1999' , year3Rent: '1999' , year4Rent: '1999' , year5Rent: '1999', effectiveDate : effectiveDate };
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/exchange-contracts');
            });
            
        });

        describe('Calling .next() from the exchange-contracts view', function() {
            var mockLocation,
                currentView = 'exchange-contracts',
                data = {};

            beforeEach(inject(function($location) {
                mockLocation = $location;
                service.next(currentView, data, mockLocation);
            }));

           it('should set the location path to /summary', function() {
                expect(mockLocation.path()).toEqual('/summary');
            });

            it('should set the location path to /summary when contractPre201603 = No', function() {
                data = { contractPre201603 : 'No'};
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/summary');
            });

            it('should set the location path to /relevant-rent when contractPre201603 = Yes, contractVariedPost201603 = No', function() {
                data = { contractPre201603 : 'Yes', contractVariedPost201603 : 'No'};
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/relevant-rent');
            });

            it('should set the location path to /summary when contractPre201603 = Yes, contractVariedPost201603 = Yes', function() {
                data = { contractPre201603 : 'Yes', contractVariedPost201603 : 'Yes'};
                service.next(currentView, data, mockLocation);
                expect(mockLocation.path()).toEqual('/summary');
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

        describe('calling startNow()', function() {
            var mockLocation;

            beforeEach(inject(function($location) {
                mockLocation = $location;

                service.startNow(mockLocation);
            }));

            it('should set the location path to /holding', function() {
                expect(mockLocation.path()).toEqual('/holding');
            });
        });

        describe('calling printView()', function() {
            var mockLocation,
                currentView = 'result',
                data = {};

            beforeEach(inject(function($location) {
                mockLocation = $location;

                service.printView(data,mockLocation);
            }));

            it('should set the location path to /print', function() {
                expect(mockLocation.path()).toEqual('/print');
            });
        });

        describe('calling viewDetails()', function() {
            var mockLocation,
                currentView = 'result',
                data = {};

            beforeEach(inject(function($location) {
                mockLocation = $location;

                service.viewDetails(data,mockLocation);
            }));

            it('should set the location path to /detail', function() {
                expect(mockLocation.path()).toEqual('/detail');
            });
        });
        
    });
}());
