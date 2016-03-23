(function() {
    "use strict";

    var app = require("./module");

    app.service('calculationService', function(){

        // Heading and Hint component parts
        var _DATE_04_12_2014             = '4 December 2014';
        var _DATE_01_04_2016             = '1 April 2016';
        var _DATE_26_11_2015             = '26 November 2015';
        var _DATE_17_03_2016             = '17 March 2016';

        var RESULT_HEADING_BEFORE_       = 'Results based on SDLT rules before ';
        var RESULT_HEADING_FROM_         = 'Results based on SDLT rules from ';

        var RESULT_HINT_ADDNL_PROP_REFUND_ = 'If you dispose of your previous main residence within 3 years you may be eligible for a refund of £';
        var RESULT_HINT_EXCHANGE_BEFORE_   = 'You may be entitled to pay SDLT using the old rules if you exchanged contracts before ';

        var _BASED_ON_THE_RULES_FROM_     = ' based on the rules from ';
        var _BASED_ON_THE_RULES_BEFORE_   = ' based on the rules before ';

        // Heading and Hint usable values
        var RESULT_HEADING_BEFORE_DEC_2014 = RESULT_HEADING_BEFORE_ + _DATE_04_12_2014;
        var RESULT_HEADING_FROM_DEC_2014   = RESULT_HEADING_FROM_ + _DATE_04_12_2014;
        var RESULT_HEADING_BEFORE_MAR_2016 = RESULT_HEADING_BEFORE_ + _DATE_17_03_2016;
        var RESULT_HEADING_FROM_MAR_2016   = RESULT_HEADING_FROM_ + _DATE_17_03_2016;
        var RESULT_HEADING_BEFORE_APR_2016 = RESULT_HEADING_BEFORE_ + _DATE_01_04_2016;
        var RESULT_HEADING_FROM_APR_2016   = RESULT_HEADING_FROM_ + _DATE_01_04_2016;

        var RESULT_HINT_EXCHANGE_BEFORE_NOV_2015 = RESULT_HINT_EXCHANGE_BEFORE_ + _DATE_26_11_2015 + ".";
        var RESULT_HINT_EXCHANGE_BEFORE_MAR_2016 = RESULT_HINT_EXCHANGE_BEFORE_ + _DATE_17_03_2016 + ".";

        var DETAIL_HEADING_TOTAL_SDLT   = 'This is a breakdown of how the total amount of SDLT was calculated';
        var DETAIL_HEADING_SDLT_ON_RENT = 'This is a breakdown of how the amount of SDLT on the rent was calculated';
        var DETAIL_HEADING_SDLT_ON_PREM = 'This is a breakdown of how the amount of SDLT on the premium was calculated';

        var DETAIL_HEADING_TOTAL_SDLT_FROM_MAR_2016   = DETAIL_HEADING_TOTAL_SDLT + _BASED_ON_THE_RULES_FROM_ + _DATE_17_03_2016;
        var DETAIL_HEADING_SDLT_ON_RENT_FROM_MAR_2016 = DETAIL_HEADING_SDLT_ON_RENT + _BASED_ON_THE_RULES_FROM_ + _DATE_17_03_2016;
        var DETAIL_HEADING_SDLT_ON_PREM_FROM_MAR_2016 = DETAIL_HEADING_SDLT_ON_PREM + _BASED_ON_THE_RULES_FROM_ + _DATE_17_03_2016;

        var DETAIL_HEADING_SDLT_ON_RENT_BEFORE_MAR_2016 = DETAIL_HEADING_SDLT_ON_RENT + _BASED_ON_THE_RULES_BEFORE_ + _DATE_17_03_2016;
        var DETAIL_HEADING_SDLT_ON_PREM_BEFORE_MAR_2016 = DETAIL_HEADING_SDLT_ON_PREM + _BASED_ON_THE_RULES_BEFORE_ + _DATE_17_03_2016;

        var DETAIL_HEADING_TOTAL_SDLT_FROM_APR_2016   = DETAIL_HEADING_TOTAL_SDLT + _BASED_ON_THE_RULES_FROM_ + _DATE_01_04_2016;
        var DETAIL_HEADING_SDLT_ON_RENT_FROM_APR_2016 = DETAIL_HEADING_SDLT_ON_RENT + _BASED_ON_THE_RULES_FROM_ + _DATE_01_04_2016;
        var DETAIL_HEADING_SDLT_ON_PREM_FROM_APR_2016 = DETAIL_HEADING_SDLT_ON_PREM + _BASED_ON_THE_RULES_FROM_ + _DATE_01_04_2016;

        var DETAIL_HEADING_TOTAL_SDLT_BEFORE_APR_2016   = DETAIL_HEADING_TOTAL_SDLT + _BASED_ON_THE_RULES_BEFORE_ + _DATE_01_04_2016;
        var DETAIL_HEADING_SDLT_ON_RENT_BEFORE_APR_2016 = DETAIL_HEADING_SDLT_ON_RENT + _BASED_ON_THE_RULES_BEFORE_ + _DATE_01_04_2016;
        var DETAIL_HEADING_SDLT_ON_PREM_BEFORE_APR_2016 = DETAIL_HEADING_SDLT_ON_PREM + _BASED_ON_THE_RULES_BEFORE_ + _DATE_01_04_2016;

        var DETAIL_COL_HEADER_PREM           = 'Premium bands (£)';
        var DETAIL_COL_HEADER_PURCHASE_PRICE = 'Purchase price bands (£)';
        var DETAIL_COL_HEADER_RENT           = 'Rent bands (£)';

        var DETAIL_FOOTER_TOTAL          = 'Total SDLT due';
        var DETAIL_FOOTER_PREM           = 'SDLT due on the premium';
        var DETAIL_FOOTER_RENT           = 'SDLT due on the rent';

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

            var premResult = calculateTaxDueSlice(premium, slicesArray);

            var premCalc = {taxType : 'premium', calcType : 'slice', detailHeading : '', bandHeading : '', detailFooter : '', taxDue : 0, slices : []};
            premCalc.detailHeading = DETAIL_HEADING_TOTAL_SDLT;
            premCalc.bandHeading = DETAIL_COL_HEADER_PURCHASE_PRICE;
            premCalc.detailFooter = DETAIL_FOOTER_TOTAL;
            premCalc.taxDue = premResult.taxDue;
            premCalc.slices = premResult.slices;

            var taxCalcs = [premCalc];

            var result = {};
            result.totalTax = premResult.taxDue; 
            result.taxCalcs = taxCalcs;

            return [result];
        };


        // from 201604 Additional properties are charged at higher rate.
        // There is a transitional period for exchanged contracts < 25/11/2015 so we also calculate using the old rates
        var calcFreeResPremAddProp_201604_Undef = function(premium, isIndividual){

            // calculation for previous rate. Uses rates from 201412 onwards but needs headings/hints adding
            var prevRatesArray = calcFreeResPrem_201412_Undef(premium);
            var prevRatesResult = prevRatesArray[0];
            prevRatesResult.resultHeading = RESULT_HEADING_BEFORE_APR_2016;
            prevRatesResult.resultHint = RESULT_HINT_EXCHANGE_BEFORE_NOV_2015;
            prevRatesResult.taxCalcs[0].detailHeading = DETAIL_HEADING_TOTAL_SDLT_BEFORE_APR_2016;

            // calculation for current rate
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
            var premResult = calculateTaxDueSlice(premium, slicesArray);

            var premCalc = {taxType : 'premium', calcType : 'slice', detailHeading : '', bandHeading : '', detailFooter : '', taxDue : 0, slices : []};
            premCalc.detailHeading = DETAIL_HEADING_TOTAL_SDLT_FROM_APR_2016;
            premCalc.bandHeading = DETAIL_COL_HEADER_PURCHASE_PRICE;
            premCalc.detailFooter = DETAIL_FOOTER_TOTAL;
            premCalc.taxDue = premResult.taxDue;
            premCalc.slices = premResult.slices;

            var taxCalcs = [premCalc];

            var result = {};
            result.resultHeading = RESULT_HEADING_FROM_APR_2016;
            if (isIndividual && (premCalc.taxDue > prevRatesResult.taxCalcs[0].taxDue)) {
               result.resultHint = RESULT_HINT_ADDNL_PROP_REFUND_ + (premCalc.taxDue - prevRatesResult.taxCalcs[0].taxDue).toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",") +".";
            }
            result.totalTax = premResult.taxDue; 
            result.taxCalcs = taxCalcs;

            return [result, prevRatesResult];
        };

        var calcFreeNonResPrem_201203_201603 = function(premium){

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

        // There is a transitional period for exchanged contracts < 17/03/2016 so we also calculate using the old rates
        var calcFreeNonResPrem_201603_Undef = function(premium){

            var slicesArray = [
                    { from: 0,       to : 150000,   rate : 0,  taxDue : -1},
                    { from: 150000,  to : 250000,   rate : 2,  taxDue : -1},
                    { from: 250000,  to : -1,       rate : 5,  taxDue : -1}
            ];

            var premResult = calculateTaxDueSlice(premium, slicesArray);

            var premCalc = {taxType : 'premium', calcType : 'slice', detailHeading : '', bandHeading : '', detailFooter : '', taxDue : 0, slices : []};
            premCalc.detailHeading = DETAIL_HEADING_TOTAL_SDLT_FROM_MAR_2016;
            premCalc.bandHeading = DETAIL_COL_HEADER_PURCHASE_PRICE;
            premCalc.detailFooter = DETAIL_FOOTER_TOTAL;
            premCalc.taxDue = premResult.taxDue;
            premCalc.slices = premResult.slices;

            var taxCalcs = [premCalc];

            var result = {};
            result.resultHeading = RESULT_HEADING_FROM_MAR_2016;
            result.totalTax = premResult.taxDue; 
            result.taxCalcs = taxCalcs;

            // calculation for previous rate. Uses rates before 201603 but needs headings/hints adding
            var prevRatesArray = calcFreeNonResPrem_201203_201603(premium);
            var prevRatesResult = prevRatesArray[0];
            prevRatesResult.resultHeading = RESULT_HEADING_BEFORE_MAR_2016;
            prevRatesResult.resultHint = RESULT_HINT_EXCHANGE_BEFORE_MAR_2016;

            return [result, prevRatesResult];
        };

        var calcLeaseResPremAndRent_201203_201412 = function(premium, npv){

            var rentSlicesArray = [
                    { from: 0,       to : 125000,   rate : 0,  taxDue : -1},
                    { from: 125000,  to : -1    ,   rate : 1,  taxDue : -1}
            ];

            var rentResult = calculateTaxDueSlice(npv, rentSlicesArray);
            var rentCalc = {taxType : "rent", calcType : 'slice', detailHeading : '', bandHeading : '', detailFooter : '', taxDue : 0, slices : []};
            rentCalc.detailHeading = DETAIL_HEADING_SDLT_ON_RENT;
            rentCalc.bandHeading = DETAIL_COL_HEADER_RENT;
            rentCalc.detailFooter = DETAIL_FOOTER_RENT;
            rentCalc.taxDue = rentResult.taxDue;
            rentCalc.slices = rentResult.slices;

            var premSlabsArray = [
                    { threshold : 2000000,   rate : 7},
                    { threshold : 1000000,   rate : 5},
                    { threshold : 500000,    rate : 4},
                    { threshold : 250000,    rate : 3},
                    { threshold : 125000,    rate : 1},
                    { threshold : -1,        rate : 0}
            ];

            var premResult = calculateTaxDueSlab(premium, premSlabsArray);
            var premCalc = {taxType : "premium", calcType : 'slab', taxDue : 0, rate : 0};
            premCalc.taxDue = premResult.taxDue;
            premCalc.rate = premResult.rate;

            var taxCalcs = [rentCalc, premCalc];

            var result = {};
            result.totalTax = rentCalc.taxDue + premCalc.taxDue; 
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
            var rentCalc = {taxType : "rent", calcType : 'slice', detailHeading : '', bandHeading : '', detailFooter : '', taxDue : 0, slices : []};
            rentCalc.detailHeading = DETAIL_HEADING_SDLT_ON_RENT;
            rentCalc.bandHeading = DETAIL_COL_HEADER_RENT;
            rentCalc.detailFooter = DETAIL_FOOTER_RENT;

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
            var premCalc = {taxType : "premium", calcType : 'slice', detailHeading : '', bandHeading : '', detailFooter : '', taxDue : 0, slices : []};
            premCalc.detailHeading = DETAIL_HEADING_SDLT_ON_PREM;
            premCalc.bandHeading = DETAIL_COL_HEADER_PREM;
            premCalc.detailFooter = DETAIL_FOOTER_PREM;
            premCalc.taxDue = premResult.taxDue;
            premCalc.slices = premResult.slices;

            var taxCalcs = [rentCalc, premCalc];

            var result = {};
            result.totalTax = rentCalc.taxDue + premCalc.taxDue; 
            result.npv = npv;
            result.taxCalcs = taxCalcs;

            return [result];
        };

        // from 201604 Additional properties are charged at higher rate.
        // There is a transitional period for exchanged contracts < 16/03/2016 so we also calculate using the old rates
        var calcLeaseResPremAndRentAddProp_201604_Undef = function(premium, npv, isIndividual){

            // calculation for previous rates. Uses rates from 201412 onwards but needs headings/hints adding
            var prevRatesArray = calcLeaseResPremAndRent_201412_Undef(premium, npv);
            var prevRatesResult = prevRatesArray[0];
            prevRatesResult.resultHeading = RESULT_HEADING_BEFORE_APR_2016;
            prevRatesResult.resultHint =  RESULT_HINT_EXCHANGE_BEFORE_NOV_2015;
            prevRatesResult.taxCalcs[0].detailHeading = DETAIL_HEADING_SDLT_ON_RENT_BEFORE_APR_2016;
            prevRatesResult.taxCalcs[1].detailHeading = DETAIL_HEADING_SDLT_ON_PREM_BEFORE_APR_2016;

            var rentSlicesArray = [
                    { from: 0,       to : 125000,   rate : 0,  taxDue : -1},
                    { from: 125000,  to : -1    ,   rate : 1,  taxDue : -1}
            ];

            var rentResult = calculateTaxDueSlice(npv, rentSlicesArray);
            var rentCalc = {taxType : 'rent', calcType : 'slice', detailHeading : '', bandHeading : '', detailFooter : '', taxDue : 0, slices : []};
            rentCalc.detailHeading = DETAIL_HEADING_SDLT_ON_RENT_FROM_APR_2016;
            rentCalc.bandHeading = DETAIL_COL_HEADER_RENT;
            rentCalc.detailFooter = DETAIL_FOOTER_RENT;
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

            var premCalc = {taxType : 'premium', calcType : 'slice', detailHeading : '', bandHeading : '', detailFooter : '', taxDue : 0, slices : []};
            premCalc.detailHeading = DETAIL_HEADING_SDLT_ON_PREM_FROM_APR_2016;
            premCalc.bandHeading = DETAIL_COL_HEADER_PREM;
            premCalc.detailFooter = DETAIL_FOOTER_PREM;
            premCalc.taxDue = calcResult.taxDue;
            premCalc.slices = calcResult.slices;

            var taxCalcs = [rentCalc, premCalc];

            var result = {};
            result.resultHeading = RESULT_HEADING_FROM_APR_2016;
            if (isIndividual && (premCalc.taxDue > prevRatesResult.taxCalcs[1].taxDue)) {
                result.resultHint = RESULT_HINT_ADDNL_PROP_REFUND_ + (premCalc.taxDue - prevRatesResult.taxCalcs[1].taxDue).toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",") +".";
            }
            result.totalTax = rentCalc.taxDue + premCalc.taxDue;
            result.npv = npv;
            result.taxCalcs = taxCalcs;

            return [result, prevRatesResult];
        };

        var calcLeaseNonResPremAndRent_201203_201603 = function(premium, npv, zeroRate){

            var rentSlicesArray = [
                    { from: 0,       to : 150000,   rate : 0,  taxDue : -1},
                    { from: 150000,  to : -1    ,   rate : 1,  taxDue : -1}
            ];

            var rentResult = calculateTaxDueSlice(npv, rentSlicesArray);
            var rentCalc = {taxType : "rent", calcType : 'slice', detailHeading : '', bandHeading : '', detailFooter : '', taxDue : 0, slices : []};
            rentCalc.detailHeading = DETAIL_HEADING_SDLT_ON_RENT;
            rentCalc.bandHeading = DETAIL_COL_HEADER_RENT;
            rentCalc.detailFooter = DETAIL_FOOTER_RENT;
            rentCalc.taxDue = rentResult.taxDue;
            rentCalc.slices = rentResult.slices;

            var premSlabsArray = [
                    { threshold : 500000, rate : 4},
                    { threshold : 250000, rate : 3},
                    { threshold : -1,     rate : 1}
            ];

            var premCalc = {taxType : "premium", calcType : 'slab', taxDue : 0, rate : 0};
            if ( !zeroRate ) {
                var premResult = calculateTaxDueSlab(premium, premSlabsArray);
                premCalc.taxDue = premResult.taxDue;
                premCalc.rate = premResult.rate;
            }
            var taxCalcs = [rentCalc, premCalc];

            var result = {};
            result.totalTax = rentCalc.taxDue + premCalc.taxDue; 
            result.npv = npv;
            result.taxCalcs = taxCalcs;

            return [result];
        };

        var calcLeaseNonResPremAndRent_201603_Undef = function(premium, npv, zeroRate, prevCalcReqd){

            var rentSlicesArray = [
                    { from: 0,       to : 150000 ,   rate : 0,  taxDue : -1},
                    { from: 150000,  to : 5000000,   rate : 1,  taxDue : -1},
                    { from: 5000000, to : -1,        rate : 2,  taxDue : -1}
            ];

            var rentResult = calculateTaxDueSlice(npv, rentSlicesArray);
            var rentCalc = {taxType : "rent", calcType : 'slice', detailHeading : '', bandHeading : '', detailFooter : '', taxDue : 0, slices : []};
            rentCalc.detailHeading = DETAIL_HEADING_SDLT_ON_RENT_FROM_MAR_2016;
            rentCalc.bandHeading = DETAIL_COL_HEADER_RENT;
            rentCalc.detailFooter = DETAIL_FOOTER_RENT;
            rentCalc.taxDue = rentResult.taxDue;
            rentCalc.slices = rentResult.slices;

            var premSlicesArray = [
                    { from: 0,       to : 150000,   rate : 0,  taxDue : -1},
                    { from: 150000,  to : 250000,   rate : 2,  taxDue : -1},
                    { from: 250000,  to : -1,       rate : 5,  taxDue : -1}
            ];

            var premResult = calculateTaxDueSlice(premium, premSlicesArray);
            var premCalc = {taxType : "premium", calcType : 'slice', detailHeading : '', bandHeading : '', detailFooter : '', taxDue : 0, slices : []};
            premCalc.detailHeading = DETAIL_HEADING_SDLT_ON_PREM_FROM_MAR_2016;
            premCalc.bandHeading = DETAIL_COL_HEADER_PREM;
            premCalc.detailFooter = DETAIL_FOOTER_PREM;
            premCalc.taxDue = premResult.taxDue;
            premCalc.slices = premResult.slices;

            var taxCalcs = [rentCalc, premCalc];

            var result = {};
            result.resultHeading = RESULT_HEADING_FROM_MAR_2016;
            result.totalTax = rentCalc.taxDue + premCalc.taxDue; 
            result.npv = npv;
            result.taxCalcs = taxCalcs;

            if (prevCalcReqd) {
                // calculation for previous rates. Uses rates from 201412 onwards but needs headings/hints adding
                var prevRatesArray = calcLeaseNonResPremAndRent_201203_201603(premium, npv, zeroRate);
                var prevRatesResult = prevRatesArray[0];
                prevRatesResult.resultHeading = RESULT_HEADING_BEFORE_MAR_2016;
                prevRatesResult.resultHint =  RESULT_HINT_EXCHANGE_BEFORE_MAR_2016;
                prevRatesResult.taxCalcs[0].detailHeading = DETAIL_HEADING_SDLT_ON_RENT_BEFORE_MAR_2016;

                return [result, prevRatesResult];
            }
            return [result];
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
            calculateNPV : calculateNPV,
            calcFreeResPrem_201203_201412 : calcFreeResPrem_201203_201412,
            calcFreeResPrem_201412_Undef : calcFreeResPrem_201412_Undef,
            calcFreeResPremAddProp_201604_Undef : calcFreeResPremAddProp_201604_Undef,
            calcFreeNonResPrem_201203_201603 : calcFreeNonResPrem_201203_201603,
            calcFreeNonResPrem_201603_Undef : calcFreeNonResPrem_201603_Undef,
            calcLeaseResPremAndRent_201203_201412 : calcLeaseResPremAndRent_201203_201412,
            calcLeaseResPremAndRent_201412_Undef : calcLeaseResPremAndRent_201412_Undef,
            calcLeaseResPremAndRentAddProp_201604_Undef : calcLeaseResPremAndRentAddProp_201604_Undef,
            calcLeaseNonResPremAndRent_201203_201603 : calcLeaseNonResPremAndRent_201203_201603,
            calcLeaseNonResPremAndRent_201603_Undef: calcLeaseNonResPremAndRent_201603_Undef

        };
    });
})();
