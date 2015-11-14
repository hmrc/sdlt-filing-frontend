(function() {
    "use strict";

	var app = require("./module");

	app.service('calculationService', function(){

	     var calculateTaxCredits = function(inputJSON){

                    var householdIncome = getTCIncome(inputJSON);
                    var youIncome = getYouIncome(inputJSON);

        			var benefits = getBenefits(inputJSON);

        			//Getting entitlement

        			var wtcBasic  = getWtcBasic(inputJSON);
        			var	wtc2ALP = getWtc2ALP(wtcBasic, inputJSON);
        			var wtc30Hour = getWtc30Hour(wtcBasic, inputJSON);
        			var wtcEDA = getWtcEDA(wtcBasic, inputJSON);
        			var wtcESDA = getWtcESDA(wtcBasic, inputJSON);
        			var wtcChildCare = getWtcChildCare(wtcBasic, inputJSON);
        			var wtcChildCareNum = getWtcChildCareNum(wtcBasic, inputJSON);
        			var wtcChildCareAmnt = getWtcChildCareAmnt(wtcBasic, inputJSON);

        			var ctcChildren = getCtcChildren(inputJSON);
        			var ctcIDA = getCtcIDA(inputJSON);
        			var ctcISDA = getCtcISDA(inputJSON);
        			var ctcFamily = getCtcFamily(inputJSON);

        			//Calculating amounts

        			var fullWTC = calcWTC(wtcBasic, wtc2ALP, wtc30Hour, wtcEDA, wtcESDA);
        			var fullWTCCC = calcWTCCC(wtcBasic, wtcChildCare, wtcChildCareNum, wtcChildCareAmnt);
        			var fullCTC = calcCTC(ctcChildren, ctcIDA, ctcISDA);
        			var fullCTCF = calcCTCF(ctcFamily);

        			var taperedWTC = taperWTC(householdIncome, fullWTC, wtcTaperStart, taperPercent);
        			var taperedWTCCC = taperWTCCC(householdIncome, fullWTCCC, fullWTC, wtcTaperStart, taperPercent);
        			var taperedCTC = taperCTC(householdIncome, fullCTC, fullWTC, fullWTCCC, wtcTaperStart, ctcTaperStart, taperPercent);
        			var taperedCTCF = taperCTCF(householdIncome, fullCTCF, fullCTC, fullWTC, fullWTCCC, wtcTaperStart, ctcTaperStart, taperPercent);


                    var actualWTC, actualCTC;

                    if(benefits){
                        actualWTC = fullWTC + fullWTCCC;
                        actualCTC = fullCTC + fullCTCF;
                    }
                    else{
                        actualWTC = taperedWTC + taperedWTCCC;
                        actualCTC = taperedCTC + taperedCTCF;
                    }

                    if(actualWTC+actualCTC<26){
                        actualWTC=0;
                        actualCTC=0;
                    }

                    var finalWTC = roundUp2(actualWTC/13).toFixed(2);
                    var finalCTC = roundUp2(actualCTC/13).toFixed(2);

                    var youTaxDecrease = getTaxDecrease(youIncome).toFixed(2);
                    var incomeBracket = getIncomeBracket(youIncome);

        			var outputObj = {
        				wtc : finalWTC,
        				ctc : finalCTC,
                        householdIncome : householdIncome,
                        youIncome : youIncome,
                        youTaxDecrease : youTaxDecrease,
                        incomeBracket : incomeBracket
        			};

        			return outputObj;
        		};

        var getIncomeBracket = function(income){
            var incomeFloat = parseFloat(income);

            if(incomeFloat<10600) return 'under';
            if(incomeFloat<10800) return 'in';
            return 'over';
        };

        var getTCIncome = function (inputJSONobj) {
            var youIncome = getYouIncome(inputJSONobj);
            var youDeductions = getYouDeductions(inputJSONobj);

            var partnerIncome = getPartnerIncome(inputJSONobj);
            var partnerDeductions = getPartnerDeductions(inputJSONobj);

            var householdDeductions = getHouseholdDeductions(inputJSONobj);

            var householdIncome = youIncome + partnerIncome - youDeductions - partnerDeductions - householdDeductions;

            return householdIncome;
        };

        var getYouIncome = function (inputJSONobj) {
            var youEarnings = inputJSONobj.you.income.earnings;
            var youProfit = inputJSONobj.you.income.selfEmployedProfit;
            var youOtherTaxableBenefits = inputJSONobj.you.income.otherTaxableBenefits;
            var youOtherIncome = inputJSONobj.you.income.otherIncome;

            var youIncome = youEarnings + youProfit + youOtherTaxableBenefits + youOtherIncome;

            return youIncome;
        };

        var getYouDeductions = function(inputJSONobj){
            var youSMP = inputJSONobj.you.income.smp;
            var youDeductions = inputJSONobj.you.income.deductions;
    
            var youTotalDeductions = youSMP + youDeductions;
            return youTotalDeductions;
        };

        var getPartnerIncome = function (inputJSONobj) {
            var partnerEarnings = inputJSONobj.partner.income.earnings;
            var partnerProfit = inputJSONobj.partner.income.selfEmployedProfit;
            var partnerOtherTaxableBenefits = inputJSONobj.partner.income.otherTaxableBenefits;
            var partnerOtherIncome = inputJSONobj.partner.income.otherIncome;

            var partnerIncome = partnerEarnings + partnerProfit + partnerOtherTaxableBenefits + partnerOtherIncome;

            return partnerIncome;
        };

        var getPartnerDeductions = function(inputJSONobj){
            var partnerSMP = inputJSONobj.partner.income.smp;
            var partnerDeductions = inputJSONobj.partner.income.deductions;
    
            var partnerTotalDeductions = partnerSMP + partnerDeductions;
            return partnerTotalDeductions;
        };

        var getHouseholdDeductions = function(inputJSONobj){
            var youOtherIncome = inputJSONobj.you.income.otherIncome;
            var partnerOtherIncome = inputJSONobj.partner.income.otherIncome;

            var householdDeduction = Math.min(300, youOtherIncome+partnerOtherIncome);

            return householdDeduction;
        };

        var getTaxDecrease = function (income){
            var personalAllowance2015 = 10600;
            var personalAllowance2016 = 11000;

            var currentTax = Math.max(0,income-personalAllowance2015)/5;
            var newTax = Math.max(0,income-personalAllowance2016)/5;

            var taxDecrease = currentTax - newTax;

            return taxDecrease;
        };

        var getBenefits = function getBenefits(inputJSONobj) {
          if (inputJSONobj.you.workAndBenefits.benefits === "yes") {
            return true;
          }
          if (inputJSONobj.partner.workAndBenefits.benefits === "yes") {
            return true;
          }
          return false;
        };

        var getWtcBasic = function getWtcBasic(inputJSONobj) {
            var youWorking = false;
            var partnerWorking = false;

            // See http://www.hmrc.gov.uk/manuals/tctmanual/tctm02410.htm
            // First Condition - must be working or self employed
            if (inputJSONobj.you.workAndBenefits.status === 'working') {
                youWorking = true;
            }
            if (inputJSONobj.you.workAndBenefits.status === 'selfEmployed') {
                youWorking = true;
            }
            if (inputJSONobj.partner.workAndBenefits.status === 'working') {
                partnerWorking = true;
            }
            if (inputJSONobj.partner.workAndBenefits.status === 'selfEmployed') {
                partnerWorking = true;
            }
            if (!youWorking && !partnerWorking) {
                return false;
            }

            // Second Condition
            // extract the required data from Json
            var hasPartner = inputJSONobj.household.partner;
            var age = inputJSONobj.you.personal.age;
            var partnerAge = inputJSONobj.partner.personal.age;
            var youHours = inputJSONobj.you.workAndBenefits.hours;
            var partnerHours = inputJSONobj.partner.workAndBenefits.hours;
            var children = inputJSONobj.household.totalChildren;
            var youDisabled = inputJSONobj.you.personal.disabled;
            var youSevDisabled = inputJSONobj.you.personal.severelyDisabled;
            var isDisabled = false;

            if ((youDisabled === 'yes') || (youSevDisabled === 'yes')) {
                isDisabled = true;
            }

            var partnerDisabled = inputJSONobj.partner.personal.disabled;
            var partnerSevDisabled = inputJSONobj.partner.personal.severelyDisabled;
            var partnerIsDisabled = false;

            if ((partnerDisabled === 'yes') || (partnerSevDisabled === 'yes')) {
                partnerIsDisabled = true;
            }

            var hospitalPrisonEtc = inputJSONobj.you.personal.hospitalPrisonEtc;

            // First Variation: In the case of a single claim
            if (hasPartner === 'no') {
                // a: age 16, work 16 hours AND ...
                if ((age >= 16) && (youHours >= 16)) {

                    // ... there is a child or qualifying young person..., OR
                    if (children > 0) {
                        return true;
                    }
                    // ... satisfies conditions ... disability
                    if (isDisabled) {
                        return true;
                    }
                }

                // b : 50+ element ceased 6/4/2012

                // c: age 25, work 30 hours
                if ((age >= 25) && (youHours >= 30)) {
                    return true;
                }

                // d: aged 60, work 16 hours
                if ((age >= 60) && (youHours >= 16)) {
                    return true;
                }
            }
            if (hasPartner === 'yes'){
                // Second Variation: Joint claim, no children
                if (children === 0) {
                    // 1. age 16, works 16 hours, disabled
                    if (((age >= 16) && (youHours >= 16) && isDisabled) || ((partnerAge >= 16) && (partnerHours >= 16) && partnerIsDisabled)) {
                        return true;
                    }
                    // 2. age 25, works 30 hours
                    if (((age >= 25) && (youHours >= 30)) || ((partnerAge >= 25) && (partnerHours >= 30))) {
                        return true; // C2.V2.2
                    }
                }

                // Third variation
                if (children > 0) {

                    // 1. one works > 16 hours, combined hours > 24
                    if (((youHours >= 16) || (partnerHours >= 16)) && ((youHours + partnerHours) >= 24)) {

                        return true;
                    }

                    if ((age >= 16) && (youHours >= 16)) {
                        // 2. aged 16, 16 hours and disabled
                        if (isDisabled) {
                            return true;
                        }
                        // 3. aged 16, 16 hours and partner disabled or hospital/prison
                        if ((partnerIsDisabled) || (hospitalPrisonEtc === 'partner')) {
                            return true;
                        }
                    }

                    if ((partnerAge >= 16) && (partnerHours >= 16)) {
                        // 2. aged 16, 16 hours and disabled
                        if (partnerIsDisabled) {
                            return true;
                        }
                        // 3. aged 16, 16 hours and partner disabled or hospital/prison
                        if ((isDisabled) || (hospitalPrisonEtc === 'you')) {
                            return true;
                        }
                    }
                }
                // 3. aged 60, work 16 hours (C2.V2.3/C2.V3.d)
                    if (((age >= 60) && (youHours >= 16)) || ((partnerAge >= 60) && (partnerHours >= 16))) {
                        return true;
                }
            }
            return false;
        };

        var getWtc2ALP = function getWtc2ALP(wtcBasic, inputJSONobj) {
          if (!wtcBasic) {
            return false;
          }
          if (inputJSONobj.household.partner === 'yes') {
            return true;
          }
          if (inputJSONobj.household.totalChildren > 0) {
            return true;
          }
          return false;
        };

        var getWtc30Hour = function getWtc30Hour(wtcBasic, inputJSONobj) {
          var youHours = inputJSONobj.you.workAndBenefits.hours;
          var youAge = inputJSONobj.you.personal.age;

          var partnerHours = inputJSONobj.partner.workAndBenefits.hours;
          var partnerAge = inputJSONobj.partner.personal.age;

          var totalChildren = inputJSONobj.household.totalChildren;

          if (!wtcBasic) {
              return false;
          }
          if (youHours >= 30)  {
              return true;
          }
          if (partnerHours >= 30)  {
              return true;
          }
          if (totalChildren > 0){
              var combinedHours = youHours + partnerHours;
              if(combinedHours >= 30 && ((youHours >= 16 && youAge >= 16) || (partnerHours >= 16 && partnerAge >= 16))){
                  return true;
              }
          }
          return false;
        };

        var getWtcEDA = function getWtcEDA(wtcBasic, inputJSONobj) {
          if (!wtcBasic) {
            return 0;
          }

          var edaCount = 0;

          if (inputJSONobj.you.personal.disabled === 'yes' && inputJSONobj.you.workAndBenefits.hours >= 16) {
            edaCount = edaCount + 1;
          }

          if (inputJSONobj.partner.personal.disabled === 'yes' && inputJSONobj.partner.workAndBenefits.hours >= 16) {
            edaCount = edaCount + 1;
          }

          return edaCount;
        };

        var getWtcESDA = function getWtcESDA(wtcBasic, inputJSONobj) {
          if (!wtcBasic) {
            return 0;
          }

          var esdaCount = 0;

          if (inputJSONobj.you.personal.severelyDisabled === 'yes') {
            esdaCount = esdaCount + 1;
          }

          if (inputJSONobj.partner.personal.severelyDisabled === 'yes') {
            esdaCount = esdaCount + 1;
          }

          return esdaCount;
        };
                  
        var getWtcChildCare = function getWtcChildCare(wtcBasic, inputJSONobj){
            var youWorking = (function() {
                if(inputJSONobj.you.workAndBenefits.hours>=16) return true;
            })();

            var youIncapacitated = (function() {
                if(inputJSONobj.you.personal.disabled === 'yes') return true;
                if(inputJSONobj.you.personal.severelyDisabled === 'yes') return true;
                if(inputJSONobj.you.personal.hospitalPrisonEtc === 'you') return true;
            })();

            var hasPartner = (function() {
                if(inputJSONobj.household.partner === 'yes') return true;
            })();

            var partnerWorking = (function() {
                if(inputJSONobj.partner.workAndBenefits.hours>=16) return true;
            })();

            var partnerIncapacitated = (function() {
                if(inputJSONobj.partner.personal.disabled === 'yes') return true;
                if(inputJSONobj.partner.personal.severelyDisabled === 'yes') return true;
                if(inputJSONobj.you.personal.hospitalPrisonEtc === 'partner') return true;
            })();

            var validChildcare = (function() {
                if(!wtcBasic) return false;
                if(youWorking){
                    if(!hasPartner) return true;
                    if(partnerWorking) return true;
                    if(partnerIncapacitated) return true;
                }
                if(youIncapacitated){
                    if(!hasPartner) return true;
                    if(partnerWorking) return true;
                    if(partnerIncapacitated) return true;
                }
                return false;
            })();

            return validChildcare;
        };

        var getWtcChildCareNum = function getWtcChildCareNum(wtcBasic, inputJSONobj) {
            if (!wtcBasic) {
                return 0;
            }

            return inputJSONobj.household.childCareNum;
        };


        var getWtcChildCareAmnt = function getWtcChildCareAmnt(wtcBasic, inputJSONobj) {
            if (!wtcBasic) {
                return 0;
            }

            if (inputJSONobj.household.childCareNum === 0) {
                return 0;
            }

            return inputJSONobj.household.childCareAmnt;
        };

        var getCtcChildren = function getCtcChildren(inputJSONobj) {
          return inputJSONobj.household.totalChildren;
        };

        var getCtcIDA = function getCtcIDA(inputJSONobj) {
          return inputJSONobj.household.disabledChildren;
        };

        var getCtcISDA = function getCtcISDA(inputJSONobj) {
          return inputJSONobj.household.severelyDisabledChildren;
        };

        var getCtcFamily = function getCtcFamily(inputJSONobj) {
          if (inputJSONobj.household.totalChildren > 0) {
            return true;
          }
          return false;
        };

        var calcWTC = function calcWTC(wtcBasic, wtc2ALP, wtc30Hour, wtcEDA, wtcESDA) {
          var dailyRate = 0;

          if (wtcBasic) {
            dailyRate = dailyRate + dr1A;
          }

          if (wtc2ALP) {
            dailyRate = dailyRate + dr2ALP;
          }

          if (wtc30Hour) {
            dailyRate = dailyRate + drXH;
          }

          if (wtcEDA > 0) {
            dailyRate = dailyRate + (wtcEDA * drEDA);
          }

          if (wtcESDA > 0) {
            dailyRate = dailyRate + (wtcESDA * drESDA);
          }

          return Math.round(dailyRate * daysYear * 100) / 100;
        };

        var calcWTCCC = function calcWTCCC(wtcBasic, wtcChildCare, wtcChildCareNum, wtcChildCareAmnt) {
            if (!wtcBasic || !wtcChildCare) {
              return 0;
            } else {
                    if (wtcChildCareNum >= 2) {
                    if (wtcChildCareAmnt > wtcCC2) {
                        return roundUp2(round2(wtcCC2 / 7, 2) * daysClaim * wtcCCpc);
                    } else {
                        return roundUp2(wtcChildCareAmnt * 52 * wtcCCpc * daysClaim / daysYear);
                    }
                } else {
                        if (wtcChildCareAmnt > wtcCC1) {
                        return roundUp2(round2(wtcCC1 / 7, 2) * daysClaim * wtcCCpc);
                    } else {
                        return roundUp2(wtcChildCareAmnt * 52 * wtcCCpc * daysClaim / daysYear);
                    }
                }
            }
        };

        var calcCTC = function calcCTC(ctcChildren, ctcIDA, ctcISDA) {
          var dailyRate = 0;

          if (ctcChildren > 0) {
            dailyRate = dailyRate + (ctcChildren * drChild);
          }
          if (ctcIDA > 0) {
            dailyRate = dailyRate + (ctcIDA * drIDA);
          }
          if (ctcISDA > 0) {
            dailyRate = dailyRate + (ctcISDA * drISDA);
          }
          return Math.round(dailyRate * daysYear * 100) / 100;
        };

        var calcCTCF = function calcCTCF(ctcFamily) {
          var dailyRate = 0;
          if (ctcFamily) {
            dailyRate = dailyRate + drFamily;
          }
          return dailyRate * daysYear;
        };

        var taperWTC = function taperWTC(income, wtcAmount, wtcTaperStart, taperPercent) {
          var taperAmount = roundDown2(Math.max((income - wtcTaperStart) * taperPercent, 0));

          return round2(Math.max(wtcAmount - taperAmount, 0));
        };

        var taperWTCCC = function taperWTCCC(income, cctcAmount, wtcAmount, wtcTaperStart, taperPercent) {
          var taperThreshold = roundUp2(wtcTaperStart + (wtcAmount) / taperPercent);
          var taperAmount = roundDown2((Math.max((income - taperThreshold) * taperPercent, 0)));

          return round2(Math.max(cctcAmount - taperAmount, 0));
        };

        var taperCTC = function taperCTC(income, ctcAmount, wtcAmount, cctcAmount, wtcTaperStart, ctcTaperStart, taperPercent) {
          var taperThreshold = roundUp2(Math.max(ctcTaperStart, wtcTaperStart + ((wtcAmount + cctcAmount) / taperPercent)));
          var taperAmount = roundDown2(Math.max((income - taperThreshold) * taperPercent, 0));

          return round2(Math.max(ctcAmount - taperAmount, 0));
        };

        var taperCTCF = function taperCTCF(income, ctcfAmount, ctcAmount, wtcAmount, cctcAmount, wtcTaperStart, ctcTaperStart, taperPercent) {
          var taperThreshold = roundUp2(Math.max(ctcTaperStart, wtcTaperStart + ((wtcAmount + cctcAmount) / taperPercent)) + (ctcAmount / taperPercent));
          var taperAmount = roundDown2(Math.max((income - taperThreshold) * taperPercent, 0));

          return round2(Math.max(ctcfAmount - taperAmount, 0));
        };

        function round2(amount) {
          return (Math.round(amount * 100) / 100);
        }

        function roundUp2(amount) {
          return (Math.ceil(amount * 100) / 100);
        }

        function roundDown2(amount) {
          return (Math.floor(amount * 100) / 100);
        }

        var daysClaim = 365;
        var daysYear = 365;

        var dr1A = 5.37;
        var dr2ALP = 5.51;
        var drXH = 2.22;
        var drEDA = 8.14;
        var drESDA = 3.5;
        var drChild = 7.62;
        var drIDA = 8.61;
        var drISDA = 3.5;
        var drFamily = 1.5;

        var wtcCC1 = 175;
        var wtcCC2 = 300;
        var wtcCCpc = 0.7;

        var wtcTaperStart = 3850;
        var ctcTaperStart = 12125;
        var taperPercent = 0.48;

        return {
            calculateTaxCredits : calculateTaxCredits,
            getTCIncome : getTCIncome,
            getYouIncome : getYouIncome,
            getPartnerIncome : getPartnerIncome,
            getYouDeductions : getYouDeductions,
            getPartnerDeductions : getPartnerDeductions,
            getHouseholdDeductions : getHouseholdDeductions,
            getBenefits : getBenefits,
            getWtcBasic : getWtcBasic,
            getWtc2ALP : getWtc2ALP,
            getWtc30Hour : getWtc30Hour,
            getWtcEDA : getWtcEDA,
            getWtcESDA : getWtcESDA,
            getWtcChildCare : getWtcChildCare,
            getWtcChildCareNum : getWtcChildCareNum,
            getWtcChildCareAmnt : getWtcChildCareAmnt,
            getCtcChildren : getCtcChildren,
            getCtcIDA : getCtcIDA,
            getCtcISDA : getCtcISDA,
            getCtcFamily : getCtcFamily,
            calcWTC : calcWTC,
            calcWTCCC : calcWTCCC,
            calcCTC : calcCTC,
            calcCTCF : calcCTCF,
            taperWTC : taperWTC,
            taperWTCCC : taperWTCCC,
            taperCTC : taperCTC,
            taperCTCF : taperCTCF,
            getTaxDecrease : getTaxDecrease,
            getIncomeBracket : getIncomeBracket
        };
	});
})();
