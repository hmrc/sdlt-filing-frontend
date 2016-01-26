(function() {
    'use strict';

    describe('Calculation Service', function () {

        var service;

        beforeEach(angular.mock.module("calc.services"));

        beforeEach(inject(function (_calculationService_) {
            service = _calculationService_;

            slabResults = {
                rate : 0,
                taxDue : 0
            };

            resPremSliceResults = {
                "totalSDLT" : 0,
                "slices" : [
                    { "from": 0,       "to" : 125000,  "rate" : 0,  "taxDue" : 0},
                    { "from": 125000,  "to" : 250000,  "rate" : 2,  "taxDue" : 0},
                    { "from": 250000,  "to" : 925000,  "rate" : 5,  "taxDue" : 0},
                    { "from": 925000,  "to" : 1500000, "rate" : 10, "taxDue" : 0},
                    { "from": 1500000, "to" : -1,      "rate" : 12, "taxDue" : 0}
                ]
            };

            resLeaseSliceResults = {
                "totalSDLT" : 0,
                "slices" : [
                    { "from": 0,       "to" : 125000,  "rate" : 0,  "taxDue" : 0},
                    { "from": 125000,  "to" : -1    ,  "rate" : 1,  "taxDue" : 0}
                ]
            };

            nonResLeaseSliceResults = {
                "totalSDLT" : 0,
                "slices" : [
                    { "from": 0,       "to" : 150000,  "rate" : 0,  "taxDue" : 0},
                    { "from": 150000,  "to" : -1    ,  "rate" : 1,  "taxDue" : 0}
                ]
            };

            termOfLeaseResults = {
                years : 0,
                days : 0,
                daysInPartialYear : 0
            };

        }));

        // ********************* calculateResidentialPremiumSlab *********************

        it(' calculateResidentialPremiumSlab should return 0 for purchase price of 125000', function() {
            slabResults.rate = 0;
            slabResults.taxDue = 0;
            expect(service.calculateResidentialPremiumSlab(125000)).toEqual(slabResults);
        });

        it(' calculateResidentialPremiumSlab should return 1250 for purchase price of 125000.01', function() {
            slabResults.rate = 1;
            slabResults.taxDue = 1250;
            expect(service.calculateResidentialPremiumSlab(125000.01)).toEqual(slabResults);
        });

        it(' calculateResidentialPremiumSlab should return 2500 for purchase price of 250000.00', function() {
            slabResults.rate = 1;
            slabResults.taxDue = 2500;
            expect(service.calculateResidentialPremiumSlab(250000.00)).toEqual(slabResults);
        });

        it(' calculateResidentialPremiumSlab should return 7500 for purchase price of 250000.01', function() {
            slabResults.rate = 3;
            slabResults.taxDue = 7500;
            expect(service.calculateResidentialPremiumSlab(250000.01)).toEqual(slabResults);
        });

        it(' calculateResidentialPremiumSlab should return 15000 for purchase price of 500000', function() {
            slabResults.rate = 3;
            slabResults.taxDue = 15000;
            expect(service.calculateResidentialPremiumSlab(500000)).toEqual(slabResults);
        });

        it(' calculateResidentialPremiumSlab should return 20000 for purchase price of 500000.01', function() {
            slabResults.rate = 4;
            slabResults.taxDue = 20000;
            expect(service.calculateResidentialPremiumSlab(500000.01)).toEqual(slabResults);
        });

        it(' calculateResidentialPremiumSlab should return 40000 for purchase price of 1000000', function() {
            slabResults.rate = 4;
            slabResults.taxDue = 40000;
            expect(service.calculateResidentialPremiumSlab(1000000)).toEqual(slabResults);
        });

        it(' calculateResidentialPremiumSlab should return 50000 for purchase price of 1000000.01', function() {
            slabResults.rate = 5;
            slabResults.taxDue = 50000;
            expect(service.calculateResidentialPremiumSlab(1000000.01)).toEqual(slabResults);
        });

        it(' calculateResidentialPremiumSlab should return 100000 for purchase price of 2000000', function() {
            slabResults.rate = 5;
            slabResults.taxDue = 100000;
            expect(service.calculateResidentialPremiumSlab(2000000)).toEqual(slabResults);
        });

        it(' calculateResidentialPremiumSlab should return 140000 for purchase price of 2000000.01', function() {
            slabResults.rate = 7;
            slabResults.taxDue = 140000;
            expect(service.calculateResidentialPremiumSlab(2000000.01)).toEqual(slabResults);
        });

        // ********************* calculateResidentialPremiumSlice *********************

        it(' calculateResidentialPremiumSlice should return 0 for purchase price of 125000', function() {
            expect(service.calculateResidentialPremiumSlice(125000)).toEqual(resPremSliceResults);
        });

        //  £49 is taxed @ 2%, giving 98p which is rounded down
        it(' calculateResidentialPremiumSlice should return 0 for purchase price of 125049', function() {
            expect(service.calculateResidentialPremiumSlice(125049)).toEqual(resPremSliceResults);
        });

        //  £50 is taxed @ 2%, giving £1 due
        it(' calculateResidentialPremiumSlice should return 1 for purchase price of 125050', function() {
            resPremSliceResults.totalSDLT = 1;
            resPremSliceResults.slices[1].taxDue = 1;
            expect(service.calculateResidentialPremiumSlice(125050)).toEqual(resPremSliceResults);
        });

        it(' calculateResidentialPremiumSlice should return 2500 for purchase price of 250000', function() {
            resPremSliceResults.totalSDLT = 2500;
            resPremSliceResults.slices[1].taxDue = 2500;
            expect(service.calculateResidentialPremiumSlice(250000)).toEqual(resPremSliceResults);
        });

        // £19 is taxed @ 5%, giving 95p which is rounded down
        it(' calculateResidentialPremiumSlice should return 2500 for purchase price of 250019', function() {
            resPremSliceResults.totalSDLT = 2500;
            resPremSliceResults.slices[1].taxDue = 2500;
            expect(service.calculateResidentialPremiumSlice(250019)).toEqual(resPremSliceResults);
        });

        //  £20 is taxed @ 5%, giving £1 due
        it(' calculateResidentialPremiumSlice should return 2501 for purchase price of 250020', function() {
            resPremSliceResults.totalSDLT = 2501;
            resPremSliceResults.slices[1].taxDue = 2500;
            resPremSliceResults.slices[2].taxDue = 1;
            expect(service.calculateResidentialPremiumSlice(250020)).toEqual(resPremSliceResults);
        });

        it(' calculateResidentialPremiumSlice should return 36250 for purchase price of 925000', function() {
            resPremSliceResults.totalSDLT = 36250;
            resPremSliceResults.slices[1].taxDue = 2500;
            resPremSliceResults.slices[2].taxDue = 33750;
            expect(service.calculateResidentialPremiumSlice(925000)).toEqual(resPremSliceResults);
        });

        // £9 is taxed @ 10%, giving 90p which is rounded down
        it(' calculateResidentialPremiumSlice should return 36250 for purchase price of 925009', function() {
            resPremSliceResults.totalSDLT = 36250;
            resPremSliceResults.slices[1].taxDue = 2500;
            resPremSliceResults.slices[2].taxDue = 33750;
            expect(service.calculateResidentialPremiumSlice(925009)).toEqual(resPremSliceResults);
        });

        //  £10 is taxed @ 10%, giving £1 due
        it(' calculateResidentialPremiumSlice should return 36251 for purchase price of 925010', function() {
            resPremSliceResults.totalSDLT = 36251;
            resPremSliceResults.slices[1].taxDue = 2500;
            resPremSliceResults.slices[2].taxDue = 33750;
            resPremSliceResults.slices[3].taxDue = 1;
            expect(service.calculateResidentialPremiumSlice(925010)).toEqual(resPremSliceResults);
        });

        it(' calculateResidentialPremiumSlice should return 93750 for purchase price of 1500000', function() {
            resPremSliceResults.totalSDLT = 93750;
            resPremSliceResults.slices[1].taxDue = 2500;
            resPremSliceResults.slices[2].taxDue = 33750;
            resPremSliceResults.slices[3].taxDue = 57500;
            expect(service.calculateResidentialPremiumSlice(1500000)).toEqual(resPremSliceResults);
        });

        // £8 is taxed @ 12%, giving 96p which is rounded down
        it(' calculateResidentialPremiumSlice should return 93750 for purchase price of 1500008', function() {
            resPremSliceResults.totalSDLT = 93750;
            resPremSliceResults.slices[1].taxDue = 2500;
            resPremSliceResults.slices[2].taxDue = 33750;
            resPremSliceResults.slices[3].taxDue = 57500;
            expect(service.calculateResidentialPremiumSlice(1500008)).toEqual(resPremSliceResults);
        });

        // £9 is taxed @ 12%, giving 108p = £1 due
        it(' calculateResidentialPremiumSlice should return 93750 for purchase price of 1500009', function() {
            resPremSliceResults.totalSDLT = 93751;
            resPremSliceResults.slices[1].taxDue = 2500;
            resPremSliceResults.slices[2].taxDue = 33750;
            resPremSliceResults.slices[3].taxDue = 57500;
            resPremSliceResults.slices[4].taxDue = 1;
            expect(service.calculateResidentialPremiumSlice(1500009)).toEqual(resPremSliceResults);
        });

        it(' calculateResidentialPremiumSlice should return 1113749 for purchase price of 9999999', function() {
            resPremSliceResults.totalSDLT = 1113749;
            resPremSliceResults.slices[1].taxDue = 2500;
            resPremSliceResults.slices[2].taxDue = 33750;
            resPremSliceResults.slices[3].taxDue = 57500;
            resPremSliceResults.slices[4].taxDue = 1019999;
            expect(service.calculateResidentialPremiumSlice(9999999)).toEqual(resPremSliceResults);
        });

        it(' calculateResidentialPremiumSlice should return 119,913,749 for purchase price of 999,999,999', function() {
            resPremSliceResults.totalSDLT = 119913749;
            resPremSliceResults.slices[1].taxDue = 2500;
            resPremSliceResults.slices[2].taxDue = 33750;
            resPremSliceResults.slices[3].taxDue = 57500;
            resPremSliceResults.slices[4].taxDue = 119819999;
            expect(service.calculateResidentialPremiumSlice(999999999)).toEqual(resPremSliceResults);
        });

        // ********************* calculateNonResidentialPremiumSlab *********************
        it(' calculateNonResidentialPremiumSlab should return 0 for premium of 150000, relevant rent < 1000', function() {
            slabResults.rate = 0;
            slabResults.taxDue = 0;
            expect(service.calculateNonResidentialPremiumSlab(150000, true)).toEqual(slabResults);
        });

        it(' calculateNonResidentialPremiumSlab should return 1500 for premium of 150000, relevant rent >= 1000', function() {
            slabResults.rate = 1;
            slabResults.taxDue = 1500;
            expect(service.calculateNonResidentialPremiumSlab(150000, false)).toEqual(slabResults);
        });

        it(' calculateNonResidentialPremiumSlab should return 1500 for premium of 150001, relevant rent < 1000', function() {
            slabResults.rate = 1;
            slabResults.taxDue = 1500;
            expect(service.calculateNonResidentialPremiumSlab(150001, true)).toEqual(slabResults);
        });

        it(' calculateNonResidentialPremiumSlab should return 1500 for premium of 150001, relevant rent >= 1000', function() {
            slabResults.rate = 1;
            slabResults.taxDue = 1500;
            expect(service.calculateNonResidentialPremiumSlab(150001, false)).toEqual(slabResults);
        });

        it(' calculateNonResidentialPremiumSlab should return 2500 for premium of 250000, relevant rent < 1000', function() {
            slabResults.rate = 1;
            slabResults.taxDue = 2500;
            expect(service.calculateNonResidentialPremiumSlab(250000, true)).toEqual(slabResults);
        });

        it(' calculateNonResidentialPremiumSlab should return 2500 for premium of 250000, relevant rent >= 1000', function() {
            slabResults.rate = 1;
            slabResults.taxDue = 2500;
            expect(service.calculateNonResidentialPremiumSlab(250000, false)).toEqual(slabResults);
        });

        it(' calculateNonResidentialPremiumSlab should return 7500 for premium of 250001, relevant rent < 1000', function() {
            slabResults.rate = 3;
            slabResults.taxDue = 7500;
            expect(service.calculateNonResidentialPremiumSlab(250001, true)).toEqual(slabResults);
        });

        it(' calculateNonResidentialPremiumSlab should return 7500 for premium of 250001, relevant rent >= 1000', function() {
            slabResults.rate = 3;
            slabResults.taxDue = 7500;
            expect(service.calculateNonResidentialPremiumSlab(250001, false)).toEqual(slabResults);
        });

        it(' calculateNonResidentialPremiumSlab should return 15000 for premium of 500000, relevant rent < 1000', function() {
            slabResults.rate = 3;
            slabResults.taxDue = 15000;
            expect(service.calculateNonResidentialPremiumSlab(500000, true)).toEqual(slabResults);
        });

        it(' calculateNonResidentialPremiumSlab should return 15000 for premium of 500000, relevant rent >= 1000', function() {
            slabResults.rate = 3;
            slabResults.taxDue = 15000;
            expect(service.calculateNonResidentialPremiumSlab(500000, false)).toEqual(slabResults);
        });

        it(' calculateNonResidentialPremiumSlab should return 20000 for premium of 500001, relevant rent < 1000', function() {
            slabResults.rate = 4;
            slabResults.taxDue = 20000;
            expect(service.calculateNonResidentialPremiumSlab(500001, true)).toEqual(slabResults);
        });

        it(' calculateNonResidentialPremiumSlab should return 20000 for premium of 500001, relevant rent >= 1000', function() {
            slabResults.rate = 4;
            slabResults.taxDue = 20000;
            expect(service.calculateNonResidentialPremiumSlab(500001, false)).toEqual(slabResults);
        });

        // ********************* calculateResidentialLeaseSlice *********************
        it(' calculateResidentialLeaseSlice should return 0 for npv of 125099', function() {
            resLeaseSliceResults.totalSDLT = 0;
            resLeaseSliceResults.slices[1].taxDue = 0;
            expect(service.calculateResidentialLeaseSlice(125099)).toEqual(resLeaseSliceResults);
        });

        it(' calculateResidentialLeaseSlice should return 1 for npv of 125100', function() {
            resLeaseSliceResults.totalSDLT = 1;
            resLeaseSliceResults.slices[1].taxDue = 1;
            expect(service.calculateResidentialLeaseSlice(125100)).toEqual(resLeaseSliceResults);
        });

        // ********************* calculateNonResidentialLeaseSlice *********************
        it(' calculateNonResidentialLeaseSlice should return 0 for npv of 150099', function() {
            nonResLeaseSliceResults.totalSDLT = 0;
            nonResLeaseSliceResults.slices[1].taxDue = 0;
            expect(service.calculateNonResidentialLeaseSlice(150099)).toEqual(nonResLeaseSliceResults);
        });

        it(' calculateNonResidentialLeaseSlice should return 1 for npv of 150100', function() {
            nonResLeaseSliceResults.totalSDLT = 1;
            nonResLeaseSliceResults.slices[1].taxDue = 1;
            expect(service.calculateNonResidentialLeaseSlice(150100)).toEqual(nonResLeaseSliceResults);
        });


        // ********************* calculateNPV *********************
        it(' calculateNPV should return 9661 for 1 full year, 0 partial days, rents 10000,0,0,0,0 ', function() {
            var fullYears = 1;
            var partialDays = 0;
            var daysInPartialYear = 0;
            var rentsArray = [10000, 0, 0, 0, 0];
            expect(service.calculateNPV(fullYears, partialDays, daysInPartialYear, rentsArray)).toEqual(9661);
        });

        it(' calculateNPV should return 19930 for 2 full years, 0 partial days, rents 10000,11000,0,0,0 ', function() {
            var fullYears = 2;
            var partialDays = 0;
            var daysInPartialYear = 0;
            var rentsArray = [10000, 11000, 0, 0, 0];
            expect(service.calculateNPV(fullYears, partialDays, daysInPartialYear, rentsArray)).toEqual(19930);
        });

        it(' calculateNPV should return 19930 for 3 full years, 0 partial days, rents 10000,11000,12000,0,0 ', function() {
            var fullYears = 3;
            var partialDays = 0;
            var daysInPartialYear = 0;
            var rentsArray = [10000, 11000, 12000, 0, 0];
            expect(service.calculateNPV(fullYears, partialDays, daysInPartialYear, rentsArray)).toEqual(30753);
        });

        it(' calculateNPV should return 42082 for 4 full years, 0 partial days, rents 10000,11000,12000,13000,0 ', function() {
            var fullYears = 4;
            var partialDays = 0;
            var daysInPartialYear = 0;
            var rentsArray = [10000, 11000, 12000, 13000, 0];
            expect(service.calculateNPV(fullYears, partialDays, daysInPartialYear, rentsArray)).toEqual(42082);
        });

        it(' calculateNPV should return 53870 for 4 full years, 0 partial days, rents 10000,11000,12000,13000,140000 ', function() {
            var fullYears = 5;
            var partialDays = 0;
            var daysInPartialYear = 0;
            var rentsArray = [10000, 11000, 12000, 13000, 14000];
            expect(service.calculateNPV(fullYears, partialDays, daysInPartialYear, rentsArray)).toEqual(53870);
        });

        it(' calculateNPV should return 65259 for 6 full years, 0 partial days, rents 10000,11000,12000,13000,140000 ', function() {
            var fullYears = 6;
            var partialDays = 0;
            var daysInPartialYear = 0;
            var rentsArray = [10000, 11000, 12000, 13000, 14000];
            expect(service.calculateNPV(fullYears, partialDays, daysInPartialYear, rentsArray)).toEqual(65259);
        });

        it(' calculateNPV should return 377,835 for 100 full years, 0 partial days, rents 10000,11000,12000,13000,140000 ', function() {
            var fullYears = 100;
            var partialDays = 0;
            var daysInPartialYear = 0;
            var rentsArray = [10000, 11000, 12000, 13000, 14000];
            expect(service.calculateNPV(fullYears, partialDays, daysInPartialYear, rentsArray)).toEqual(377835);
        });

        it(' calculateNPV should return 54834 for 5 full years, 31/366 partial days, rents 10000,11000,12000,13000,140000 ', function() {
            var fullYears = 5;
            var partialDays = 31;
            var daysInPartialYear = 366;
            var rentsArray = [10000, 11000, 12000, 13000, 14000];
            expect(service.calculateNPV(fullYears, partialDays, daysInPartialYear, rentsArray)).toEqual(54834);
        });

        it(' calculateNPV should return 272680 for 35 full years, 181/365 partial days, rents 10000,11000,12000,13000,140000 ', function() {
            var fullYears = 35;
            var partialDays = 181;
            var daysInPartialYear = 365;
            var rentsArray = [10000, 11000, 12000, 13000, 14000];
            expect(service.calculateNPV(fullYears, partialDays, daysInPartialYear, rentsArray)).toEqual(272680);
        });

        it(' calculateNPV should return 237461 for 1 full years, 1/365 partial days, rents 125000, 125000, 0, 0, 0', function() {
            var fullYears = 1;
            var partialDays = 1;
            var daysInPartialYear = 365;
            var rentsArray = [125000, 125000, 0, 0, 0];
            expect(service.calculateNPV(fullYears, partialDays, daysInPartialYear, rentsArray)).toEqual(237461);
        });

        // HMRC LH NonRes 1, 4, 7
        it(' calculateNPV should return 1471 for 4 full years, 0/365 partial days, rents 100, 1000, 200, 300, 0', function() {
            var fullYears = 4;
            var partialDays = 0;
            var daysInPartialYear = 365;
            var rentsArray = [100, 1000, 200, 300, 0];
            expect(service.calculateNPV(fullYears, partialDays, daysInPartialYear, rentsArray)).toEqual(1471);
        });

        // HMRC LH NonRes 2, 5, 8
        it(' calculateNPV should return 9024802 for 4 full years, 0/365 partial days, rents 1000000, 2000000, 3000000, 4000000, 0', function() {
            var fullYears = 4;
            var partialDays = 0;
            var daysInPartialYear = 365;
            var rentsArray = [1000000, 2000000, 3000000, 4000000, 0];
            expect(service.calculateNPV(fullYears, partialDays, daysInPartialYear, rentsArray)).toEqual(9024802);
        });

        // HMRC LH NonRes 3, 6, 9
        it(' calculateNPV should return 1836539 for 4 full years, 0/365 partial days, rents 500000, 500000, 500000, 500000, 0', function() {
            var fullYears = 4;
            var partialDays = 0;
            var daysInPartialYear = 365;
            var rentsArray = [500000, 500000, 500000, 500000, 0];
            expect(service.calculateNPV(fullYears, partialDays, daysInPartialYear, rentsArray)).toEqual(1836539);
        });

        // HMRC LH NonRes 10, 11, 12
        it(' calculateNPV should return 27655 for 100 full years, 0/365 partial days, rents 1000, 1000, 1000, 1000, 1000', function() {
            var fullYears = 100;
            var partialDays = 0;
            var daysInPartialYear = 365;
            var rentsArray = [1000, 1000, 1000, 1000, 1000];
            expect(service.calculateNPV(fullYears, partialDays, daysInPartialYear, rentsArray)).toEqual(27655);
        });

// *******************************************
// 
// TODO Check rounding 
// 
// *******************************************
        // // HMRC LH NonRes 13
        // it(' calculateNPV should return 13321376 for 100 full years, 0/365 partial days, rents 2500000, 2500000, 2500000, 2500000, 2500000', function() {
        //     var fullYears = 30;
        //     var partialDays = 0;
        //     var daysInPartialYear = 365;
        //     var rentsArray = [2500000, 2500000, 2500000, 2500000, 2500000];
        //     expect(service.calculateNPV(fullYears, partialDays, daysInPartialYear, rentsArray)).toEqual(13321376);
        // });


  });
}());
