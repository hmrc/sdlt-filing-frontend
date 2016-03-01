(function() {
    "use strict";



    var app = require("./module");

    app.service('calculationService', function(){

        var calcFreeResPrem_201203_201412 = function(premium){

            var slabsArray = [
                    { threshold : 2000000,   rate : 7},
                    { threshold : 1000000,   rate : 5},
                    { threshold : 500000,    rate : 4},
                    { threshold : 250000,    rate : 3},
                    { threshold : 125000,    rate : 1},
                    { threshold : -1,        rate : 0}
            ];

            var calcResult = calculateTaxDueSlab(premium, slabsArray);

            var taxCalc = {taxType : 'premium', calcType : 'slab', taxDue : 0, rate : 0};
            taxCalc.taxDue = calcResult.taxDue;
            taxCalc.rate = calcResult.rate;

            var taxCalcs = [taxCalc];

            var result = {};
            result.totalTax = calcResult.taxDue; 
            result.taxCalcs = taxCalcs;

            return [result];
        };

        // this applies to ALL calcs up to 201604 and Main Res props from 201604
        var calcFreeResPrem_201412_Undef = function(premium){

            var slicesArray = [
                    { from: 0,       to : 125000,   rate : 0,  taxDue : -1},
                    { from: 125000,  to : 250000,   rate : 2,  taxDue : -1},
                    { from: 250000,  to : 925000,   rate : 5,  taxDue : -1},
                    { from: 925000,  to : 1500000,  rate : 10, taxDue : -1},
                    { from: 1500000, to : -1,       rate : 12, taxDue : -1}
            ];

            var calcResult = calculateTaxDueSlice(premium, slicesArray);

            var taxCalc = {taxType : 'premium', calcType : 'slice', taxDue : 0, slices : []};
            taxCalc.taxDue = calcResult.taxDue;
            taxCalc.slices = calcResult.slices;

            var taxCalcs = [taxCalc];

            var result = {};
            result.totalTax = calcResult.taxDue; 
            result.taxCalcs = taxCalcs;

            return [result];
        };


        // from 201604 Additional properties are charged at higher rate.
        // There is a transitional period for exchanged contracts < 16/03/2016 so we also calculate using the old rates
        var calcFreeResPremAddProp_201604_Undef = function(premium){

            var slicesArray = [
                    { from: 0,       to : 125000,   rate : 3,  taxDue : -1},
                    { from: 125000,  to : 250000,   rate : 5,  taxDue : -1},
                    { from: 250000,  to : 925000,   rate : 8,  taxDue : -1},
                    { from: 925000,  to : 1500000,  rate : 13, taxDue : -1},
                    { from: 1500000, to : -1,       rate : 15, taxDue : -1}
            ];

            // if premium <£40,000 then exempt from filing  a return / paying SDLT
            if (premium < 40000) {
                premium = 0;
            }
            var calcResult = calculateTaxDueSlice(premium, slicesArray);

            var taxCalc = {taxType : "premium", calcType : 'slice', detailHeading : '', taxDue : 0, slices : []};
            taxCalc.detailHeading = 'This is a breakdown of how the amount of SDLT was calculated based on the rules from 1 April 2016';
            taxCalc.taxDue = calcResult.taxDue;
            taxCalc.slices = calcResult.slices;

            var taxCalcs = [taxCalc];

            var result = {};
            result.resultHeading = "Results based on SDLT rules from 1 April 2016";
            result.totalTax = calcResult.taxDue; 
            result.taxCalcs = taxCalcs;

            // calculation for previous rate. Uses rates from 201412 onwards but needs headings/hints adding
            var prevRatesArray = calcFreeResPrem_201412_Undef(premium);
            var prevRatesResult = prevRatesArray[0];
            prevRatesResult.resultHeading = "Results based on SDLT rules before 1 April 2016";
            prevRatesResult.resultHint = "You may be entitled to pay SDLT using the old rules if you exchanged contracts before 26 November 2015.";
            prevRatesResult.taxCalcs[0].detailHeading = "This is a breakdown of how the amount of SDLT was calculated based on the rules before 1 April 2016";

            return [result, prevRatesResult];
        };

        var calcFreeNonResPrem_201203_Undef = function(premium){

            var slabsArray = [
                    { threshold : 500000, rate : 4},
                    { threshold : 250000, rate : 3},
                    { threshold : 150000, rate : 1},
                    { threshold : -1,     rate : 0}
            ];

            var calcResult = calculateTaxDueSlab(premium, slabsArray);

            var taxCalc = {taxType : "premium", calcType : 'slab', taxDue : 0, rate : 0};
            taxCalc.taxDue = calcResult.taxDue;
            taxCalc.rate = calcResult.rate;

            var taxCalcs = [taxCalc];

            var result = {};
            result.totalTax = calcResult.taxDue; 
            result.taxCalcs = taxCalcs;

            return [result];
        };

        var calcLeaseResPremAndRent_201203_201412 = function(premium, npv){

            var rentSlicesArray = [
                    { from: 0,       to : 125000,   rate : 0,  taxDue : -1},
                    { from: 125000,  to : -1    ,   rate : 1,  taxDue : -1}
            ];

            var rentResult = calculateTaxDueSlice(npv, rentSlicesArray);
            var rentCalc = {taxType : "rent", calcType : 'slice', taxDue : 0, slices : []};
            rentCalc.taxDue = rentResult.taxDue;
            rentCalc.slices = rentResult.slices;

            var premSlabsArray = [
                    { threshold : 2000000,   rate : 7},
                    { threshold : 1000000,   rate : 5},
                    { threshold : 500000,    rate : 4},
                    { threshold : 250000,    rate : 3},
                    { threshold : 125000,    rate : 1},
                    { threshold : -1,         rate : 0}
            ];

            var premResult = calculateTaxDueSlab(premium, premSlabsArray);
            var premiumCalc = {taxType : "premium", calcType : 'slab', taxDue : 0, rate : 0};
            premiumCalc.taxDue = premResult.taxDue;
            premiumCalc.rate = premResult.rate;

            var taxCalcs = [rentCalc, premiumCalc];

            var result = {};
            result.totalTax = rentCalc.taxDue + premiumCalc.taxDue; 
            result.npv = npv;
            result.taxCalcs = taxCalcs;

            return [result];
        };

        var calcLeaseResPremAndRent_201412_Undef = function(premium, npv){

            var rentSlicesArray = [
                    { from: 0,       to : 125000,   rate : 0,  taxDue : -1},
                    { from: 125000,  to : -1    ,   rate : 1,  taxDue : -1}
            ];

            var rentResult = calculateTaxDueSlice(npv, rentSlicesArray);
            var rentCalc = {taxType : "rent", calcType : 'slice', taxDue : 0, slices : []};
            rentCalc.taxDue = rentResult.taxDue;
            rentCalc.slices = rentResult.slices;

            var premSlicesArray = [
                    { from: 0,       to : 125000,   rate : 0,  taxDue : -1},
                    { from: 125000,  to : 250000,   rate : 2,  taxDue : -1},
                    { from: 250000,  to : 925000,   rate : 5,  taxDue : -1},
                    { from: 925000,  to : 1500000,  rate : 10, taxDue : -1},
                    { from: 1500000, to : -1,       rate : 12, taxDue : -1}
            ];

            var premResult = calculateTaxDueSlice(premium, premSlicesArray);
            var premiumCalc = {taxType : "premium", calcType : 'slice', taxDue : 0, slices : []};
            premiumCalc.taxDue = premResult.taxDue;
            premiumCalc.slices = premResult.slices;

            var taxCalcs = [rentCalc, premiumCalc];

            var result = {};
            result.totalTax = rentCalc.taxDue + premiumCalc.taxDue; 
            result.npv = npv;
            result.taxCalcs = taxCalcs;

            return [result];
        };

        // from 201604 Additional properties are charged at higher rate.
        // There is a transitional period for exchanged contracts < 16/03/2016 so we also calculate using the old rates
        var calcLeaseResPremAndRentAddProp_201604_Undef = function(premium, npv){

            var rentSlicesArray = [
                    { from: 0,       to : 125000,   rate : 0,  taxDue : -1},
                    { from: 125000,  to : -1    ,   rate : 1,  taxDue : -1}
            ];

            var rentResult = calculateTaxDueSlice(npv, rentSlicesArray);
            var rentCalc = {taxType : 'rent', calcType : 'slice', detailHeading : '', taxDue : 0, slices : []};
            rentCalc.detailHeading = "This is a breakdown of how the amount of SDLT on rent was calculated based on the rules from 1 April 2016";
            rentCalc.taxDue = rentResult.taxDue;
            rentCalc.slices = rentResult.slices;

            var premSlicesArray = [
                    { from: 0,       to : 125000,   rate : 3,  taxDue : -1},
                    { from: 125000,  to : 250000,   rate : 5,  taxDue : -1},
                    { from: 250000,  to : 925000,   rate : 8,  taxDue : -1},
                    { from: 925000,  to : 1500000,  rate : 13, taxDue : -1},
                    { from: 1500000, to : -1,       rate : 15, taxDue : -1}
            ];

            // if premium <£40,000 then exempt from filing  a return / paying SDLT
            if (premium < 40000) {
                premium = 0;
            }
            var calcResult = calculateTaxDueSlice(premium, premSlicesArray);

            var premiumCalc = {taxType : 'premium', calcType : 'slice', detailHeading : '', taxDue : 0, slices : []};
            premiumCalc.detailHeading = 'This is a breakdown of how the amount of SDLT on premium was calculated based on the rules from 1 April 2016';
            premiumCalc.taxDue = calcResult.taxDue;
            premiumCalc.slices = calcResult.slices;

            var taxCalcs = [rentCalc, premiumCalc];

            var result = {};
            result.resultHeading = "Results based on SDLT rules from 1 April 2016";
            result.totalTax = rentCalc.taxDue + premiumCalc.taxDue;
            result.npv = npv;
            result.taxCalcs = taxCalcs;

            // calculation for previous rate. Uses rates from 201412 onwards but needs headings/hints adding
            var prevRatesArray = calcLeaseResPremAndRent_201412_Undef(premium, npv);
            var prevRatesResult = prevRatesArray[0];
            prevRatesResult.resultHeading = "Results based on SDLT rules before 1 April 2016";
            prevRatesResult.resultHint = "You may be entitled to pay SDLT using the old rules if you exchanged contracts before 26 November 2015.";
            prevRatesResult.taxCalcs[0].detailHeading = "This is a breakdown of how the amount of SDLT on rent was calculated based on the rules before 1 April 2016";
            prevRatesResult.taxCalcs[1].detailHeading = "This is a breakdown of how the amount of SDLT on premium was calculated based on the rules before 1 April 2016";

            return [result, prevRatesResult];
        };

        var calcLeaseNonResPremAndRent_201203_Undef = function(premium, npv, zeroRate){

            var rentSlicesArray = [
                    { from: 0,       to : 150000,   rate : 0,  taxDue : -1},
                    { from: 150000,  to : -1    ,   rate : 1,  taxDue : -1}
            ];

            var rentResult = calculateTaxDueSlice(npv, rentSlicesArray);
            var rentCalc = {taxType : "rent", calcType : 'slice', taxDue : 0, slices : []};
            rentCalc.taxDue = rentResult.taxDue;
            rentCalc.slices = rentResult.slices;

            var premSlabsArray = [
                    { threshold : 500000, rate : 4},
                    { threshold : 250000, rate : 3},
                    { threshold : -1,     rate : 1}
            ];

            var premiumCalc = {taxType : "premium", calcType : 'slab', taxDue : 0, rate : 0};
            if ( !zeroRate ) {
                var premResult = calculateTaxDueSlab(premium, premSlabsArray);
                premiumCalc.taxDue = premResult.taxDue;
                premiumCalc.rate = premResult.rate;
            }
            var taxCalcs = [rentCalc, premiumCalc];

            var result = {};
            result.totalTax = rentCalc.taxDue + premiumCalc.taxDue; 
            result.npv = npv;
            result.taxCalcs = taxCalcs;

            return [result];
        };



        var calculateResidentialPremiumSlab = function(premium){

            var slabsArray = [
                    { threshold : 2000000,   rate : 7},
                    { threshold : 1000000,   rate : 5},
                    { threshold : 500000,    rate : 4},
                    { threshold : 250000,    rate : 3},
                    { threshold : 125000,    rate : 1},
                    { threshold : -1,         rate : 0}
            ];

            var resultJSON = calculateTaxDueSlab(premium, slabsArray);
            return resultJSON;

        };

        var calculateResidentialPremiumSlice = function(premium){
            var slicesArray = [
                    { from: 0,       to : 125000,   rate : 0,  taxDue : -1},
                    { from: 125000,  to : 250000,   rate : 2,  taxDue : -1},
                    { from: 250000,  to : 925000,   rate : 5,  taxDue : -1},
                    { from: 925000,  to : 1500000,  rate : 10, taxDue : -1},
                    { from: 1500000, to : -1,       rate : 12, taxDue : -1}
            ];

            var resultJSON = calculateTaxDueSliceOLD(premium, slicesArray);
            return resultJSON;
        };

        var calculateNonResidentialPremiumSlab = function(premium, zeroRate){

            var slabsArray = [
                    { threshold : 500000, rate : 4},
                    { threshold : 250000, rate : 3},
                    { threshold : -1,     rate : 1}
            ];

            if ( (premium <= 150000) && zeroRate) {
                var noTaxResults = {
                    rate : 0,
                    taxDue : 0
                };
                return noTaxResults;
            } else {
                var resultJSON = calculateTaxDueSlab(premium, slabsArray);
                return resultJSON;
            }
        };

        var calculateResidentialLeaseSlice = function(npv){

            var slicesArray = [
                    { from: 0,       to : 125000,   rate : 0,  taxDue : -1},
                    { from: 125000,  to : -1    ,   rate : 1,  taxDue : -1}
            ];

            var resultJSON = calculateTaxDueSliceOLD(npv, slicesArray);
            return resultJSON;
        };

        var calculateNonResidentialLeaseSlice = function(npv){

            var slicesArray = [
                    { from: 0,       to : 150000,   rate : 0,  taxDue : -1},
                    { from: 150000,  to : -1    ,   rate : 1,  taxDue : -1}
            ];

            var resultJSON = calculateTaxDueSliceOLD(npv, slicesArray);
            return resultJSON;
        };

        var calculate201604SecondHomeSlice = function(premium){
            var slicesArray = [
                    { from: 0,       to : 125000,   rate : 3,  taxDue : -1},
                    { from: 125000,  to : 250000,   rate : 5,  taxDue : -1},
                    { from: 250000,  to : 925000,   rate : 8,  taxDue : -1},
                    { from: 925000,  to : 1500000,  rate : 13, taxDue : -1},
                    { from: 1500000, to : -1,       rate : 15, taxDue : -1}
            ];

            if (premium < 40000) {
                premium = 0;
            }
            var resultJSON = calculateTaxDueSliceOLD(premium, slicesArray);
            return resultJSON;
        };

        var calculateTaxDueSlab = function(amount, slabs) {

            var slabRate = -1;
            var slabTaxDue = -1;

            for (var i = 0; i < slabs.length; i++) {
                if (amount > slabs[i].threshold)
                {
                    slabRate = slabs[i].rate;
                    slabTaxDue = calcTax(amount, slabs[i].rate);
                    break;
                }
            }

            var slabResults = {
                rate : slabRate,
                taxDue : slabTaxDue
            };

            return slabResults;
        };

        var calculateTaxDueSliceOLD = function(amount, slices) {

            var sliceAmount = -1;
            var sliceTaxDue = -1;
            var totalTaxDue = 0;

            for (var i = 0; i < slices.length; i++) {
                if (slices[i].to == -1) {
                    if (amount > slices[i].from) {
                        sliceAmount = amount - slices[i].from;
                        sliceTaxDue = calcTax(sliceAmount, slices[i].rate);
                        slices[i].taxDue = sliceTaxDue;
                        totalTaxDue += sliceTaxDue;
                    } else {
                        slices[i].taxDue = 0;
                    }
                } else if (amount > slices[i].to) {   
                    sliceAmount = slices[i].to - slices[i].from;
                    sliceTaxDue = calcTax(sliceAmount, slices[i].rate);
                    slices[i].taxDue = sliceTaxDue;
                    totalTaxDue += sliceTaxDue;
                } else if (amount <= slices[i].from) {    
                    slices[i].taxDue = 0;
                } else { 
                    sliceAmount = amount - slices[i].from;
                    sliceTaxDue = calcTax(sliceAmount, slices[i].rate);
                    slices[i].taxDue = sliceTaxDue;
                    totalTaxDue += sliceTaxDue;
                }
            }

            var resultJSON = { totalSDLT : totalTaxDue, 
                               slices : slices
            };
            return resultJSON;
        };

        var calculateTaxDueSlice = function(amount, slices) {

            var sliceAmount = -1;
            var sliceTaxDue = -1;
            var totalTaxDue = 0;

            for (var i = 0; i < slices.length; i++) {
                if (slices[i].to == -1) {
                    if (amount > slices[i].from) {
                        sliceAmount = amount - slices[i].from;
                        sliceTaxDue = calcTax(sliceAmount, slices[i].rate);
                        slices[i].taxDue = sliceTaxDue;
                        totalTaxDue += sliceTaxDue;
                    } else {
                        slices[i].taxDue = 0;
                    }
                } else if (amount > slices[i].to) {   
                    sliceAmount = slices[i].to - slices[i].from;
                    sliceTaxDue = calcTax(sliceAmount, slices[i].rate);
                    slices[i].taxDue = sliceTaxDue;
                    totalTaxDue += sliceTaxDue;
                } else if (amount <= slices[i].from) {    
                    slices[i].taxDue = 0;
                } else { 
                    sliceAmount = amount - slices[i].from;
                    sliceTaxDue = calcTax(sliceAmount, slices[i].rate);
                    slices[i].taxDue = sliceTaxDue;
                    totalTaxDue += sliceTaxDue;
                }
            }

            var resultJSON = { taxDue : totalTaxDue,
                               slices : slices
            };
            return resultJSON;
        };

        function calcTax(amount, rate) {
          return (Math.floor(Math.floor(amount) * rate / 100));
        }

        function calculateNPV(fullYears, partialDays, daysInPartialYear, rentsArray) {

            var totalNPV = 0;
            var DIVISOR_RATE = 1.035;
            var divisor = 1.0;
            var highRentFirst5 = 0;
            var rentPartialYear = 0;

            for (var i = 0; i <= 4; i++) {
                divisor = divisor * DIVISOR_RATE;
                totalNPV += Math.floor(rentsArray[i] * 1000 / divisor) / 1000;
  
                if (rentsArray[i] > highRentFirst5) {
                    highRentFirst5 = rentsArray[i];
                }
            }

            if (fullYears > 5) {
                for (var j = 6; j <= fullYears; j++) {
                    divisor = divisor * DIVISOR_RATE;
                    totalNPV +=  Math.floor(highRentFirst5 * 1000 / divisor) / 1000;
                }
            }

            if ((fullYears >= 5) && (partialDays > 0)) {
                divisor = divisor * DIVISOR_RATE;
                rentPartialYear = highRentFirst5 * partialDays / daysInPartialYear;
                totalNPV += Math.floor(rentPartialYear / divisor);
            }

            return Math.floor(totalNPV);
        }

        return {
            calculateResidentialPremiumSlab : calculateResidentialPremiumSlab,
            calculateResidentialPremiumSlice : calculateResidentialPremiumSlice,
            calculateNonResidentialPremiumSlab : calculateNonResidentialPremiumSlab,
            calculateResidentialLeaseSlice : calculateResidentialLeaseSlice,
            calculateNonResidentialLeaseSlice : calculateNonResidentialLeaseSlice,
            calculate201604SecondHomeSlice : calculate201604SecondHomeSlice,
            calculateNPV : calculateNPV,
            calcFreeResPrem_201203_201412 : calcFreeResPrem_201203_201412,
            calcFreeResPrem_201412_Undef : calcFreeResPrem_201412_Undef,
            calcFreeResPremAddProp_201604_Undef : calcFreeResPremAddProp_201604_Undef,
            calcFreeNonResPrem_201203_Undef : calcFreeNonResPrem_201203_Undef,
            calcLeaseResPremAndRent_201203_201412 : calcLeaseResPremAndRent_201203_201412,
            calcLeaseResPremAndRent_201412_Undef : calcLeaseResPremAndRent_201412_Undef,
            calcLeaseResPremAndRentAddProp_201604_Undef : calcLeaseResPremAndRentAddProp_201604_Undef,
            calcLeaseNonResPremAndRent_201203_Undef : calcLeaseNonResPremAndRent_201203_Undef
        };
    });
})();
