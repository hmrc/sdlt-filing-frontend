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
                    { "threshold" : 0,         "rate" : 0}
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

        var calculateNonResidentialPremiumSlab = function(premium, rent){

            var slabsArray = [
                    { "threshold" : 500000, "rate" : 4},
                    { "threshold" : 250000, "rate" : 3},
                    { "threshold" : 0,      "rate" : 1}
            ];

            if ( (premium <= 150000) && (rent <= 1000) ) {
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

        var calculateResidentialLeaseSlab = function(npv, rent){

            var slabsArray = [
                    { "threshold" : 125000, "rate" : 1},
                    { "threshold" : 0,      "rate" : 0}
            ];

            var resultJSON = calculateTaxDueSlab(npv, slabsArray);
            return resultJSON;
        };

        var calculateNonResidentialLeaseSlab = function(npv, rent){

            var slabsArray = [
                    { "threshold" : 150000, "rate" : 1},
                    { "threshold" : 0,      "rate" : 0}
            ];

            var resultJSON = calculateTaxDueSlab(npv, slabsArray);
            return resultJSON;
        };


        var calculateTaxDueSlab = function(amount, slabs) {

            var slabRate = -1;
            var slabTaxDue = -1;

            for (i = 0; i < slabs.length; i++) {
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

            for (i = 0; i < slices.length; i++) {
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
                    // full tax for slice
                    sliceAmount = slices[i].to - slices[i].from;
                    sliceTaxDue = calcTax(sliceAmount, slices[i].rate);
                    slices[i].taxDue = sliceTaxDue;
                    totalTaxDue += sliceTaxDue;
                } else if (amount <= slices[i].from) {    
                    // no tax for slice
                    slices[i].taxDue = 0;
                } else { 
                    // amount is between from and to
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

        // function round2(amount) {
        //   return (Math.round(amount * 100) / 100);
        // }

        // function roundUp2(amount) {
        //   return (Math.ceil(amount * 100) / 100);
        // }

        // function roundDown2(amount) {
        //   return (Math.floor(amount * 100) / 100);
        // }

        function calcTax(amount, rate) {
          return (Math.floor(Math.floor(amount) * rate / 100));
        }

        return {
            calculateResidentialPremiumSlab : calculateResidentialPremiumSlab,
            calculateResidentialPremiumSlice : calculateResidentialPremiumSlice,
            calculateNonResidentialPremiumSlab : calculateNonResidentialPremiumSlab,
            calculateResidentialLeaseSlab : calculateResidentialLeaseSlab,
            calculateNonResidentialLeaseSlab : calculateNonResidentialLeaseSlab

        };
    });
})();
