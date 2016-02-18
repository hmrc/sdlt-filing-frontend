(function() {
    "use strict";

    var app = require("./module");

    app.service('calculationService', function(){

        var calculateResidentialPremiumSlab = function(premium){

            var slabsArray = [
                    { "threshold" : 2000000,   "rate" : 7},
                    { "threshold" : 1000000,   "rate" : 5},
                    { "threshold" : 500000,    "rate" : 4},
                    { "threshold" : 250000,    "rate" : 3},
                    { "threshold" : 125000,    "rate" : 1},
                    { "threshold" : -1,         "rate" : 0}
            ];

            var resultJSON = calculateTaxDueSlab(premium, slabsArray);
            return resultJSON;

        };

        var calculateResidentialPremiumSlice = function(premium){
            var slicesArray = [
                    { "from": 0,       "to" : 125000,   "rate" : 0,  "taxDue" : -1},
                    { "from": 125000,  "to" : 250000,   "rate" : 2,  "taxDue" : -1},
                    { "from": 250000,  "to" : 925000,   "rate" : 5,  "taxDue" : -1},
                    { "from": 925000,  "to" : 1500000,  "rate" : 10, "taxDue" : -1},
                    { "from": 1500000, "to" : -1,       "rate" : 12, "taxDue" : -1}
            ];

            var resultJSON = calculateTaxDueSlice(premium, slicesArray);
            return resultJSON;
        };

        var calculateNonResidentialPremiumSlab = function(premium, zeroRate){

            var slabsArray = [
                    { "threshold" : 500000, "rate" : 4},
                    { "threshold" : 250000, "rate" : 3},
                    { "threshold" : -1,     "rate" : 1}
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
                    { "from": 0,       "to" : 125000,   "rate" : 0,  "taxDue" : -1},
                    { "from": 125000,  "to" : -1    ,   "rate" : 1,  "taxDue" : -1}
            ];

            var resultJSON = calculateTaxDueSlice(npv, slicesArray);
            return resultJSON;
        };

        var calculateNonResidentialLeaseSlice = function(npv){

            var slicesArray = [
                    { "from": 0,       "to" : 150000,   "rate" : 0,  "taxDue" : -1},
                    { "from": 150000,  "to" : -1    ,   "rate" : 1,  "taxDue" : -1}
            ];

            var resultJSON = calculateTaxDueSlice(npv, slicesArray);
            return resultJSON;
        };

        var calculate201604SecondHomeSlice = function(premium){
            var slicesArray = [
                    { "from": 0,       "to" : 125000,   "rate" : 3,  "taxDue" : -1},
                    { "from": 125000,  "to" : 250000,   "rate" : 5,  "taxDue" : -1},
                    { "from": 250000,  "to" : 925000,   "rate" : 8,  "taxDue" : -1},
                    { "from": 925000,  "to" : 1500000,  "rate" : 13, "taxDue" : -1},
                    { "from": 1500000, "to" : -1,       "rate" : 15, "taxDue" : -1}
            ];

            if (premium < 40000) {
                premium = 0;
            }
            var resultJSON = calculateTaxDueSlice(premium, slicesArray);
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

            var resultJSON = { "totalSDLT" : totalTaxDue,
                                "slices" : slices
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
            calculateNPV : calculateNPV
        };
    });
})();
