(function() {
    "use strict";

    var app = require("./module");

    var transformationService = function() {

        var transform = function(data) {

            var json4Calc = {
              "household": {
                "partner": "no",
                "hasChildren": "no",
                "childCareNum": 0,
                "childCareAmnt": 0.00,
                "totalChildren": 0,
                "disabledChildren": 0,
                "severelyDisabledChildren": 0
              },
              "you": {
                "personal": {
                  "age": 0,
                  "hospitalPrisonEtc": "neither",
                  "disabled": "no",
                  "severelyDisabled": "no"
                },
                "workAndBenefits": {
                  "status": "working",
                  "benefits": "no",
                  "hours": 0
                },
                "income": {
                  "earnings": 0,
                  "smp": 0,
                  "selfEmployedProfit": 0,
                  "otherTaxableBenefits": 0,
                  "otherIncome": 0,
                  "deductions": 0
                }
              },
              "partner": {
                "personal": {
                  "age": 0,
                  "disabled": "no",
                  "severelyDisabled": "no"
                },
                "workAndBenefits": {
                  "status": "working",
                  "benefits": "no",
                  "hours": 0
                },
                "income": {
                  "earnings": 0,
                  "smp": 0,
                  "selfEmployedProfit": 0,
                  "otherTaxableBenefits": 0,
                  "otherIncome": 0,
                  "deductions": 0
                }
              }
            };
                
            if (data.havePartner == 'yes') {
                json4Calc.household.partner = 'yes';
            }

            if (data.haveChildren == 'yes') {
                json4Calc.household.hasChildren = 'yes';
            }

            if (data.numChildcare > 0) {
                json4Calc.household.childCareNum = data.numChildcare;
            }

            if (data.costChildcare > 0) {
                json4Calc.household.childCareAmnt = data.costChildcare;
            }

            if (data.numChildren > 0) {
                json4Calc.household.totalChildren = data.numChildren;
            }

            if (data.numDisabledChildren > 0) {
                json4Calc.household.disabledChildren = data.numDisabledChildren;
            }

            if (data.numSeverelyDisabled > 0) {
                json4Calc.household.severelyDisabledChildren = data.numSeverelyDisabled;
            }

            if (data.yourAge > 0) {
                json4Calc.you.personal.age = data.yourAge;
            }

            if ( (data.hospitalPrisonEtc) && (data.hospitalPrisonEtc.length > 0) ) {
                json4Calc.you.personal.hospitalPrisonEtc = data.hospitalPrisonEtc;
            }

            if (data.youDisabled == 'yes') {
                json4Calc.you.personal.disabled = 'yes';
            }

            if (data.youSeverelyDisabled == 'yes') {
                json4Calc.you.personal.severelyDisabled = 'yes';
            }

            if (data.youEmployment != 'working') {
                json4Calc.you.workAndBenefits.status = data.youEmployment;
            }

            if (data.youBenefit == 'yes') {
                json4Calc.you.workAndBenefits.benefits = data.youBenefit;
            }

            if (data.yourHours > 0) {
                json4Calc.you.workAndBenefits.hours = parseInt(data.yourHours);
            }

            if (data.income > 0) {
                json4Calc.you.income.earnings = parseFloat(data.income);
            }

            if (data.smpSppSap > 0) {
                json4Calc.you.income.smp = parseFloat(data.smpSppSap);
            }

            if (data.profit > 0) {
                json4Calc.you.income.selfEmployedProfit = parseFloat(data.profit);
            }

            if (data.otherTaxableBenefits > 0) {
                json4Calc.you.income.otherTaxableBenefits = parseFloat(data.otherTaxableBenefits);
            }

            if (data.otherIncome > 0) {
                json4Calc.you.income.otherIncome = parseFloat(data.otherIncome);
            }

            if (data.deductions > 0) {
                json4Calc.you.income.deductions = parseFloat(data.deductions);
            }

            // Partner
            if (data.partnerAge > 0) {
                json4Calc.partner.personal.age = parseInt(data.partnerAge);
            }

            if (data.partnerDisabled == 'yes') {
                json4Calc.partner.personal.disabled = 'yes';
            }

            if (data.partnerSeverelyDisabled == 'yes') {
                json4Calc.partner.personal.severelyDisabled = 'yes';
            }

            if (data.partnerEmployment != 'working') {
                json4Calc.partner.workAndBenefits.status = data.partnerEmployment;
            }

            if (data.partnerBenefit == 'yes') {
                json4Calc.partner.workAndBenefits.benefits = data.partnerBenefit;
            }

            if (data.partnerHours > 0) {
                json4Calc.partner.workAndBenefits.hours = parseInt(data.partnerHours);
            }

            if (data.partnerIncome > 0) {
                json4Calc.partner.income.earnings = parseFloat(data.partnerIncome);
            }

            if (data.partnerSmpSppSap > 0) {
                json4Calc.partner.income.smp = parseFloat(data.partnerSmpSppSap);
            }

            if (data.partnerProfit > 0) {
                json4Calc.partner.income.selfEmployedProfit = parseFloat(data.partnerProfit);
            }

            if (data.partnerOtherTaxableBenefits > 0) {
                json4Calc.partner.income.otherTaxableBenefits = parseFloat(data.partnerOtherTaxableBenefits);
            }

            if (data.partnerOtherIncome > 0) {
                json4Calc.partner.income.otherIncome = parseFloat(data.partnerOtherIncome);
            }

            if (data.partnerDeductions > 0) {
                json4Calc.partner.income.deductions = parseFloat(data.partnerDeductions);
            }

           return json4Calc;
        };

        return {
            transform : transform
        };
    };

    app.service('transformationService', transformationService);

}());
