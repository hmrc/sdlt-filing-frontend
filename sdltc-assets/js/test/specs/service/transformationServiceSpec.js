(function() {
    'use strict';

    describe('Transformation Service', function () {

        var service;

        beforeEach(angular.mock.module("sdltc.services"));

        beforeEach(inject(function (_transformationService_) {
            service = _transformationService_;
        }));

        // **************** Household Section ****************
        it('transform should return household.partner = yes when data.havePartner is yes', function() {
            var input = {"havePartner" : "yes"};
            var output = service.transform(input);

            expect(output.household.partner).toEqual("yes");
        });

        it('transform should return household.hasChildren = yes when data.haveChildren is yes', function() {
            var input = {"haveChildren" : "yes"};
            var output = service.transform(input);

            expect(output.household.hasChildren).toEqual("yes");
        });

        it('transform should return household.childCareNum = 1 when data.numChildcare = 1', function() {
            var input = {"numChildcare" : 1};
            var output = service.transform(input);

            expect(output.household.childCareNum).toEqual(1);
        });

        it('transform should return household.childCareAmnt = 123.45 when data.costChildcare = 123.45', function() {
            var input = {"costChildcare" : 123.45};
            var output = service.transform(input);

            expect(output.household.childCareAmnt).toEqual(123.45);
        });

        it('transform should return household.totalChildren = 3 when data.numChildren = 3', function() {
            var input = {"numChildren" : 3};
            var output = service.transform(input);

            expect(output.household.totalChildren).toEqual(3);
        });

        it('transform should return household.disabledChildren = 2 when data.numDisabledChildren = 2', function() {
            var input = {"numDisabledChildren" : 2};
            var output = service.transform(input);

            expect(output.household.disabledChildren).toEqual(2);
        });

        it('transform should return household.severelyDisabledChildren = 1 when data.numSeverelyDisabled = 1', function() {
            var input = {"numSeverelyDisabled" : 1};
            var output = service.transform(input);

            expect(output.household.severelyDisabledChildren).toEqual(1);
        });

        // **************** You : Personal Section ****************
        it('transform should return you.personal.age = 33 when data.yourAge is 33', function() {
            var input = {"yourAge" : 33};
            var output = service.transform(input);

            expect(output.you.personal.age).toEqual(33);
        });

        it('transform should return you.personal.hospitalPrisonEtc = partner when data.hospitalPrisonEtc is partner', function() {
            var input = {"hospitalPrisonEtc" : "partner"};
            var output = service.transform(input);

            expect(output.you.personal.hospitalPrisonEtc).toEqual("partner");
        });

        it('transform should return you.personal.disabled = yes when data.youDisabled is yes', function() {
            var input = {"youDisabled" : "yes"};
            var output = service.transform(input);

            expect(output.you.personal.disabled).toEqual("yes");
        });

        it('transform should return you.personal.severelyDisabled = yes when data.youSeverelyDisabled is yes', function() {
            var input = {"youSeverelyDisabled" : "yes"};
            var output = service.transform(input);

            expect(output.you.personal.severelyDisabled).toEqual("yes");
        });

        // **************** You : Work and Benefits Section ****************
        it('transform should return you.workAndBenefits.status = working when data.youEmployment is working', function() {
            var input = {"youEmployment" : "working"};
            var output = service.transform(input);

            expect(output.you.workAndBenefits.status).toEqual("working");
        });

        it('transform should return you.workAndBenefits.status = selfEmployed when data.youEmployment is selfEmployed', function() {
            var input = {"youEmployment" : "selfEmployed"};
            var output = service.transform(input);

            expect(output.you.workAndBenefits.status).toEqual("selfEmployed");
        });

        it('transform should return you.workAndBenefits.status = notWorking when data.youEmployment is notWorking', function() {
            var input = {"youEmployment" : "notWorking"};
            var output = service.transform(input);

            expect(output.you.workAndBenefits.status).toEqual("notWorking");
        });

        it('transform should return you.workAndBenefits.benefits = yes when data.youBenefit is yes', function() {
            var input = {"youBenefit" : "yes"};
            var output = service.transform(input);

            expect(output.you.workAndBenefits.benefits).toEqual("yes");
        });

        it('transform should return you.workAndBenefits.hours = 37 when data.yourHours is 37', function() {
            var input = {"yourHours" : 37};
            var output = service.transform(input);

            expect(output.you.workAndBenefits.hours).toEqual(37);
        });

        // **************** You : income Section ****************
        it('transform should return you.income.earnings = 1000 when data.income is 1000', function() {
            var input = {"income" : 1000};
            var output = service.transform(input);

            expect(output.you.income.earnings).toEqual(1000);
        });

        it('transform should return you.income.smp = 2000 when data.smpSppSap is 2000', function() {
            var input = {"smpSppSap" : 2000};
            var output = service.transform(input);

            expect(output.you.income.smp).toEqual(2000);
        });

        it('transform should return you.income.selfEmployedProfit = 3000 when data.profit is 3000', function() {
            var input = {"profit" : 3000};
            var output = service.transform(input);

            expect(output.you.income.selfEmployedProfit).toEqual(3000);
        });

        it('transform should return you.income.otherTaxableBenefits = 4000 when data.otherTaxableBenefits is 4000', function() {
            var input = {"otherTaxableBenefits" : 4000};
            var output = service.transform(input);

            expect(output.you.income.otherTaxableBenefits).toEqual(4000);
        });

        it('transform should return you.income.otherIncome = 5000 when data.otherIncome is 5000', function() {
            var input = {"otherIncome" : 5000};
            var output = service.transform(input);

            expect(output.you.income.otherIncome).toEqual(5000);
        });

        it('transform should return you.income.deductions = 5000 when data.deductions is 5000', function() {
            var input = {"deductions" : 5000};
            var output = service.transform(input);

            expect(output.you.income.deductions).toEqual(5000);
        });

        // **************** Partner : Personal Section ****************
        it('transform should return partner.personal.age = 34 when data.partnerAge is 34', function() {
            var input = {"partnerAge" : 34};
            var output = service.transform(input);

            expect(output.partner.personal.age).toEqual(34);
        });

        it('transform should return partner.personal.disabled = yes when data.partnerDisabled is yes', function() {
            var input = {"partnerDisabled" : "yes"};
            var output = service.transform(input);

            expect(output.partner.personal.disabled).toEqual("yes");
        });

        it('transform should return partner.personal.severelyDisabled = yes when data.partnerSeverelyDisabled is yes', function() {
            var input = {"partnerSeverelyDisabled" : "yes"};
            var output = service.transform(input);

            expect(output.partner.personal.severelyDisabled).toEqual("yes");
        });

        // **************** Partner : Work and Benefits Section ****************
        it('transform should return partner.workAndBenefits.status = working when data.partnerEmployment is working', function() {
            var input = {"partnerEmployment" : "working"};
            var output = service.transform(input);

            expect(output.partner.workAndBenefits.status).toEqual("working");
        });

        it('transform should return partner.workAndBenefits.status = selfEmployed when data.partnerEmployment is selfEmployed', function() {
            var input = {"partnerEmployment" : "selfEmployed"};
            var output = service.transform(input);

            expect(output.partner.workAndBenefits.status).toEqual("selfEmployed");
        });

        it('transform should return partner.workAndBenefits.status = notWorking when data.partnerEmployment is notWorking', function() {
            var input = {"partnerEmployment" : "notWorking"};
            var output = service.transform(input);

            expect(output.partner.workAndBenefits.status).toEqual("notWorking");
        });

        it('transform should return partner.workAndBenefits.benefits = yes when data.partnerBenefit is yes', function() {
            var input = {"partnerBenefit" : "yes"};
            var output = service.transform(input);

            expect(output.partner.workAndBenefits.benefits).toEqual("yes");
        });

        it('transform should return partner.workAndBenefits.hours = 38 when data.partnerHours is 38', function() {
            var input = {"partnerHours" : 38};
            var output = service.transform(input);

            expect(output.partner.workAndBenefits.hours).toEqual(38);
        });

        // **************** partner : income Section ****************
        it('transform should return partner.income.earnings = 1001 when data.partnerIncome is 1001', function() {
            var input = {"partnerIncome" : 1001};
            var output = service.transform(input);

            expect(output.partner.income.earnings).toEqual(1001);
        });

        it('transform should return partner.income.smp = 2001 when data.partnerSmpSppSap is 2001', function() {
            var input = {"partnerSmpSppSap" : 2001};
            var output = service.transform(input);

            expect(output.partner.income.smp).toEqual(2001);
        });

        it('transform should return partner.income.selfEmployedProfit = 3000 when data.partnerProfit is 3001', function() {
            var input = {"partnerProfit" : 3001};
            var output = service.transform(input);

            expect(output.partner.income.selfEmployedProfit).toEqual(3001);
        });

        it('transform should return partner.income.otherTaxableBenefits = 4001 when data.partnerOtherTaxableBenefits is 4001', function() {
            var input = {"partnerOtherTaxableBenefits" : 4001};
            var output = service.transform(input);

            expect(output.partner.income.otherTaxableBenefits).toEqual(4001);
        });

        it('transform should return partner.income.otherIncome = 5000 when data.partnerOtherIncome is 5000', function() {
            var input = {"partnerOtherIncome" : 5000};
            var output = service.transform(input);

            expect(output.partner.income.otherIncome).toEqual(5000);
        });

        it('transform should return partner.income.deductions = 5000 when data.partnerDeductions is 5000', function() {
            var input = {"partnerDeductions" : 5000};
            var output = service.transform(input);

            expect(output.partner.income.deductions).toEqual(5000);
        });

    });

}());
