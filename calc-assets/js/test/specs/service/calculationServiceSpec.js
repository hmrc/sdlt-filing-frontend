(function() {
    'use strict';

    describe('Calculation Service', function () {

        var service;

        beforeEach(angular.mock.module("calc.services"));

        beforeEach(inject(function (_calculationService_) {
            service = _calculationService_;
        }));

        // ********************* calculateNPV *********************
        it(' calculateNPV should return 31020 for 18 years, 91/365 partial days, rents 500, 900, 1100, 2500, 2750 ', function() {
            var fullYears = 18;
            var partialDays = 91;
            var daysInPartialYear = 365;
            var rentsArray = [500, 900, 1100, 2500, 2750];
            expect(service.calculateNPV(fullYears, partialDays, daysInPartialYear, rentsArray)).toEqual(31020);
        });

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

        // ********************* calcFreeResPrem_201203_201412 *********************
        calcFreeResPrem_201203_201412_Results = [{
            totalTax : 0,
            taxCalcs : [{
                taxType : "premium",
                calcType : "slab",
                taxDue : 0,
                rate : 0
            }]
        }];
        it(' calcFreeResPrem_201203_201412 should return 0 for purchase price of 125000', function() {
            calcFreeResPrem_201203_201412_Results[0].totalTax = 0;
            calcFreeResPrem_201203_201412_Results[0].taxCalcs[0].taxDue = 0;
            calcFreeResPrem_201203_201412_Results[0].taxCalcs[0].rate = 0;
            expect(service.calcFreeResPrem_201203_201412(125000)).toEqual(calcFreeResPrem_201203_201412_Results);
        });
        it(' calcFreeResPrem_201203_201412 should return 1250 for purchase price of 125001', function() {
            calcFreeResPrem_201203_201412_Results[0].totalTax = 1250;
            calcFreeResPrem_201203_201412_Results[0].taxCalcs[0].taxDue = 1250;
            calcFreeResPrem_201203_201412_Results[0].taxCalcs[0].rate = 1;
            expect(service.calcFreeResPrem_201203_201412(125001)).toEqual(calcFreeResPrem_201203_201412_Results);
        });
        it(' calcFreeResPrem_201203_201412 should return 2500 for purchase price of 250000', function() {
            calcFreeResPrem_201203_201412_Results[0].totalTax = 2500;
            calcFreeResPrem_201203_201412_Results[0].taxCalcs[0].taxDue = 2500;
            calcFreeResPrem_201203_201412_Results[0].taxCalcs[0].rate = 1;
            expect(service.calcFreeResPrem_201203_201412(250000)).toEqual(calcFreeResPrem_201203_201412_Results);
        });
       it(' calcFreeResPrem_201203_201412 should return 7500 for purchase price of 250001', function() {
            calcFreeResPrem_201203_201412_Results[0].totalTax = 7500;
            calcFreeResPrem_201203_201412_Results[0].taxCalcs[0].taxDue = 7500;
            calcFreeResPrem_201203_201412_Results[0].taxCalcs[0].rate = 3;
            expect(service.calcFreeResPrem_201203_201412(250001)).toEqual(calcFreeResPrem_201203_201412_Results);
        });
       it(' calcFreeResPrem_201203_201412 should return 15000 for purchase price of 500000', function() {
            calcFreeResPrem_201203_201412_Results[0].totalTax = 15000;
            calcFreeResPrem_201203_201412_Results[0].taxCalcs[0].taxDue = 15000;
            calcFreeResPrem_201203_201412_Results[0].taxCalcs[0].rate = 3;
            expect(service.calcFreeResPrem_201203_201412(500000)).toEqual(calcFreeResPrem_201203_201412_Results);
        });
       it(' calcFreeResPrem_201203_201412 should return 20000 for purchase price of 500001', function() {
            calcFreeResPrem_201203_201412_Results[0].totalTax = 20000;
            calcFreeResPrem_201203_201412_Results[0].taxCalcs[0].taxDue = 20000;
            calcFreeResPrem_201203_201412_Results[0].taxCalcs[0].rate = 4;
            expect(service.calcFreeResPrem_201203_201412(500001)).toEqual(calcFreeResPrem_201203_201412_Results);
        });
       it(' calcFreeResPrem_201203_201412 should return 40000 for purchase price of 1000000', function() {
            calcFreeResPrem_201203_201412_Results[0].totalTax = 40000;
            calcFreeResPrem_201203_201412_Results[0].taxCalcs[0].taxDue = 40000;
            calcFreeResPrem_201203_201412_Results[0].taxCalcs[0].rate = 4;
            expect(service.calcFreeResPrem_201203_201412(1000000)).toEqual(calcFreeResPrem_201203_201412_Results);
        });
       it(' calcFreeResPrem_201203_201412 should return 50000 for purchase price of 1000001', function() {
            calcFreeResPrem_201203_201412_Results[0].totalTax = 50000;
            calcFreeResPrem_201203_201412_Results[0].taxCalcs[0].taxDue = 50000;
            calcFreeResPrem_201203_201412_Results[0].taxCalcs[0].rate = 5;
            expect(service.calcFreeResPrem_201203_201412(1000001)).toEqual(calcFreeResPrem_201203_201412_Results);
        });
       it(' calcFreeResPrem_201203_201412 should return 100000 for purchase price of 2000000', function() {
            calcFreeResPrem_201203_201412_Results[0].totalTax = 100000;
            calcFreeResPrem_201203_201412_Results[0].taxCalcs[0].taxDue = 100000;
            calcFreeResPrem_201203_201412_Results[0].taxCalcs[0].rate = 5;
            expect(service.calcFreeResPrem_201203_201412(2000000)).toEqual(calcFreeResPrem_201203_201412_Results);
        });
       it(' calcFreeResPrem_201203_201412 should return 140000 for purchase price of 2000001', function() {
            calcFreeResPrem_201203_201412_Results[0].totalTax = 140000;
            calcFreeResPrem_201203_201412_Results[0].taxCalcs[0].taxDue = 140000;
            calcFreeResPrem_201203_201412_Results[0].taxCalcs[0].rate = 7;
            expect(service.calcFreeResPrem_201203_201412(2000001)).toEqual(calcFreeResPrem_201203_201412_Results);
        });

        // ********************* calcFreeResPrem_201412_Undef *********************
        calcFreeResPrem_201412_Undef_Results = [{
            totalTax : 0,
            taxCalcs : [{
                taxType : "premium",
                calcType : "slice",
                detailHeading : 'This is a breakdown of how the total amount of SDLT was calculated',
                bandHeading : 'Purchase price bands (£)',
                detailFooter : 'Total SDLT due',
                taxDue : 0,
                slices : [
                    { from: 0,       to : 125000,  rate : 0,  taxDue : 0},
                    { from: 125000,  to : 250000,  rate : 2,  taxDue : 0},
                    { from: 250000,  to : 925000,  rate : 5,  taxDue : 0},
                    { from: 925000,  to : 1500000, rate : 10, taxDue : 0},
                    { from: 1500000, to : -1,      rate : 12, taxDue : 0}
                ]
            }]
        }];

        it(' calcFreeResPrem_201412_Undef should return 0 for purchase price of 125000', function() {
            calcFreeResPrem_201412_Undef_Results[0].totalTax = 0;
            calcFreeResPrem_201412_Undef_Results[0].taxCalcs[0].taxDue = 0;
            calcFreeResPrem_201412_Undef_Results[0].taxCalcs[0].slices[0].taxDue = 0;
            calcFreeResPrem_201412_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 0;
            calcFreeResPrem_201412_Undef_Results[0].taxCalcs[0].slices[2].taxDue = 0;
            calcFreeResPrem_201412_Undef_Results[0].taxCalcs[0].slices[3].taxDue = 0;
            calcFreeResPrem_201412_Undef_Results[0].taxCalcs[0].slices[4].taxDue = 0;
            expect(service.calcFreeResPrem_201412_Undef(125000)).toEqual(calcFreeResPrem_201412_Undef_Results);
        });

        it(' calcFreeResPrem_201412_Undef should return 1 for purchase price of 125050', function() {
            calcFreeResPrem_201412_Undef_Results[0].totalTax = 1;
            calcFreeResPrem_201412_Undef_Results[0].taxCalcs[0].taxDue = 1;
            calcFreeResPrem_201412_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 1;
            expect(service.calcFreeResPrem_201412_Undef(125050)).toEqual(calcFreeResPrem_201412_Undef_Results);
        });

        it(' calcFreeResPrem_201412_Undef should return 2500 for purchase price of 250000', function() {
            calcFreeResPrem_201412_Undef_Results[0].totalTax = 2500;
            calcFreeResPrem_201412_Undef_Results[0].taxCalcs[0].taxDue = 2500;
            calcFreeResPrem_201412_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 2500;
            expect(service.calcFreeResPrem_201412_Undef(250000)).toEqual(calcFreeResPrem_201412_Undef_Results);
        });

        it(' calcFreeResPrem_201412_Undef should return 2501 for purchase price of 250020', function() {
            calcFreeResPrem_201412_Undef_Results[0].totalTax = 2501;
            calcFreeResPrem_201412_Undef_Results[0].taxCalcs[0].taxDue = 2501;
            calcFreeResPrem_201412_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 2500;
            calcFreeResPrem_201412_Undef_Results[0].taxCalcs[0].slices[2].taxDue = 1;
            expect(service.calcFreeResPrem_201412_Undef(250020)).toEqual(calcFreeResPrem_201412_Undef_Results);
        });

        it(' calcFreeResPrem_201412_Undef should return 36250 for purchase price of 925000', function() {
            calcFreeResPrem_201412_Undef_Results[0].totalTax = 36250;
            calcFreeResPrem_201412_Undef_Results[0].taxCalcs[0].taxDue = 36250;
            calcFreeResPrem_201412_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 2500;
            calcFreeResPrem_201412_Undef_Results[0].taxCalcs[0].slices[2].taxDue = 33750;
            expect(service.calcFreeResPrem_201412_Undef(925000)).toEqual(calcFreeResPrem_201412_Undef_Results);
        });       

        it(' calcFreeResPrem_201412_Undef should return 36251 for purchase price of 925010', function() {
            calcFreeResPrem_201412_Undef_Results[0].totalTax = 36251;
            calcFreeResPrem_201412_Undef_Results[0].taxCalcs[0].taxDue = 36251;
            calcFreeResPrem_201412_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 2500;
            calcFreeResPrem_201412_Undef_Results[0].taxCalcs[0].slices[2].taxDue = 33750;
            calcFreeResPrem_201412_Undef_Results[0].taxCalcs[0].slices[3].taxDue = 1;
            expect(service.calcFreeResPrem_201412_Undef(925010)).toEqual(calcFreeResPrem_201412_Undef_Results);
        });       

        it(' calcFreeResPrem_201412_Undef should return 93750 for purchase price of 1500000', function() {
            calcFreeResPrem_201412_Undef_Results[0].totalTax = 93750;
            calcFreeResPrem_201412_Undef_Results[0].taxCalcs[0].taxDue = 93750;
            calcFreeResPrem_201412_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 2500;
            calcFreeResPrem_201412_Undef_Results[0].taxCalcs[0].slices[2].taxDue = 33750;
            calcFreeResPrem_201412_Undef_Results[0].taxCalcs[0].slices[3].taxDue = 57500;
            expect(service.calcFreeResPrem_201412_Undef(1500000)).toEqual(calcFreeResPrem_201412_Undef_Results);
        });       

        it(' calcFreeResPrem_201412_Undef should return 93751 for purchase price of 1500009', function() {
            calcFreeResPrem_201412_Undef_Results[0].totalTax = 93751;
            calcFreeResPrem_201412_Undef_Results[0].taxCalcs[0].taxDue = 93751;
            calcFreeResPrem_201412_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 2500;
            calcFreeResPrem_201412_Undef_Results[0].taxCalcs[0].slices[2].taxDue = 33750;
            calcFreeResPrem_201412_Undef_Results[0].taxCalcs[0].slices[3].taxDue = 57500;
            calcFreeResPrem_201412_Undef_Results[0].taxCalcs[0].slices[4].taxDue = 1;
            expect(service.calcFreeResPrem_201412_Undef(1500009)).toEqual(calcFreeResPrem_201412_Undef_Results);
        });       

        // ********************* calcFreeResPremAddProp_201604_Undef *********************
        calcFreeResPremAddProp_201604_Undef_Results = [
            {
                resultHeading : "Results based on SDLT rules from 1 April 2016",
                totalTax : 0,
                taxCalcs : [
                    {
                        taxType : "premium",
                        calcType : "slice",
                        detailHeading : "This is a breakdown of how the total amount of SDLT was calculated based on the rules from 1 April 2016",
                        bandHeading : 'Purchase price bands (£)',
                        detailFooter : 'Total SDLT due',
                        taxDue : 0,
                        slices : [
                            { from: 0,       to : 125000,  rate : 3,  taxDue : 0},
                            { from: 125000,  to : 250000,  rate : 5,  taxDue : 0},
                            { from: 250000,  to : 925000,  rate : 8,  taxDue : 0},
                            { from: 925000,  to : 1500000, rate : 13, taxDue : 0},
                            { from: 1500000, to : -1,      rate : 15, taxDue : 0}
                        ]
                    }
                ]
            },
            {
                totalTax : 0,
                taxCalcs : [
                    {
                        taxType : "premium",
                        calcType : "slice",
                        detailHeading : "This is a breakdown of how the total amount of SDLT was calculated based on the rules before 1 April 2016",
                        bandHeading : 'Purchase price bands (£)',
                        detailFooter : 'Total SDLT due',
                        taxDue : 0,
                        slices : [
                            { from: 0,       to : 125000,  rate : 0,  taxDue : 0},
                            { from: 125000,  to : 250000,  rate : 2,  taxDue : 0},
                            { from: 250000,  to : 925000,  rate : 5,  taxDue : 0},
                            { from: 925000,  to : 1500000, rate : 10, taxDue : 0},
                            { from: 1500000, to : -1,      rate : 12, taxDue : 0}
                        ]
                    }
                ],
                resultHeading : "Results based on SDLT rules before 1 April 2016",
                resultHint : "You may be entitled to pay SDLT using the old rules if you exchanged contracts before 26 November 2015."
            }    
        ];

        it(' calcFreeResPremAddProp_201604_Undef should return 0, 0 for purchase price of 39999.99', function() {
            expect(service.calcFreeResPremAddProp_201604_Undef(39999.99)).toEqual(calcFreeResPremAddProp_201604_Undef_Results);
        });

        it(' calcFreeResPremAddProp_201604_Undef should return 1200, 0 for purchase price of 40000', function() {
            calcFreeResPremAddProp_201604_Undef_Results[0].totalTax = 1200;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].taxDue = 1200;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[0].taxDue = 1200;
            expect(service.calcFreeResPremAddProp_201604_Undef(40000)).toEqual(calcFreeResPremAddProp_201604_Undef_Results);
        });

        it(' calcFreeResPremAddProp_201604_Undef should return 3750, 0 for purchase price of 125000', function() {
            calcFreeResPremAddProp_201604_Undef_Results[0].totalTax = 3750;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].taxDue = 3750;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[0].taxDue = 3750;
            expect(service.calcFreeResPremAddProp_201604_Undef(125000)).toEqual(calcFreeResPremAddProp_201604_Undef_Results);
        });

        it(' calcFreeResPremAddProp_201604_Undef should return 3755, 2 for purchase price of 125100', function() {
            calcFreeResPremAddProp_201604_Undef_Results[0].totalTax = 3755;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].taxDue = 3755;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[0].taxDue = 3750;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 5;
            calcFreeResPremAddProp_201604_Undef_Results[1].totalTax = 2;
            calcFreeResPremAddProp_201604_Undef_Results[1].taxCalcs[0].taxDue = 2;
            calcFreeResPremAddProp_201604_Undef_Results[1].taxCalcs[0].slices[1].taxDue = 2;
            expect(service.calcFreeResPremAddProp_201604_Undef(125100)).toEqual(calcFreeResPremAddProp_201604_Undef_Results);
        });

        it(' calcFreeResPremAddProp_201604_Undef should return 10000, 2500 for purchase price of 250000', function() {
            calcFreeResPremAddProp_201604_Undef_Results[0].totalTax = 10000;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].taxDue = 10000;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[0].taxDue = 3750;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 6250;
            calcFreeResPremAddProp_201604_Undef_Results[1].totalTax = 2500;
            calcFreeResPremAddProp_201604_Undef_Results[1].taxCalcs[0].taxDue = 2500;
            calcFreeResPremAddProp_201604_Undef_Results[1].taxCalcs[0].slices[1].taxDue = 2500;
            expect(service.calcFreeResPremAddProp_201604_Undef(250000)).toEqual(calcFreeResPremAddProp_201604_Undef_Results);
        });

        it(' calcFreeResPremAddProp_201604_Undef should return 10008, 2505 for purchase price of 250100', function() {
            calcFreeResPremAddProp_201604_Undef_Results[0].totalTax = 10008;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].taxDue = 10008;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[0].taxDue = 3750;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 6250;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[2].taxDue = 8;
            calcFreeResPremAddProp_201604_Undef_Results[1].totalTax = 2505;
            calcFreeResPremAddProp_201604_Undef_Results[1].taxCalcs[0].taxDue = 2505;
            calcFreeResPremAddProp_201604_Undef_Results[1].taxCalcs[0].slices[1].taxDue = 2500;
            calcFreeResPremAddProp_201604_Undef_Results[1].taxCalcs[0].slices[2].taxDue = 5;
            expect(service.calcFreeResPremAddProp_201604_Undef(250100)).toEqual(calcFreeResPremAddProp_201604_Undef_Results);
        });

        it(' calcFreeResPremAddProp_201604_Undef should return 64000, 36250 for purchase price of 925000', function() {
            calcFreeResPremAddProp_201604_Undef_Results[0].totalTax = 64000;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].taxDue = 64000;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[0].taxDue = 3750;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 6250;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[2].taxDue = 54000;
            calcFreeResPremAddProp_201604_Undef_Results[1].totalTax = 36250;
            calcFreeResPremAddProp_201604_Undef_Results[1].taxCalcs[0].taxDue = 36250;
            calcFreeResPremAddProp_201604_Undef_Results[1].taxCalcs[0].slices[1].taxDue = 2500;
            calcFreeResPremAddProp_201604_Undef_Results[1].taxCalcs[0].slices[2].taxDue = 33750;
            expect(service.calcFreeResPremAddProp_201604_Undef(925000)).toEqual(calcFreeResPremAddProp_201604_Undef_Results);
        });

        it(' calcFreeResPremAddProp_201604_Undef should return 64000, 36250 for purchase price of 925100', function() {
            calcFreeResPremAddProp_201604_Undef_Results[0].totalTax = 64013;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].taxDue = 64013;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[0].taxDue = 3750;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 6250;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[2].taxDue = 54000;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[3].taxDue = 13;
            calcFreeResPremAddProp_201604_Undef_Results[1].totalTax = 36260;
            calcFreeResPremAddProp_201604_Undef_Results[1].taxCalcs[0].taxDue = 36260;
            calcFreeResPremAddProp_201604_Undef_Results[1].taxCalcs[0].slices[1].taxDue = 2500;
            calcFreeResPremAddProp_201604_Undef_Results[1].taxCalcs[0].slices[2].taxDue = 33750;
            calcFreeResPremAddProp_201604_Undef_Results[1].taxCalcs[0].slices[3].taxDue = 10;
            expect(service.calcFreeResPremAddProp_201604_Undef(925100)).toEqual(calcFreeResPremAddProp_201604_Undef_Results);
        });

        it(' calcFreeResPremAddProp_201604_Undef should return 138750, 93750 for purchase price of 1500000', function() {
            calcFreeResPremAddProp_201604_Undef_Results[0].totalTax = 138750;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].taxDue = 138750;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[0].taxDue = 3750;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 6250;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[2].taxDue = 54000;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[3].taxDue = 74750;
            calcFreeResPremAddProp_201604_Undef_Results[1].totalTax = 93750;
            calcFreeResPremAddProp_201604_Undef_Results[1].taxCalcs[0].taxDue = 93750;
            calcFreeResPremAddProp_201604_Undef_Results[1].taxCalcs[0].slices[1].taxDue = 2500;
            calcFreeResPremAddProp_201604_Undef_Results[1].taxCalcs[0].slices[2].taxDue = 33750;
            calcFreeResPremAddProp_201604_Undef_Results[1].taxCalcs[0].slices[3].taxDue = 57500;
            expect(service.calcFreeResPremAddProp_201604_Undef(1500000)).toEqual(calcFreeResPremAddProp_201604_Undef_Results);
        });

        it(' calcFreeResPremAddProp_201604_Undef should return 138765, 93762 for purchase price of 1500100', function() {
            calcFreeResPremAddProp_201604_Undef_Results[0].totalTax = 138765;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].taxDue = 138765;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[0].taxDue = 3750;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 6250;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[2].taxDue = 54000;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[3].taxDue = 74750;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[4].taxDue = 15;
            calcFreeResPremAddProp_201604_Undef_Results[1].totalTax = 93762;
            calcFreeResPremAddProp_201604_Undef_Results[1].taxCalcs[0].taxDue = 93762;
            calcFreeResPremAddProp_201604_Undef_Results[1].taxCalcs[0].slices[1].taxDue = 2500;
            calcFreeResPremAddProp_201604_Undef_Results[1].taxCalcs[0].slices[2].taxDue = 33750;
            calcFreeResPremAddProp_201604_Undef_Results[1].taxCalcs[0].slices[3].taxDue = 57500;
            calcFreeResPremAddProp_201604_Undef_Results[1].taxCalcs[0].slices[4].taxDue = 12;
            expect(service.calcFreeResPremAddProp_201604_Undef(1500100)).toEqual(calcFreeResPremAddProp_201604_Undef_Results);
        });

        it(' calcFreeResPremAddProp_201604_Undef should return 11163750, 8913750 for purchase price of 75000000', function() {
            calcFreeResPremAddProp_201604_Undef_Results[0].totalTax = 11163750;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].taxDue = 11163750;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[0].taxDue = 3750;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 6250;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[2].taxDue = 54000;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[3].taxDue = 74750;
            calcFreeResPremAddProp_201604_Undef_Results[0].taxCalcs[0].slices[4].taxDue = 11025000;
            calcFreeResPremAddProp_201604_Undef_Results[1].totalTax = 8913750;
            calcFreeResPremAddProp_201604_Undef_Results[1].taxCalcs[0].taxDue = 8913750;
            calcFreeResPremAddProp_201604_Undef_Results[1].taxCalcs[0].slices[0].taxDue = 0;
            calcFreeResPremAddProp_201604_Undef_Results[1].taxCalcs[0].slices[1].taxDue = 2500;
            calcFreeResPremAddProp_201604_Undef_Results[1].taxCalcs[0].slices[2].taxDue = 33750;
            calcFreeResPremAddProp_201604_Undef_Results[1].taxCalcs[0].slices[3].taxDue = 57500;
            calcFreeResPremAddProp_201604_Undef_Results[1].taxCalcs[0].slices[4].taxDue = 8820000;
            expect(service.calcFreeResPremAddProp_201604_Undef(75000000)).toEqual(calcFreeResPremAddProp_201604_Undef_Results);
        });


        // ********************* calcFreeNonResPrem_201203_201603 *********************
        calcFreeNonResPrem_201203_201603_Results = [{
            totalTax : 0,
            taxCalcs : [{
                taxType : "premium",
                calcType : "slab",
                taxDue : 0,
                rate : 0
            }]
        }];
        it(' calcFreeNonResPrem_201203_201603 should return 0 for purchase price of 150000', function() {
            calcFreeNonResPrem_201203_201603_Results[0].totalTax = 0;
            calcFreeNonResPrem_201203_201603_Results[0].taxCalcs[0].taxDue = 0;
            calcFreeNonResPrem_201203_201603_Results[0].taxCalcs[0].rate = 0;
            expect(service.calcFreeNonResPrem_201203_201603(150000)).toEqual(calcFreeNonResPrem_201203_201603_Results);
        });

        it(' calcFreeNonResPrem_201203_201603 should return 1500 for purchase price of 150001', function() {
            calcFreeNonResPrem_201203_201603_Results[0].totalTax = 1500;
            calcFreeNonResPrem_201203_201603_Results[0].taxCalcs[0].taxDue = 1500;
            calcFreeNonResPrem_201203_201603_Results[0].taxCalcs[0].rate = 1;
            expect(service.calcFreeNonResPrem_201203_201603(150001)).toEqual(calcFreeNonResPrem_201203_201603_Results);
        });

        it(' calcFreeNonResPrem_201203_201603 should return 2500 for purchase price of 250000', function() {
            calcFreeNonResPrem_201203_201603_Results[0].totalTax = 2500;
            calcFreeNonResPrem_201203_201603_Results[0].taxCalcs[0].taxDue = 2500;
            calcFreeNonResPrem_201203_201603_Results[0].taxCalcs[0].rate = 1;
            expect(service.calcFreeNonResPrem_201203_201603(250000)).toEqual(calcFreeNonResPrem_201203_201603_Results);
        });

        it(' calcFreeNonResPrem_201203_201603 should return 7500 for purchase price of 250001', function() {
            calcFreeNonResPrem_201203_201603_Results[0].totalTax = 7500;
            calcFreeNonResPrem_201203_201603_Results[0].taxCalcs[0].taxDue = 7500;
            calcFreeNonResPrem_201203_201603_Results[0].taxCalcs[0].rate = 3;
            expect(service.calcFreeNonResPrem_201203_201603(250001)).toEqual(calcFreeNonResPrem_201203_201603_Results);
        });

        it(' calcFreeNonResPrem_201203_201603 should return 15000 for purchase price of 500000', function() {
            calcFreeNonResPrem_201203_201603_Results[0].totalTax = 15000;
            calcFreeNonResPrem_201203_201603_Results[0].taxCalcs[0].taxDue = 15000;
            calcFreeNonResPrem_201203_201603_Results[0].taxCalcs[0].rate = 3;
            expect(service.calcFreeNonResPrem_201203_201603(500000)).toEqual(calcFreeNonResPrem_201203_201603_Results);
        });

        it(' calcFreeNonResPrem_201203_201603 should return 20000 for purchase price of 500001', function() {
            calcFreeNonResPrem_201203_201603_Results[0].totalTax = 20000;
            calcFreeNonResPrem_201203_201603_Results[0].taxCalcs[0].taxDue = 20000;
            calcFreeNonResPrem_201203_201603_Results[0].taxCalcs[0].rate = 4;
            expect(service.calcFreeNonResPrem_201203_201603(500001)).toEqual(calcFreeNonResPrem_201203_201603_Results);
        });

        // ********************* calcFreeNonResPrem_201603_Undef *********************
        calcFreeNonResPrem_201603_Undef_Results = [
            {
                resultHeading : "Results based on SDLT rules from 17 March 2016",
                totalTax : 0,
                taxCalcs : [
                    {
                        taxType : "premium",
                        calcType : "slice",
                        detailHeading : "This is a breakdown of how the total amount of SDLT was calculated based on the rules from 17 March 2016",
                        bandHeading : 'Purchase price bands (£)',
                        detailFooter : 'Total SDLT due',
                        taxDue : 0,
                        slices : [
                            { from: 0,       to : 150000,  rate : 0,  taxDue : 0},
                            { from: 150000,  to : 250000,  rate : 2,  taxDue : 0},
                            { from: 250000,  to : -1,      rate : 5,  taxDue : 0}
                        ]
                    }
                ]
            },
            {
                totalTax : 0,
                taxCalcs : [
                    {
                        taxType : "premium",
                        calcType : "slab",
                        taxDue : 0,
                        rate : 0
                    }
                ],
                resultHeading : "Results based on SDLT rules before 17 March 2016",
                resultHint : "You may be entitled to pay SDLT using the old rules if you exchanged contracts before 17 March 2016."
            }    
        ];

        it(' calcFreeNonResPrem_201603_Undef should return 0, 0 for purchase price of 150000', function() {
            expect(service.calcFreeNonResPrem_201603_Undef(150000, true)).toEqual(calcFreeNonResPrem_201603_Undef_Results);
        });

        it(' calcFreeNonResPrem_201603_Undef should return 2, 1501 for purchase price of 150100', function() {
            calcFreeNonResPrem_201603_Undef_Results[0].totalTax = 2;
            calcFreeNonResPrem_201603_Undef_Results[0].taxCalcs[0].taxDue = 2;
            calcFreeNonResPrem_201603_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 2;
            calcFreeNonResPrem_201603_Undef_Results[1].totalTax = 1501;
            calcFreeNonResPrem_201603_Undef_Results[1].taxCalcs[0].taxDue = 1501;
            calcFreeNonResPrem_201603_Undef_Results[1].taxCalcs[0].rate = 1;
            expect(service.calcFreeNonResPrem_201603_Undef(150100, true)).toEqual(calcFreeNonResPrem_201603_Undef_Results);
        });

        it(' calcFreeNonResPrem_201603_Undef should return 2000, 2500 for purchase price of 250000', function() {
            calcFreeNonResPrem_201603_Undef_Results[0].totalTax = 2000;
            calcFreeNonResPrem_201603_Undef_Results[0].taxCalcs[0].taxDue = 2000;
            calcFreeNonResPrem_201603_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 2000;
            calcFreeNonResPrem_201603_Undef_Results[1].totalTax = 2500;
            calcFreeNonResPrem_201603_Undef_Results[1].taxCalcs[0].taxDue = 2500;
            calcFreeNonResPrem_201603_Undef_Results[1].taxCalcs[0].rate = 1;
            expect(service.calcFreeNonResPrem_201603_Undef(250000, true)).toEqual(calcFreeNonResPrem_201603_Undef_Results);
        });

        it(' calcFreeNonResPrem_201603_Undef should return 2005, 7503 for purchase price of 250100', function() {
            calcFreeNonResPrem_201603_Undef_Results[0].totalTax = 2005;
            calcFreeNonResPrem_201603_Undef_Results[0].taxCalcs[0].taxDue = 2005;
            calcFreeNonResPrem_201603_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 2000;
            calcFreeNonResPrem_201603_Undef_Results[0].taxCalcs[0].slices[2].taxDue = 5;
            calcFreeNonResPrem_201603_Undef_Results[1].totalTax = 7503;
            calcFreeNonResPrem_201603_Undef_Results[1].taxCalcs[0].taxDue = 7503;
            calcFreeNonResPrem_201603_Undef_Results[1].taxCalcs[0].rate = 3;
            expect(service.calcFreeNonResPrem_201603_Undef(250100, true)).toEqual(calcFreeNonResPrem_201603_Undef_Results);
        });

        it(' calcFreeNonResPrem_201603_Undef should return 14500, 15000 for purchase price of 500000', function() {
            calcFreeNonResPrem_201603_Undef_Results[0].totalTax = 14500;
            calcFreeNonResPrem_201603_Undef_Results[0].taxCalcs[0].taxDue = 14500;
            calcFreeNonResPrem_201603_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 2000;
            calcFreeNonResPrem_201603_Undef_Results[0].taxCalcs[0].slices[2].taxDue = 12500;
            calcFreeNonResPrem_201603_Undef_Results[1].totalTax = 15000;
            calcFreeNonResPrem_201603_Undef_Results[1].taxCalcs[0].taxDue = 15000;
            calcFreeNonResPrem_201603_Undef_Results[1].taxCalcs[0].rate = 3;
            expect(service.calcFreeNonResPrem_201603_Undef(500000, true)).toEqual(calcFreeNonResPrem_201603_Undef_Results);
        });

        it(' calcFreeNonResPrem_201603_Undef should return 14505, 20004 for purchase price of 500100', function() {
            calcFreeNonResPrem_201603_Undef_Results[0].totalTax = 14505;
            calcFreeNonResPrem_201603_Undef_Results[0].taxCalcs[0].taxDue = 14505;
            calcFreeNonResPrem_201603_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 2000;
            calcFreeNonResPrem_201603_Undef_Results[0].taxCalcs[0].slices[2].taxDue = 12505;
            calcFreeNonResPrem_201603_Undef_Results[1].totalTax = 20004;
            calcFreeNonResPrem_201603_Undef_Results[1].taxCalcs[0].taxDue = 20004;
            calcFreeNonResPrem_201603_Undef_Results[1].taxCalcs[0].rate = 4;
            expect(service.calcFreeNonResPrem_201603_Undef(500100, true)).toEqual(calcFreeNonResPrem_201603_Undef_Results);
        });


        // ********************* calcLeaseResPremAndRent_201203_201412 *********************
        calcLeaseResPremAndRent_201203_201412_Results = [{
            totalTax : 0,
            npv : 0,
            taxCalcs : [
                {
                    taxType : "rent",
                    calcType : "slice",
                    detailHeading : "This is a breakdown of how the amount of SDLT on the rent was calculated",
                    bandHeading : 'Rent bands (£)',
                    detailFooter : 'SDLT due on the rent',

                    taxDue : 0,
                    slices : [
                        { from: 0,       to : 125000,   rate : 0,  taxDue : 0},
                        { from: 125000,  to : -1    ,   rate : 1,  taxDue : -1}
                    ]

                },
                {
                    taxType : "premium",
                    calcType : "slab",
                    taxDue : 0,
                    rate : 0
                }
            ]
        }];
        it(' calcLeaseResPremAndRent_201203_201412_Results should return 0, 0 for purchase price of 125000, npv of 125000', function() {
            calcLeaseResPremAndRent_201203_201412_Results[0].totalTax = 0;
            calcLeaseResPremAndRent_201203_201412_Results[0].npv = 125000;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].taxDue = 0;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].slices[1].taxDue = 0;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[1].taxDue = 0;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[1].rate = 0;
            expect(service.calcLeaseResPremAndRent_201203_201412(125000, 125000)).toEqual(calcLeaseResPremAndRent_201203_201412_Results);
        });

        it(' calcLeaseResPremAndRent_201203_201412_Results should return 1250, 0 for purchase price of 125001, npv of 125000', function() {
            calcLeaseResPremAndRent_201203_201412_Results[0].totalTax = 1250;
            calcLeaseResPremAndRent_201203_201412_Results[0].npv = 125000;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[1].taxDue = 1250;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[1].rate = 1;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].taxDue = 0;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].slices[1].taxDue = 0;
            expect(service.calcLeaseResPremAndRent_201203_201412(125001, 125000)).toEqual(calcLeaseResPremAndRent_201203_201412_Results);
        });

        it(' calcLeaseResPremAndRent_201203_201412_Results should return 2500, 0 for purchase price of 250000, npv of 125000', function() {
            calcLeaseResPremAndRent_201203_201412_Results[0].totalTax = 2500;
            calcLeaseResPremAndRent_201203_201412_Results[0].npv = 125000;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[1].taxDue = 2500;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[1].rate = 1;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].taxDue = 0;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].slices[1].taxDue = 0;
            expect(service.calcLeaseResPremAndRent_201203_201412(250000, 125000)).toEqual(calcLeaseResPremAndRent_201203_201412_Results);
        });

        it(' calcLeaseResPremAndRent_201203_201412_Results should return 7500, 1 for purchase price of 250001, npv of 125100', function() {
            calcLeaseResPremAndRent_201203_201412_Results[0].totalTax = 7501;
            calcLeaseResPremAndRent_201203_201412_Results[0].npv = 125100;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[1].taxDue = 7500;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[1].rate = 3;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].taxDue = 1;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].slices[1].taxDue = 1;
            expect(service.calcLeaseResPremAndRent_201203_201412(250001, 125100)).toEqual(calcLeaseResPremAndRent_201203_201412_Results);
        });

        it(' calcLeaseResPremAndRent_201203_201412_Results should return 15000, 1 for purchase price of 500000, npv of 125100', function() {
            calcLeaseResPremAndRent_201203_201412_Results[0].totalTax = 15001;
            calcLeaseResPremAndRent_201203_201412_Results[0].npv = 125100;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[1].taxDue = 15000;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[1].rate = 3;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].taxDue = 1;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].slices[1].taxDue = 1;
            expect(service.calcLeaseResPremAndRent_201203_201412(500000, 125100)).toEqual(calcLeaseResPremAndRent_201203_201412_Results);
        });

        it(' calcLeaseResPremAndRent_201203_201412_Results should return 20000, 1 for purchase price of 500001, npv of 125100', function() {
            calcLeaseResPremAndRent_201203_201412_Results[0].totalTax = 20001;
            calcLeaseResPremAndRent_201203_201412_Results[0].npv = 125100;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[1].taxDue = 20000;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[1].rate = 4;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].taxDue = 1;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].slices[1].taxDue = 1;
            expect(service.calcLeaseResPremAndRent_201203_201412(500001, 125100)).toEqual(calcLeaseResPremAndRent_201203_201412_Results);
        });

        it(' calcLeaseResPremAndRent_201203_201412_Results should return 40000, 1 for purchase price of 1000000, npv of 125100', function() {
            calcLeaseResPremAndRent_201203_201412_Results[0].totalTax = 40001;
            calcLeaseResPremAndRent_201203_201412_Results[0].npv = 125100;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[1].taxDue = 40000;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[1].rate = 4;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].taxDue = 1;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].slices[1].taxDue = 1;
            expect(service.calcLeaseResPremAndRent_201203_201412(1000000, 125100)).toEqual(calcLeaseResPremAndRent_201203_201412_Results);
        });

        it(' calcLeaseResPremAndRent_201203_201412_Results should return 50000, 1 for purchase price of 1000001, npv of 125100', function() {
            calcLeaseResPremAndRent_201203_201412_Results[0].totalTax = 50001;
            calcLeaseResPremAndRent_201203_201412_Results[0].npv = 125100;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[1].taxDue = 50000;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[1].rate = 5;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].taxDue = 1;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].slices[1].taxDue = 1;
            expect(service.calcLeaseResPremAndRent_201203_201412(1000001, 125100)).toEqual(calcLeaseResPremAndRent_201203_201412_Results);
        });

        it(' calcLeaseResPremAndRent_201203_201412_Results should return 100000, 1 for purchase price of 2000000, npv of 125100', function() {
            calcLeaseResPremAndRent_201203_201412_Results[0].totalTax = 100001;
            calcLeaseResPremAndRent_201203_201412_Results[0].npv = 125100;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[1].taxDue = 100000;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[1].rate = 5;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].taxDue = 1;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].slices[1].taxDue = 1;
            expect(service.calcLeaseResPremAndRent_201203_201412(2000000, 125100)).toEqual(calcLeaseResPremAndRent_201203_201412_Results);
        });

        it(' calcLeaseResPremAndRent_201203_201412_Results should return 140000, 1250 for purchase price of 2000001, npv of 250000', function() {
            calcLeaseResPremAndRent_201203_201412_Results[0].totalTax = 141250;
            calcLeaseResPremAndRent_201203_201412_Results[0].npv = 250000;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[1].taxDue = 140000;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[1].rate = 7;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].taxDue = 1250;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseResPremAndRent_201203_201412_Results[0].taxCalcs[0].slices[1].taxDue = 1250;
            expect(service.calcLeaseResPremAndRent_201203_201412(2000001, 250000)).toEqual(calcLeaseResPremAndRent_201203_201412_Results);
        });

        // ********************* calcLeaseResPremAndRent_201412_Undef *********************
        calcLeaseResPremAndRent_201412_Undef_Results = [
            {
                totalTax : 0,
                npv : 0,
                taxCalcs : [
                    {
                        taxType : "rent",
                        calcType : "slice",
                        detailHeading : "This is a breakdown of how the amount of SDLT on the rent was calculated",
                        bandHeading : 'Rent bands (£)',
                        detailFooter : 'SDLT due on the rent',
                        taxDue : 0,
                        slices : [
                            { from: 0,       to : 125000,   rate : 0,  taxDue : 0},
                            { from: 125000,  to : -1    ,   rate : 1,  taxDue : -1}
                        ]

                    },
                    {
                        taxType : "premium",
                        calcType : "slice",
                        detailHeading : "This is a breakdown of how the amount of SDLT on the premium was calculated",
                        bandHeading : 'Premium bands (£)',
                        detailFooter : 'SDLT due on the premium',
                        taxDue : 0,
                        slices : [
                            { from: 0,       to : 125000,  rate : 0,  taxDue : 0},
                            { from: 125000,  to : 250000,  rate : 2,  taxDue : 0},
                            { from: 250000,  to : 925000,  rate : 5,  taxDue : 0},
                            { from: 925000,  to : 1500000, rate : 10, taxDue : 0},
                            { from: 1500000, to : -1,      rate : 12, taxDue : 0}
                        ]
                    }
                ]
            }
        ];
        it(' calcLeaseResPremAndRent_201412_Undef should return 0, 0 for purchase price of 125000, npv of 125000', function() {
            calcLeaseResPremAndRent_201412_Undef_Results[0].totalTax = 0;
            calcLeaseResPremAndRent_201412_Undef_Results[0].npv = 125000;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[0].taxDue = 0;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 0;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].taxDue = 0;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].slices[0].taxDue = 0;
            expect(service.calcLeaseResPremAndRent_201412_Undef(125000, 125000)).toEqual(calcLeaseResPremAndRent_201412_Undef_Results);
        });
        it(' calcLeaseResPremAndRent_201412_Undef should return 1, 0 for purchase price of 125050, npv of 125000', function() {
            calcLeaseResPremAndRent_201412_Undef_Results[0].totalTax = 1;
            calcLeaseResPremAndRent_201412_Undef_Results[0].npv = 125000;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[0].taxDue = 0;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 0;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].taxDue = 1;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].slices[0].taxDue = 0;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].slices[1].taxDue = 1;
            expect(service.calcLeaseResPremAndRent_201412_Undef(125050, 125000)).toEqual(calcLeaseResPremAndRent_201412_Undef_Results);
        });
        it(' calcLeaseResPremAndRent_201412_Undef should return 2500, 0 for purchase price of 250000, npv of 125000', function() {
            calcLeaseResPremAndRent_201412_Undef_Results[0].totalTax = 2500;
            calcLeaseResPremAndRent_201412_Undef_Results[0].npv = 125000;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[0].taxDue = 0;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 0;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].taxDue = 2500;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].slices[0].taxDue = 0;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].slices[1].taxDue = 2500;
            expect(service.calcLeaseResPremAndRent_201412_Undef(250000, 125000)).toEqual(calcLeaseResPremAndRent_201412_Undef_Results);
        });
        it(' calcLeaseResPremAndRent_201412_Undef should return 2501, 0 for purchase price of 250020, npv of 125000', function() {
            calcLeaseResPremAndRent_201412_Undef_Results[0].totalTax = 2501;
            calcLeaseResPremAndRent_201412_Undef_Results[0].npv = 125000;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[0].taxDue = 0;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 0;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].taxDue = 2501;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].slices[0].taxDue = 0;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].slices[1].taxDue = 2500;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].slices[2].taxDue = 1;
            expect(service.calcLeaseResPremAndRent_201412_Undef(250020, 125000)).toEqual(calcLeaseResPremAndRent_201412_Undef_Results);
        });
        it(' calcLeaseResPremAndRent_201412_Undef should return 36250, 1 for purchase price of 925000, npv of 125100', function() {
            calcLeaseResPremAndRent_201412_Undef_Results[0].totalTax = 36251;
            calcLeaseResPremAndRent_201412_Undef_Results[0].npv = 125100;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[0].taxDue = 1;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 1;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].taxDue = 36250;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].slices[0].taxDue = 0;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].slices[1].taxDue = 2500;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].slices[2].taxDue = 33750;
            expect(service.calcLeaseResPremAndRent_201412_Undef(925000, 125100)).toEqual(calcLeaseResPremAndRent_201412_Undef_Results);
        });
        it(' calcLeaseResPremAndRent_201412_Undef should return 36251, 1 for purchase price of 925010, npv of 125100', function() {
            calcLeaseResPremAndRent_201412_Undef_Results[0].totalTax = 36252;
            calcLeaseResPremAndRent_201412_Undef_Results[0].npv = 125100;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[0].taxDue = 1;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 1;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].taxDue = 36251;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].slices[0].taxDue = 0;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].slices[1].taxDue = 2500;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].slices[2].taxDue = 33750;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].slices[3].taxDue = 1;
            expect(service.calcLeaseResPremAndRent_201412_Undef(925010, 125100)).toEqual(calcLeaseResPremAndRent_201412_Undef_Results);
        });
        it(' calcLeaseResPremAndRent_201412_Undef should return 93750, 1 for purchase price of 1500000, npv of 125100', function() {
            calcLeaseResPremAndRent_201412_Undef_Results[0].totalTax = 93751;
            calcLeaseResPremAndRent_201412_Undef_Results[0].npv = 125100;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[0].taxDue = 1;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 1;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].taxDue = 93750;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].slices[0].taxDue = 0;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].slices[1].taxDue = 2500;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].slices[2].taxDue = 33750;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].slices[3].taxDue = 57500;
            expect(service.calcLeaseResPremAndRent_201412_Undef(1500000, 125100)).toEqual(calcLeaseResPremAndRent_201412_Undef_Results);
        });
        it(' calcLeaseResPremAndRent_201412_Undef should return 0, 0 for purchase price of 1500100, npv of 250000', function() {
            calcLeaseResPremAndRent_201412_Undef_Results[0].totalTax = 95012;
            calcLeaseResPremAndRent_201412_Undef_Results[0].npv = 250000;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[0].taxDue = 1250;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 1250;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].taxDue = 93762;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].slices[0].taxDue = 0;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].slices[1].taxDue = 2500;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].slices[2].taxDue = 33750;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].slices[3].taxDue = 57500;
            calcLeaseResPremAndRent_201412_Undef_Results[0].taxCalcs[1].slices[4].taxDue = 12;
            expect(service.calcLeaseResPremAndRent_201412_Undef(1500100, 250000)).toEqual(calcLeaseResPremAndRent_201412_Undef_Results);
        });

        // ********************* calcLeaseResPremAndRentAddProp_201604_Undef *********************
        calcLeaseResPremAndRentAddProp_201604_Undef_Results = [
            {
                resultHeading : "Results based on SDLT rules from 1 April 2016",
                totalTax : 0,
                npv : 0,
                taxCalcs : [
                    {
                        taxType : "rent",
                        calcType : "slice",
                        detailHeading : "This is a breakdown of how the amount of SDLT on the rent was calculated based on the rules from 1 April 2016",
                        bandHeading: 'Rent bands (£)', 
                        detailFooter: 'SDLT due on the rent',
                        taxDue : 0,
                        slices : [
                            { from: 0,       to : 125000,   rate : 0,  taxDue : 0},
                            { from: 125000,  to : -1    ,   rate : 1,  taxDue : 0}
                        ]

                    },
                    {
                        taxType : "premium",
                        calcType : "slice",
                        detailHeading : "This is a breakdown of how the amount of SDLT on the premium was calculated based on the rules from 1 April 2016",
                        bandHeading: 'Premium bands (£)', 
                        detailFooter: 'SDLT due on the premium',
                        taxDue : 0,
                        slices : [
                            { from: 0,       to : 125000,  rate : 3,  taxDue : 0},
                            { from: 125000,  to : 250000,  rate : 5,  taxDue : 0},
                            { from: 250000,  to : 925000,  rate : 8,  taxDue : 0},
                            { from: 925000,  to : 1500000, rate : 13, taxDue : 0},
                            { from: 1500000, to : -1,      rate : 15, taxDue : 0}
                        ]
                    }
                ]
            },
            {
                totalTax : 0,
                npv : 0,
                taxCalcs : [
                    {
                        taxType : "rent",
                        calcType : "slice",
                        detailHeading : "This is a breakdown of how the amount of SDLT on the rent was calculated based on the rules before 1 April 2016",
                        bandHeading: 'Rent bands (£)', 
                        detailFooter: 'SDLT due on the rent',
                        taxDue : 0,
                        slices : [
                            { from: 0,       to : 125000,   rate : 0,  taxDue : 0},
                            { from: 125000,  to : -1    ,   rate : 1,  taxDue : 0}
                        ]

                    },
                    {
                        taxType : "premium",
                        calcType : "slice",
                        detailHeading : "This is a breakdown of how the amount of SDLT on the premium was calculated based on the rules before 1 April 2016",
                        bandHeading: 'Premium bands (£)', 
                        detailFooter: 'SDLT due on the premium',
                        taxDue : 0,
                        slices : [
                            { from: 0,       to : 125000,  rate : 0,  taxDue : 0},
                            { from: 125000,  to : 250000,  rate : 2,  taxDue : 0},
                            { from: 250000,  to : 925000,  rate : 5,  taxDue : 0},
                            { from: 925000,  to : 1500000, rate : 10, taxDue : 0},
                            { from: 1500000, to : -1,      rate : 12, taxDue : 0}
                        ]
                    }
                ],
                resultHeading : "Results based on SDLT rules before 1 April 2016",
                resultHint : "You may be entitled to pay SDLT using the old rules if you exchanged contracts before 26 November 2015."
            }
        ];

        it(' calcLeaseResPremAndRentAddProp_201604_Undef should return 0, 0 for purchase price of 39,999.99, npv of 125000', function() {
            calcLeaseResPremAndRentAddProp_201604_Undef_Results[0].npv = 125000;
            calcLeaseResPremAndRentAddProp_201604_Undef_Results[1].npv = 125000;
            expect(service.calcLeaseResPremAndRentAddProp_201604_Undef(39999.99, 125000, true)).toEqual(calcLeaseResPremAndRentAddProp_201604_Undef_Results);
        });

        it(' calcLeaseResPremAndRentAddProp_201604_Undef should return 1250, 138765, 1250, 93762 for purchase price of 1500100, npv of 250000', function() {
            calcLeaseResPremAndRentAddProp_201604_Undef_Results[0].totalTax = 140015;
            calcLeaseResPremAndRentAddProp_201604_Undef_Results[0].npv = 250000;
            calcLeaseResPremAndRentAddProp_201604_Undef_Results[0].taxCalcs[0].taxDue = 1250;
            calcLeaseResPremAndRentAddProp_201604_Undef_Results[0].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseResPremAndRentAddProp_201604_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 1250;
            calcLeaseResPremAndRentAddProp_201604_Undef_Results[0].taxCalcs[1].taxDue = 138765;
            calcLeaseResPremAndRentAddProp_201604_Undef_Results[0].taxCalcs[1].slices[0].taxDue = 3750;
            calcLeaseResPremAndRentAddProp_201604_Undef_Results[0].taxCalcs[1].slices[1].taxDue = 6250;
            calcLeaseResPremAndRentAddProp_201604_Undef_Results[0].taxCalcs[1].slices[2].taxDue = 54000;
            calcLeaseResPremAndRentAddProp_201604_Undef_Results[0].taxCalcs[1].slices[3].taxDue = 74750;
            calcLeaseResPremAndRentAddProp_201604_Undef_Results[0].taxCalcs[1].slices[4].taxDue = 15;

            calcLeaseResPremAndRentAddProp_201604_Undef_Results[1].totalTax = 95012;
            calcLeaseResPremAndRentAddProp_201604_Undef_Results[1].npv = 250000;
            calcLeaseResPremAndRentAddProp_201604_Undef_Results[1].taxCalcs[0].taxDue = 1250;
            calcLeaseResPremAndRentAddProp_201604_Undef_Results[1].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseResPremAndRentAddProp_201604_Undef_Results[1].taxCalcs[0].slices[1].taxDue = 1250;
            calcLeaseResPremAndRentAddProp_201604_Undef_Results[1].taxCalcs[1].taxDue = 93762;
            calcLeaseResPremAndRentAddProp_201604_Undef_Results[1].taxCalcs[1].slices[0].taxDue = 0;
            calcLeaseResPremAndRentAddProp_201604_Undef_Results[1].taxCalcs[1].slices[1].taxDue = 2500;
            calcLeaseResPremAndRentAddProp_201604_Undef_Results[1].taxCalcs[1].slices[2].taxDue = 33750;
            calcLeaseResPremAndRentAddProp_201604_Undef_Results[1].taxCalcs[1].slices[3].taxDue = 57500;
            calcLeaseResPremAndRentAddProp_201604_Undef_Results[1].taxCalcs[1].slices[4].taxDue = 12;

            expect(service.calcLeaseResPremAndRentAddProp_201604_Undef(1500100, 250000, true)).toEqual(calcLeaseResPremAndRentAddProp_201604_Undef_Results);
        });

        // ********************* calcLeaseNonResPremAndRent_201203_201603 *********************
        calcLeaseNonResPremAndRent_201203_201603_Results = [
            {
                totalTax : 0,
                npv : 0,
                taxCalcs : [
                    {
                        taxType : "rent",
                        calcType : "slice",
                        detailHeading : "This is a breakdown of how the amount of SDLT on the rent was calculated",
                        bandHeading: 'Rent bands (£)', 
                        detailFooter: 'SDLT due on the rent',
                        taxDue : 0,
                        slices : [
                            { from: 0,       to : 150000,   rate : 0,  taxDue : 0},
                            { from: 150000,  to : -1    ,   rate : 1,  taxDue : 0}
                        ]

                    },
                    {
                        taxType : "premium",
                        calcType : "slab",
                        taxDue : 0,
                        rate : 0
                    }
                ]
            }
        ];
        it(' calcLeaseNonResPremAndRent_201203_201603 should return 0, 0 for purchase price of 150000, npv of 150000 and zeroRate is TRUE', function() {
            calcLeaseNonResPremAndRent_201203_201603_Results[0].totalTax = 0;
            calcLeaseNonResPremAndRent_201203_201603_Results[0].npv = 150000;
            calcLeaseNonResPremAndRent_201203_201603_Results[0].taxCalcs[0].taxDue = 0;
            calcLeaseNonResPremAndRent_201203_201603_Results[0].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseNonResPremAndRent_201203_201603_Results[0].taxCalcs[0].slices[1].taxDue = 0;
            calcLeaseNonResPremAndRent_201203_201603_Results[0].taxCalcs[1].taxDue = 0;
            calcLeaseNonResPremAndRent_201203_201603_Results[0].taxCalcs[1].rate = 0;
            expect(service.calcLeaseNonResPremAndRent_201203_201603(150000, 150000, true)).toEqual(calcLeaseNonResPremAndRent_201203_201603_Results);
        });
        it(' calcLeaseNonResPremAndRent_201203_201603 should return 1500, 0 for purchase price of 150000, npv of 150000 and zeroRate is FALSE', function() {
            calcLeaseNonResPremAndRent_201203_201603_Results[0].totalTax = 1500;
            calcLeaseNonResPremAndRent_201203_201603_Results[0].npv = 150000;
            calcLeaseNonResPremAndRent_201203_201603_Results[0].taxCalcs[0].taxDue = 0;
            calcLeaseNonResPremAndRent_201203_201603_Results[0].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseNonResPremAndRent_201203_201603_Results[0].taxCalcs[0].slices[1].taxDue = 0;
            calcLeaseNonResPremAndRent_201203_201603_Results[0].taxCalcs[1].taxDue = 1500;
            calcLeaseNonResPremAndRent_201203_201603_Results[0].taxCalcs[1].rate = 1;
            expect(service.calcLeaseNonResPremAndRent_201203_201603(150000, 150000, false)).toEqual(calcLeaseNonResPremAndRent_201203_201603_Results);
        });
        it(' calcLeaseNonResPremAndRent_201203_201603 should return 20000, 1 for purchase price of 500001, npv of 150100 and zeroRate is FALSE', function() {
            calcLeaseNonResPremAndRent_201203_201603_Results[0].totalTax = 20001;
            calcLeaseNonResPremAndRent_201203_201603_Results[0].npv = 150100;
            calcLeaseNonResPremAndRent_201203_201603_Results[0].taxCalcs[0].taxDue = 1;
            calcLeaseNonResPremAndRent_201203_201603_Results[0].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseNonResPremAndRent_201203_201603_Results[0].taxCalcs[0].slices[1].taxDue = 1;
            calcLeaseNonResPremAndRent_201203_201603_Results[0].taxCalcs[1].taxDue = 20000;
            calcLeaseNonResPremAndRent_201203_201603_Results[0].taxCalcs[1].rate = 4;
            expect(service.calcLeaseNonResPremAndRent_201203_201603(500001, 150100, false)).toEqual(calcLeaseNonResPremAndRent_201203_201603_Results);
        });

        // ********************* calcLeaseNonResPremAndRent_201603_Undef *********************
        calcLeaseNonResPremAndRent_201603_Undef_Results = [
            {
                resultHeading : "Results based on SDLT rules from 17 March 2016",
                totalTax : 0,
                npv : 0,
                taxCalcs : [
                    {
                        taxType : "rent",
                        calcType : "slice",
                        detailHeading : "This is a breakdown of how the amount of SDLT on the rent was calculated based on the rules from 17 March 2016",
                        bandHeading: 'Rent bands (£)', 
                        detailFooter: 'SDLT due on the rent',
                        taxDue : 0,
                        slices : [
                            { from: 0,       to : 150000,   rate : 0,  taxDue : 0},
                            { from: 150000,  to : 5000000,  rate : 1,  taxDue : 0},
                            { from: 5000000, to : -1,       rate : 2,  taxDue : 0}
                        ]
                    },
                    {
                        taxType : "premium",
                        calcType : "slice",
                        detailHeading : "This is a breakdown of how the amount of SDLT on the premium was calculated based on the rules from 17 March 2016",
                        bandHeading: 'Premium bands (£)', 
                        detailFooter: 'SDLT due on the premium',
                        taxDue : 0,
                        slices : [
                            { from: 0,       to : 150000,   rate : 0,  taxDue : 0},
                            { from: 150000,  to : 250000,   rate : 2,  taxDue : 0},
                            { from: 250000,  to : -1,       rate : 5,  taxDue : 0}
                        ]
                    }
                ]
            },
            {
                totalTax : 0,
                npv : 0,
                taxCalcs : [
                    {
                        taxType : "rent",
                        calcType : "slice",
                        detailHeading : "This is a breakdown of how the amount of SDLT on the rent was calculated based on the rules before 17 March 2016",
                        bandHeading: 'Rent bands (£)', 
                        detailFooter: 'SDLT due on the rent',
                        taxDue : 0,
                        slices : [
                            { from: 0,       to : 150000,   rate : 0,  taxDue : 0},
                            { from: 150000,  to : -1    ,   rate : 1,  taxDue : 0}
                        ]
                    },
                    {
                        taxType : "premium",
                        calcType : "slab",
                        taxDue : 0,
                        rate : 0
                    }
                ],
                resultHeading : "Results based on SDLT rules before 17 March 2016",
                resultHint : "You may be entitled to pay SDLT using the old rules if you exchanged contracts before 17 March 2016."
            }
        ];
        it(' calcLeaseNonResPremAndRent_201603_Undef should return 48502, 48501 for premium of 150000, npv of 5,000,100 and zeroRate is TRUE, precCalc is TRUE', function() {
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].totalTax = 48502;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].npv = 5000100;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].taxCalcs[0].taxDue = 48502;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 48500;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].taxCalcs[0].slices[2].taxDue = 2;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].taxCalcs[1].taxDue = 0;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].taxCalcs[1].slices[0].taxDue = 0;
            calcLeaseNonResPremAndRent_201603_Undef_Results[1].totalTax = 48501;
            calcLeaseNonResPremAndRent_201603_Undef_Results[1].npv = 5000100;
            calcLeaseNonResPremAndRent_201603_Undef_Results[1].taxCalcs[0].taxDue = 48501;
            calcLeaseNonResPremAndRent_201603_Undef_Results[1].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseNonResPremAndRent_201603_Undef_Results[1].taxCalcs[0].slices[1].taxDue = 48501;
            calcLeaseNonResPremAndRent_201603_Undef_Results[1].taxCalcs[1].taxDue = 0;
            calcLeaseNonResPremAndRent_201603_Undef_Results[1].taxCalcs[1].rate = 0;
            expect(service.calcLeaseNonResPremAndRent_201603_Undef(150000, 5000100, true, true)).toEqual(calcLeaseNonResPremAndRent_201603_Undef_Results);
        });
        it(' calcLeaseNonResPremAndRent_201603_Undef should return 48502, 50001 for premium of 150000, npv of 5,000,100 and zeroRate is FALSE, precCalc is TRUE', function() {
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].totalTax = 48502;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].npv = 5000100;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].taxCalcs[0].taxDue = 48502;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 48500;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].taxCalcs[0].slices[2].taxDue = 2;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].taxCalcs[1].taxDue = 0;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].taxCalcs[1].slices[0].taxDue = 0;
            calcLeaseNonResPremAndRent_201603_Undef_Results[1].totalTax = 50001;
            calcLeaseNonResPremAndRent_201603_Undef_Results[1].npv = 5000100;
            calcLeaseNonResPremAndRent_201603_Undef_Results[1].taxCalcs[0].taxDue = 48501;
            calcLeaseNonResPremAndRent_201603_Undef_Results[1].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseNonResPremAndRent_201603_Undef_Results[1].taxCalcs[0].slices[1].taxDue = 48501;
            calcLeaseNonResPremAndRent_201603_Undef_Results[1].taxCalcs[1].taxDue = 1500;
            calcLeaseNonResPremAndRent_201603_Undef_Results[1].taxCalcs[1].rate = 1;
            expect(service.calcLeaseNonResPremAndRent_201603_Undef(150000, 5000100, false, true)).toEqual(calcLeaseNonResPremAndRent_201603_Undef_Results);
        });
        it(' calcLeaseNonResPremAndRent_201603_Undef should return 63007, 68505 for premium of 500,100, npv of 5,000,100 and zeroRate is FALSE, precCalc is TRUE', function() {
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].totalTax = 63007;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].npv = 5000100;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].taxCalcs[0].taxDue = 48502;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 48500;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].taxCalcs[0].slices[2].taxDue = 2;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].taxCalcs[1].taxDue = 14505;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].taxCalcs[1].slices[0].taxDue = 0;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].taxCalcs[1].slices[1].taxDue = 2000;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].taxCalcs[1].slices[2].taxDue = 12505;
            calcLeaseNonResPremAndRent_201603_Undef_Results[1].totalTax = 68505;
            calcLeaseNonResPremAndRent_201603_Undef_Results[1].npv = 5000100;
            calcLeaseNonResPremAndRent_201603_Undef_Results[1].taxCalcs[0].taxDue = 48501;
            calcLeaseNonResPremAndRent_201603_Undef_Results[1].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseNonResPremAndRent_201603_Undef_Results[1].taxCalcs[0].slices[1].taxDue = 48501;
            calcLeaseNonResPremAndRent_201603_Undef_Results[1].taxCalcs[1].taxDue = 20004;
            calcLeaseNonResPremAndRent_201603_Undef_Results[1].taxCalcs[1].rate = 4;
            expect(service.calcLeaseNonResPremAndRent_201603_Undef(500100, 5000100, false, true)).toEqual(calcLeaseNonResPremAndRent_201603_Undef_Results);
        });

        it(' calcLeaseNonResPremAndRent_201603_Undef should return 63007, N/A for premium of 500,100, npv of 5,000,100 and zeroRate is FALSE, precCalc is FALSE', function() {
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].totalTax = 63007;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].npv = 5000100;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].taxCalcs[0].taxDue = 48502;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].taxCalcs[0].slices[0].taxDue = 0;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].taxCalcs[0].slices[1].taxDue = 48500;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].taxCalcs[0].slices[2].taxDue = 2;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].taxCalcs[1].taxDue = 14505;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].taxCalcs[1].slices[0].taxDue = 0;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].taxCalcs[1].slices[1].taxDue = 2000;
            calcLeaseNonResPremAndRent_201603_Undef_Results[0].taxCalcs[1].slices[2].taxDue = 12505;
            calcLeaseNonResPremAndRent_201603_Undef_Results.pop();
            expect(service.calcLeaseNonResPremAndRent_201603_Undef(500100, 5000100, false, false)).toEqual(calcLeaseNonResPremAndRent_201603_Undef_Results);
        });
  });
}());
