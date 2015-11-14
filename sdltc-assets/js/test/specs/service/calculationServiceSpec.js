(function() {
    'use strict';

    describe('Calculation Service', function () {

        var service, input;

        beforeEach(angular.mock.module("sdltc.services"));


        beforeEach(inject(function (_calculationService_) {
            service = _calculationService_;

            input = {
                        household: {
                            partner: 'no',
                            hasChildren: 'no',
                            childCareNum: 0,
                            childCareAmnt: 0.00,
                            totalChildren: 0,
                            disabledChildren: 0,
                            severelyDisabledChildren: 0
                        },
                        you: {
                            personal: {
                                age: 0,
                                hospitalPrisonEtc: 'neither',
                                disabled: 'no',
                                severelyDisabled: 'no'
                            },
                            workAndBenefits: {
                                status: 'working',
                                benefits: 'no',
                                other: 'no',
                                hours: 0
                            },
                            income: {
                                earnings: 0,
                                smp: 0,
                                selfEmployedProfit: 0,
                                otherTaxableBenefits: 0,
                                otherIncome: 0,
                                deductions: 0
                            }
                        },
                        partner: {
                            personal: {
                                age: 0,
                                disabled: 'no',
                                severelyDisabled: 'no'
                            },
                            workAndBenefits: {
                                status: 'working',
                                benefits: 'no',
                                other: 'no',
                                hours: 0
                            },
                            income: {
                                earnings: 0,
                                smp: 0,
                                selfEmployedProfit: 0,
                                otherTaxableBenefits: 0,
                                otherIncome: 0,
                                deductions: 0
                            }
                        }
                    };
        }));

        // ********************* getIncome *********************

        it('getYouIncome should return 3000 for a full data test', function() {
            input.you.income.earnings=1000;
            input.you.income.selfEmployedProfit=1000;
            input.you.income.otherTaxableBenefits=1000;
            input.you.income.otherIncome=1000;

            expect(service.getYouIncome(input)).toEqual(4000);
        });

        it('getYouDeductions should return 500 for an SMP of 300 and deductions of 200', function() {
            input.you.income.smp=300;
            input.you.income.deductions=200;

            expect(service.getYouDeductions(input)).toEqual(500);
        });

        it('getPartnerIncome should return 3000 for a full data test', function() {
            input.partner.income.earnings=1000;
            input.partner.income.selfEmployedProfit=1000;
            input.partner.income.otherTaxableBenefits=1000;
            input.partner.income.otherIncome=1000;

            expect(service.getPartnerIncome(input)).toEqual(4000);
        });

        it('getPartnerDeductions should return 300 for an SMP of 300 and deductions of 200', function() {
            input.partner.income.smp=300;
            input.partner.income.deductions=200;

            expect(service.getPartnerDeductions(input)).toEqual(500);
        });

        it('getHouseholdDeductions should return 299 where one other income is 299', function() {
            input.partner.income.otherIncome=299;

            expect(service.getHouseholdDeductions(input)).toEqual(299);
        });

        it('getTCIncome should return 7500 for a full data test', function() {
            input.you.income.earnings=1000;
            input.you.income.smp=100;
            input.you.income.selfEmployedProfit=1000;
            input.you.income.otherTaxableBenefits=1000;
            input.you.income.otherIncome=1000;

            input.partner.income.earnings=1000;
            input.partner.income.smp=100;
            input.partner.income.selfEmployedProfit=1000;
            input.partner.income.otherTaxableBenefits=1000;
            input.partner.income.otherIncome=1000;

            expect(service.getTCIncome(input)).toEqual(7500);
        });

        it('getTCIncome should not reduce "other income" below 0', function() {
            input.you.income.earnings=1000;
            input.you.income.smp=100;
            input.you.income.selfEmployedProfit=1000;
            input.you.income.otherTaxableBenefits=1000;
            input.you.income.otherIncome=100;

            input.partner.income.earnings=1000;
            input.partner.income.smp=100;
            input.partner.income.selfEmployedProfit=1000;
            input.partner.income.otherTaxableBenefits=1000;
            input.partner.income.otherIncome=100;

            expect(service.getTCIncome(input)).toEqual(5800);
        });


        // ********************* getTaxDecrease *****************
        it('getTaxDecrease should return 0 if income is 10600', function() {
            expect(service.getTaxDecrease(10600)).toEqual(0);
        });

        it('getTaxDecrease should return 20 if income is 10800', function() {
            expect(service.getTaxDecrease(10800)).toEqual(40);
        });
        it('getTaxDecrease should return 40 if income is 11000', function() {
            expect(service.getTaxDecrease(11000)).toEqual(80);
        });

        it('getTaxDecrease should return 40 if income is above 11000', function() {
            expect(service.getTaxDecrease(99999)).toEqual(80);
        });

        // ********************* getBenefits *********************
        it('getBenefits should return false if both benefits are no', function() {
            input.you.workAndBenefits.benefits='no';
            input.partner.workAndBenefits.benefits='no';
            expect(service.getBenefits(input)).toEqual(false);
        });

        it('getBenefits should return true if you benefits is yes', function() {
            input.you.workAndBenefits.benefits='yes';
            input.partner.workAndBenefits.benefits='no';
            expect(service.getBenefits(input)).toEqual(true);
        });

        it('getBenefits should return true if partner benefits is yes', function() {
            input.you.workAndBenefits.benefits='no';
            input.partner.workAndBenefits.benefits='yes';
            expect(service.getBenefits(input)).toEqual(true);
        });

        it('getBenefits should return true if both benefits are yes', function() {
            input.you.workAndBenefits.benefits='yes';
            input.partner.workAndBenefits.benefits='yes';
            expect(service.getBenefits(input)).toEqual(true);
        });

        // ********************* getWtcBasic *********************
        it('getWtcBasic should return false for both not working', function() {
            input.you.workAndBenefits.status='notWorking';
            input.partner.workAndBenefits.status='notWorking';
            expect(service.getWtcBasic(input)).toEqual(false);
        });

        it('getWtcBasic should return true for Working, Single, No Children, Age 25+, Hours 30+', function() {
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'notWorking';
            input.household.partner = 'no';
            input.household.totalChildren = 0;
            input.household.disabledChildren = 0;
            input.household.severelyDisabledChildren = 0;
            input.you.personal.age = 26;
            input.you.workAndBenefits.hours = 31;
            expect(service.getWtcBasic(input)).toEqual(true);
        });

        //TCTM02410 Second variation (1)
        it('getWtcBasic should return true for Working, Lone Parent - severely disabled, has 2 Children, 1 disabled and severely disabled child,childcare, claim income support, Age 25+, Hours 30+', function() {
            input.household.partner = 'no';
            input.household.hasChildren = 'yes';
            input.household.childCareNum = 1;
            input.household.childCareAmnt = 120.01;
            input.household.totalChildren = 2;
            input.household.disabledChildren = 1;
            input.household.severelyDisabledChildren = 1;
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'notWorking';
            input.you.personal.disabled = 'yes';
            input.partner.personal.disabled = 'no';
            input.you.personal.severelyDisabled = 'yes';
            input.partner.personal.severelyDisabled = 'no';
            input.you.workAndBenefits.benefits = 'yes';
            input.partner.workAndBenefits.benefits = 'no';
            input.you.personal.age = 26;
            input.you.workAndBenefits.hours = 31;
            expect(service.getWtcBasic(input)).toEqual(true);
        });

        it('getWtcBasic should return false for Working, Single, Age 15, Hours 30+', function() {
            input.household.partner = 'no';
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'notWorking';
            input.you.personal.age = 15;
            input.you.workAndBenefits.hours = 30;
            expect(service.getWtcBasic(input)).toEqual(false);
        });

        it('getWtcBasic should return false for Working, Single, Age 16, Hours 16', function() {
            input.household.partner = 'no';
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'notWorking';
            input.you.personal.age = 16;
            input.you.workAndBenefits.hours = 16;
            expect(service.getWtcBasic(input)).toEqual(false);
        });

        it('getWtcBasic should return true for Working, Single, Age 16, Hours 16', function() {
            input.household.partner = 'no';
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'notWorking';
            input.you.personal.disabled = 'yes';
            input.partner.personal.disabled = 'no';
            input.you.personal.age = 16;
            input.you.workAndBenefits.hours = 16;
            expect(service.getWtcBasic(input)).toEqual(true);
        });

        it('getWtcBasic should return false for Working, Single - disabled, Age 15, Hours 15', function() {
            input.household.partner = 'no';
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'notWorking';
            input.you.personal.disabled = 'yes';
            input.partner.personal.disabled = 'no';
            input.you.personal.age = 15;
            input.you.workAndBenefits.hours = 15;
            expect(service.getWtcBasic(input)).toEqual(false);
        });

        it('getWtcBasic should return false for Working, Single, Age 30+, Hours 15', function() {
            input.household.partner = 'no';
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'notWorking';
            input.you.personal.age = 30;
            input.you.workAndBenefits.hours = 15;
            expect(service.getWtcBasic(input)).toEqual(false);
        });

        it('getWtcBasic should return true for Working, Single disabled, Age 30+, Hours 16', function() {
            input.household.partner = 'no';
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'notWorking';
            input.you.personal.disabled = 'yes';
            input.partner.personal.disabled = 'no';
            input.you.personal.age = 30;
            input.you.workAndBenefits.hours = 16;
            expect(service.getWtcBasic(input)).toEqual(true);
        });

        it('getWtcBasic should return true for Working, Single, Age 30+, Hours 30', function() {
            input.household.partner = 'no';
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'notWorking';
            input.you.personal.disabled = 'no';
            input.partner.personal.disabled = 'no';
            input.you.personal.age = 30;
            input.you.workAndBenefits.hours = 30;
            expect(service.getWtcBasic(input)).toEqual(true);
        });

        it('getWtcBasic should return true for Working, Single, Age 61, Hours 16', function() {
            input.household.partner = 'no';
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'notWorking';
            input.you.personal.disabled = 'no';
            input.partner.personal.disabled = 'no';
            input.you.personal.age = 61;
            input.you.workAndBenefits.hours = 16;
            expect(service.getWtcBasic(input)).toEqual(true);
        });

        it('getWtcBasic should return false for Working, Single , Age 60, Hours 15', function() {
            input.household.partner = 'no';
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'notWorking';
            input.you.personal.disabled = 'no';
            input.partner.personal.disabled = 'no';
            input.you.personal.age = 60;
            input.you.workAndBenefits.hours = 15;
            expect(service.getWtcBasic(input)).toEqual(false);
        });

        it('getWtcBasic should return false for Working, Single - disabled, Age 59, Hours 15+', function() {
            input.household.partner = 'no';
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'notWorking';
            input.you.personal.disabled = 'no';
            input.partner.personal.disabled = 'no';
            input.you.personal.age = 59;
            input.you.workAndBenefits.hours = 15;
            expect(service.getWtcBasic(input)).toEqual(false);
        });

        it('getWtcBasic should return false for Working, Single, Age 25, Hours 29', function() {
            input.household.partner = 'no';
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'notWorking';
            input.you.personal.disabled = 'no';
            input.partner.personal.disabled = 'no';
            input.you.personal.age = 25;
            input.you.workAndBenefits.hours = 29;
            expect(service.getWtcBasic(input)).toEqual(false);
        });

        it('getWtcBasic should return true for Working, Lone Parent,1 child, Age 25, Hours >=16', function() {
            input.household.partner = 'no';
            input.household.totalChildren = 1;
            input.household.disabledChildren = 0;
            input.household.severelyDisabledChildren = 0;
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'notWorking';
            input.you.personal.disabled = 'no';
            input.partner.personal.disabled = 'no';
            input.you.personal.age = 25;
            input.you.workAndBenefits.hours = 16;
            expect(service.getWtcBasic(input)).toEqual(true);
        });

        it('getWtcBasic should return false for Working, Lone Parent,1 child, Age 25, Hours 15', function() {
            input.household.partner = 'no';
            input.household.totalChildren = 1;
            input.household.disabledChildren = 0;
            input.household.severelyDisabledChildren = 0;
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'notWorking';
            input.you.personal.disabled = 'no';
            input.partner.personal.disabled = 'no';
            input.you.personal.age = 25;
            input.you.workAndBenefits.hours = 15;
            expect(service.getWtcBasic(input)).toEqual(false);
        });

        //Couple
        it('getWtcBasic should return true for Working, Couple, 1 child,Age 16, Hours 24 (Couple 16:8)', function() {
            input.household.partner = 'yes';
            input.household.totalChildren = 1;
            input.household.disabledChildren = 0;
            input.household.severelyDisabledChildren = 0;
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'notWorking';
            input.you.personal.disabled = 'no';
            input.partner.personal.disabled = 'no';
            input.you.personal.age = 16;
            input.partner.personal.age = 16;
            input.you.workAndBenefits.hours = 16;
            input.partner.workAndBenefits.hours = 8;
            expect(service.getWtcBasic(input)).toEqual(true);
        });

        it('getWtcBasic should return false for Working, Couple,Age 16, Hours 24 (Couple 16:8)', function() {
            input.household.partner = 'yes';
            input.household.totalChildren = 0;
            input.household.disabledChildren = 0;
            input.household.severelyDisabledChildren = 0;
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'working';
            input.you.personal.age = 16;
            input.partner.personal.age = 16;
            input.you.workAndBenefits.hours = 16;
            input.partner.workAndBenefits.hours = 8;
            expect(service.getWtcBasic(input)).toEqual(false);
        });

        it('getWtcBasic should return false for Working, Couple,1 child,Age 20, Hours 24 (Couple 12:12)', function() {
            input.household.partner = 'yes';
            input.household.totalChildren = 1;
            input.household.disabledChildren = 0;
            input.household.severelyDisabledChildren = 0;
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'working';
            input.you.personal.age = 20;
            input.partner.personal.age = 20;
            input.you.workAndBenefits.hours = 12;
            input.partner.workAndBenefits.hours = 12;
            expect(service.getWtcBasic(input)).toEqual(false);
        });

        it('getWtcBasic should return false for Working, Couple,Age 16, Hours 23 (Couple 16:7)', function() {
            input.household.partner = 'yes';
            input.household.totalChildren = 0;
            input.household.disabledChildren = 0;
            input.household.severelyDisabledChildren = 0;
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'working';
            input.you.personal.age = 16;
            input.partner.personal.age = 16;
            input.you.workAndBenefits.hours = 16;
            input.partner.workAndBenefits.hours = 7;
            expect(service.getWtcBasic(input)).toEqual(false);
        });

        it('getWtcBasic should return true for Working, Couple, you disabled,1 child, Age 16, Hours 23 (Couple 16:7)', function() {
            input.household.partner = 'yes';
            input.household.totalChildren = 1;
            input.household.disabledChildren = 0;
            input.household.severelyDisabledChildren = 0;
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'working';
            input.you.personal.disabled = 'yes';
            input.partner.personal.disabled = 'no';
            input.you.personal.age = 16;
            input.partner.personal.age = 16;
            input.you.workAndBenefits.hours = 16;
            input.partner.workAndBenefits.hours = 7;
            expect(service.getWtcBasic(input)).toEqual(true);
        });

        it('getWtcBasic should return false for Working, Couple, you disabled,1 child, Age 16, Hours 24 (Couple 12:12)', function() {
            input.household.partner = 'yes';
            input.household.totalChildren = 1;
            input.household.disabledChildren = 0;
            input.household.severelyDisabledChildren = 0;
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'working';
            input.you.personal.disabled = 'yes';
            input.partner.personal.disabled = 'no';
            input.you.personal.age = 16;
            input.partner.personal.age = 16;
            input.you.workAndBenefits.hours = 12;
            input.partner.workAndBenefits.hours = 12;
            expect(service.getWtcBasic(input)).toEqual(false);
        });

        it('getWtcBasic should return false for Working, Couple,1 child, Age 16, Hours 23 (Couple 16:7)', function() {
            input.household.partner = 'yes';
            input.household.totalChildren = 1;
            input.household.disabledChildren = 0;
            input.household.severelyDisabledChildren = 0;
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'working';
            input.you.personal.disabled = 'no';
            input.partner.personal.disabled = 'no';
            input.you.personal.age = 16;
            input.partner.personal.age = 16;
            input.you.workAndBenefits.hours = 16;
            input.partner.workAndBenefits.hours = 7;
            expect(service.getWtcBasic(input)).toEqual(false);
        });

        it('getWtcBasic should return true for Working, Couple,1 child, Age 60, Hours 23 (Couple 16:7)', function() {
            input.household.partner = 'yes';
            input.household.totalChildren = 1;
            input.household.disabledChildren = 0;
            input.household.severelyDisabledChildren = 0;
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'working';
            input.you.personal.disabled = 'no';
            input.partner.personal.disabled = 'no';
            input.you.personal.age = 60;
            input.partner.personal.age = 60;
            input.you.workAndBenefits.hours = 16;
            input.partner.workAndBenefits.hours = 7;
            expect(service.getWtcBasic(input)).toEqual(true);
        });

        it('getWtcBasic should return false for Working, Couple,1 child, Age 59, Hours 23 (Couple 16:7)', function() {
            input.household.partner = 'yes';
            input.household.totalChildren = 1;
            input.household.disabledChildren = 0;
            input.household.severelyDisabledChildren = 0;
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'working';
            input.you.personal.disabled = 'no';
            input.partner.personal.disabled = 'no';
            input.you.personal.age = 59;
            input.partner.personal.age = 59;
            input.you.workAndBenefits.hours = 16;
            input.partner.workAndBenefits.hours = 7;
            expect(service.getWtcBasic(input)).toEqual(false);
        });

        it('getWtcBasic should return true for Working, Couple- partner disabled, claiming benefits,1 child, Age 25, Hours 23 (Couple 16:7)', function() {
            input.household.partner = 'yes';
            input.household.totalChildren = 1;
            input.household.disabledChildren = 0;
            input.household.severelyDisabledChildren = 0;
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'working';
            input.you.workAndBenefits.benefits = 'no';
            input.partner.workAndBenefits.benefits = 'yes';
            input.you.personal.disabled = 'no';
            input.partner.personal.disabled = 'yes';
            input.you.personal.age = 25;
            input.partner.personal.age = 25;
            input.you.workAndBenefits.hours = 16;
            input.partner.workAndBenefits.hours = 7;
            expect(service.getWtcBasic(input)).toEqual(true);
        });

        it('getWtcBasic should return true for Working, Couple- partner disabled,1 child, Age 25, Hours 23 (Couple 16:7)', function() {
            input.household.partner = 'yes';
            input.household.totalChildren = 1;
            input.household.disabledChildren = 0;
            input.household.severelyDisabledChildren = 0;
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'working';
            input.you.workAndBenefits.benefits = 'no';
            input.partner.workAndBenefits.benefits = 'no';
            input.you.personal.disabled = 'no';
            input.partner.personal.disabled = 'yes';
            input.you.personal.age = 25;
            input.partner.personal.age = 25;
            input.you.workAndBenefits.hours = 16;
            input.partner.workAndBenefits.hours = 7;
            expect(service.getWtcBasic(input)).toEqual(true);
        });

        it('getWtcBasic should return true for Working, Couple- partner HospitalPrison,1 child, Age 25, Hours 23 (Couple 16:7)', function() {
            input.household.partner = 'yes';
            input.household.totalChildren = 1;
            input.household.disabledChildren = 0;
            input.household.severelyDisabledChildren = 0;
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'working';
            input.you.workAndBenefits.benefits = 'no';
            input.partner.workAndBenefits.benefits = 'no';
            input.you.personal.hospitalPrisonEtc = 'partner';
            input.you.personal.disabled = 'no';
            input.partner.personal.disabled = 'yes';
            input.you.personal.age = 25;
            input.partner.personal.age = 25;
            input.you.workAndBenefits.hours = 16;
            input.partner.workAndBenefits.hours = 7;
            expect(service.getWtcBasic(input)).toEqual(true);
        });

        it('getWtcBasic should return true where partner is self employed and working 30 hours or more', function() {
            input.household.partner = 'yes';
            input.partner.workAndBenefits.status = 'selfEmployed';
            input.you.personal.age = 25;
            input.partner.personal.age = 25;
            input.partner.workAndBenefits.hours = 30;
            expect(service.getWtcBasic(input)).toEqual(true);
        });

        it('getWtcBasic should return true for partner disabled and working 16 hours or more', function() {
            input.household.partner = 'yes';
            input.household.totalChildren = 0;
            input.household.disabledChildren = 0;
            input.household.severelyDisabledChildren = 0;
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'working';
            input.you.workAndBenefits.benefits = 'no';
            input.partner.workAndBenefits.benefits = 'no';
            input.you.personal.hospitalPrisonEtc = 'neither';
            input.you.personal.disabled = 'no';
            input.partner.personal.disabled = 'yes';
            input.you.personal.age = 25;
            input.partner.personal.age = 16;
            input.you.workAndBenefits.hours = 0;
            input.partner.workAndBenefits.hours = 16;
            expect(service.getWtcBasic(input)).toEqual(true);
        });

        it('getWtcBasic should return true for partner disabled and working 16 hours or more where there ', function() {
            input.household.partner = 'yes';
            input.household.totalChildren = 1;
            input.household.disabledChildren = 0;
            input.household.severelyDisabledChildren = 0;
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'working';
            input.you.workAndBenefits.benefits = 'no';
            input.partner.workAndBenefits.benefits = 'no';
            input.you.personal.hospitalPrisonEtc = 'neither';
            input.you.personal.disabled = 'no';
            input.partner.personal.disabled = 'yes';
            input.you.personal.age = 25;
            input.partner.personal.age = 16;
            input.you.workAndBenefits.hours = 0;
            input.partner.workAndBenefits.hours = 16;
            expect(service.getWtcBasic(input)).toEqual(true);
        });

        it('getWtcBasic should return true for you disabled and partner working 16 hours or more where there is a child', function() {
            input.household.partner = 'yes';
            input.household.totalChildren = 1;
            input.household.disabledChildren = 0;
            input.household.severelyDisabledChildren = 0;
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'working';
            input.you.workAndBenefits.benefits = 'no';
            input.partner.workAndBenefits.benefits = 'no';
            input.you.personal.hospitalPrisonEtc = 'neither';
            input.you.personal.disabled = 'yes';
            input.partner.personal.disabled = 'no';
            input.you.personal.age = 25;
            input.partner.personal.age = 16;
            input.you.workAndBenefits.hours = 0;
            input.partner.workAndBenefits.hours = 16;
            expect(service.getWtcBasic(input)).toEqual(true);
        });

        it('getWtcBasic should return true for you in prison and partner working 16 hours or more where there is a child ', function() {
            input.household.partner = 'yes';
            input.household.totalChildren = 1;
            input.household.disabledChildren = 0;
            input.household.severelyDisabledChildren = 0;
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'working';
            input.you.workAndBenefits.benefits = 'no';
            input.partner.workAndBenefits.benefits = 'no';
            input.you.personal.hospitalPrisonEtc = 'you';
            input.you.personal.disabled = 'no';
            input.partner.personal.disabled = 'no';
            input.you.personal.age = 25;
            input.partner.personal.age = 16;
            input.you.workAndBenefits.hours = 0;
            input.partner.workAndBenefits.hours = 16;
            expect(service.getWtcBasic(input)).toEqual(true);
        });

        it('getWtcBasic should return true where partner is over 60 and working 16 hours or more ', function() {
            input.household.partner = 'yes';
            input.household.totalChildren = 0;
            input.household.disabledChildren = 0;
            input.household.severelyDisabledChildren = 0;
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'working';
            input.you.workAndBenefits.benefits = 'no';
            input.partner.workAndBenefits.benefits = 'no';
            input.you.personal.hospitalPrisonEtc = 'neither';
            input.you.personal.disabled = 'no';
            input.partner.personal.disabled = 'no';
            input.you.personal.age = 16;
            input.partner.personal.age = 60;
            input.you.workAndBenefits.hours = 0;
            input.partner.workAndBenefits.hours = 16;
            expect(service.getWtcBasic(input)).toEqual(true);
        });

        it('getWtcBasic should return false where has partner, child, and partner working 16 hours or more but you are not working, disabled, or in prison', function() {
            input.household.partner = 'yes';
            input.household.totalChildren = 1;
            input.household.disabledChildren = 0;
            input.household.severelyDisabledChildren = 0;
            input.you.workAndBenefits.status = 'working';
            input.partner.workAndBenefits.status = 'working';
            input.you.workAndBenefits.benefits = 'no';
            input.partner.workAndBenefits.benefits = 'no';
            input.you.personal.hospitalPrisonEtc = 'neither';
            input.you.personal.disabled = 'no';
            input.partner.personal.disabled = 'no';
            input.you.personal.age = 16;
            input.partner.personal.age = 16;
            input.you.workAndBenefits.hours = 0;
            input.partner.workAndBenefits.hours = 16;
            expect(service.getWtcBasic(input)).toEqual(false);
        });



        // ********************* getWtc2ALP *********************
        it('getWtc2ALP should return false if not eligible for wtcBasic', function() {
            expect(service.getWtc2ALP(false, input)).toEqual(false);
        });

        it('getWtc2ALP should return false if eligible for wtcBasic but no partner, no children', function() {
            expect(service.getWtc2ALP(true, input)).toEqual(false);
        });

        it('getWtc2ALP should return true if eligible for wtcBasic, has partner, no children', function() {
            input.household.partner = 'yes';
            expect(service.getWtc2ALP(true, input)).toEqual(true);
        });

        it('getWtc2ALP should return true if eligible for wtcBasic, no partner, has 1 child', function() {
            input.household.partner = 'no';
            input.household.totalChildren = 1;
            input.household.disabledChildren = 0;
            input.household.severelyDisabledChildren = 0;
            expect(service.getWtc2ALP(true, input)).toEqual(true);
        });

        it('getWtc2ALP should return true if eligible for wtcBasic, no partner, has 2 children', function() {
            input.household.partner = 'no';
            input.household.totalChildren = 2;
            input.household.disabledChildren = 0;
            input.household.severelyDisabledChildren = 0;
            expect(service.getWtc2ALP(true, input)).toEqual(true);
        });

        it('getWtc2ALP should return true if eligible for wtcBasic, has partner, has 2 children', function() {
            input.household.partner = 'yes';
            input.household.totalChildren = 2;
            input.household.disabledChildren = 0;
            input.household.severelyDisabledChildren = 0;
            expect(service.getWtc2ALP(true, input)).toEqual(true);
        });

        // ********************* getWtc30Hour *********************
        it('getWtc30Hour should return false if not eligible for wtcBasic', function() {
            expect(service.getWtc30Hour(false, input)).toEqual(false);
        });

        it('getWtc30Hour should return false if hours < 30', function() {
            input.you.workAndBenefits.hours = 29;
            expect(service.getWtc30Hour(true, input)).toEqual(false);
        });

        it('getWtc30Hour should return true if hours >= 30', function() {
            input.you.workAndBenefits.hours = 31;
            expect(service.getWtc30Hour(true, input)).toEqual(true);
        });

        it('getWtc30Hour should return true if children >= 0, combined hours are >=30 and your hours and age are >=16', function() {
            input.household.totalChildren = 1;
            input.household.partner = 'yes';

            input.you.personal.age = 16;
            input.you.workAndBenefits.hours = 16;

            input.partner.personal.age = 16;
            input.partner.workAndBenefits.hours = 14;

            expect(service.getWtc30Hour(true, input)).toEqual(true);
        });

        // ********************* getWtcEDA *********************
        it('getWtcEDA should return 0 if not eligible for wtcBasic', function() {
            expect(service.getWtcEDA(false, input)).toEqual(0);
        });

        it('getWtcEDA should return 0 if neither disabled', function() {
            input.you.personal.disabled = 'no';
            input.partner.personal.disabled = 'no';
            input.you.personal.severelyDisabled = 'no';
            input.partner.personal.severelyDisabled = 'no';
            expect(service.getWtcEDA(true, input)).toEqual(0);
        });

        it('getWtcEDA should return 1 if you disabled is yes and working 16 hours or more', function() {
            input.you.personal.disabled = 'yes';
            input.you.workAndBenefits.hours = 16;
            input.partner.personal.disabled = 'no';
            input.you.personal.severelyDisabled = 'no';
            input.partner.personal.severelyDisabled = 'no';
            expect(service.getWtcEDA(true, input)).toEqual(1);
        });

        it('getWtcEDA should return 1 if partner disabled is yes and working 16 hours or more', function() {
            input.you.personal.disabled = 'no';
            input.partner.personal.disabled = 'yes';
            input.partner.workAndBenefits.hours = 16;
            input.you.personal.severelyDisabled = 'no';
            input.partner.personal.severelyDisabled = 'no';
            expect(service.getWtcEDA(true, input)).toEqual(1);
        });

        it('getWtcEDA should return 2 if both you and partner disabled is yes and both working 16 hours or more', function() {
            input.you.personal.disabled = 'yes';
            input.you.workAndBenefits.hours = 16;
            input.partner.personal.disabled = 'yes';
            input.partner.workAndBenefits.hours = 16;
            input.you.personal.severelyDisabled = 'no';
            input.partner.personal.severelyDisabled = 'no';
            expect(service.getWtcEDA(true, input)).toEqual(2);
        });

        it('getWtcEDA should return 0 if you disabled is yes and not working 16 hours or more', function() {
            input.you.personal.disabled = 'yes';
            input.you.workAndBenefits.hours = 15;
            input.you.personal.severelyDisabled = 'no';
            input.partner.personal.severelyDisabled = 'no';
            expect(service.getWtcEDA(true, input)).toEqual(0);
        });

        it('getWtcEDA should return 0 if partner disabled is yes and not working 16 hours or more', function() {
            input.you.personal.disabled = 'no';
            input.partner.personal.disabled = 'yes';
            input.you.workAndBenefits.hours = 15;
            input.you.personal.severelyDisabled = 'no';
            input.partner.personal.severelyDisabled = 'no';
            expect(service.getWtcEDA(true, input)).toEqual(0);
        });

        // ********************* getWtcESDA *********************
        it('getWtcESDA should return 0 if not eligible for wtcBasic', function() {
            expect(service.getWtcESDA(false, input)).toEqual(0);
        });

        it('getWtcESDA should return 0 if neither severely disabled', function() {
            input.you.personal.disabled = 'no';
            input.partner.personal.disabled = 'no';
            input.you.personal.severelyDisabled = 'no';
            input.partner.personal.severelyDisabled = 'no';
            expect(service.getWtcESDA(true, input)).toEqual(0);
        });

        it('getWtcESDA should return 1 if you severely disabled is yes', function() {
            input.you.personal.disabled = 'no';
            input.partner.personal.disabled = 'no';
            input.you.personal.severelyDisabled = 'yes';
            input.partner.personal.severelyDisabled = 'no';
            expect(service.getWtcESDA(true, input)).toEqual(1);
        });

        it('getWtcESDA should return 1 if partner severely disabled is yes', function() {
            input.you.personal.disabled = 'no';
            input.partner.personal.disabled = 'no';
            input.you.personal.severelyDisabled = 'no';
            input.partner.personal.severelyDisabled = 'yes';
            expect(service.getWtcESDA(true, input)).toEqual(1);
        });

        it('getWtcESDA should return 2 if both you and partner severely disabled is yes', function() {
            input.you.personal.disabled = 'no';
            input.partner.personal.disabled = 'no';
            input.you.personal.severelyDisabled = 'yes';
            input.partner.personal.severelyDisabled = 'yes';
            expect(service.getWtcESDA(true, input)).toEqual(2);
        });

        // ********************* getWtcChildCare ************************
        //Single
        it('getWtcChildCare should return false if not eligible for wtcBasic', function() {
            expect(service.getWtcChildCare(false, input)).toEqual(false);
        });

        it('getWtcChildCare should return false if not working for 16 hours or incapacitated', function() {
            input.you.workAndBenefits.hours = 15;
            expect(service.getWtcChildCare(true, input)).toEqual(false);
        });

        it('getWtcChildCare should return true if working for 16 hours or more', function() {
            input.you.workAndBenefits.hours = 16;
            expect(service.getWtcChildCare(true, input)).toEqual(true);
        });

        it('getWtcChildCare should return true if single parent and not working for 16 hours but incapacitated (disabled)', function() {
            input.you.workAndBenefits.hours = 15;
            input.you.personal.disabled = 'yes';
            expect(service.getWtcChildCare(true, input)).toEqual(true);
        });

        it('getWtcChildCare should return true if single parent and not working for 16 hours but incapacitated (sev. disabled)', function() {
            input.you.workAndBenefits.hours = 15;
            input.you.personal.severelyDisabled = 'yes';
            expect(service.getWtcChildCare(true, input)).toEqual(true);
        });

        //Couple
        it('getWtcChildCare should return false if working for 16 hours or more but partner is working less than 16 hours', function() {
            input.household.partner = 'yes';

            input.you.workAndBenefits.hours = 16;
            input.partner.workAndBenefits.hours = 15;

            expect(service.getWtcChildCare(true, input)).toEqual(false);
        });

        it('getWtcChildCare should return true if working for 16 hours or more and partner is working', function() {
            input.household.partner = 'yes';

            input.you.workAndBenefits.hours = 16;
            input.partner.workAndBenefits.hours = 16;

            expect(service.getWtcChildCare(true, input)).toEqual(true);
        });

        it('getWtcChildCare should return true if working for 16 hours or more and partner is incapacitated', function() {
            input.household.partner = 'yes';

            input.you.workAndBenefits.hours = 16;
            input.partner.personal.disabled = 'yes';

            expect(service.getWtcChildCare(true, input)).toEqual(true);
        });

        it('getWtcChildCare should return false if incapacitated but partner is working less than 16 hours', function() {
            input.household.partner = 'yes';

            input.you.personal.disabled = 'yes';
            input.partner.workAndBenefits.hours = 15;

            expect(service.getWtcChildCare(true, input)).toEqual(false);
        });

        it('getWtcChildCare should return true if incapacitated and partner is working', function() {
            input.household.partner = 'yes';

            input.you.personal.disabled = 'yes';
            input.partner.workAndBenefits.hours = 16;

            expect(service.getWtcChildCare(true, input)).toEqual(true);
        });

        it('getWtcChildCare should return true if incapacitated and partner is incapacitated', function() {
            input.household.partner = 'yes';

            input.you.personal.disabled = 'yes';
            input.partner.personal.disabled = 'yes';

            expect(service.getWtcChildCare(true, input)).toEqual(true);
        });

        it('getWtcChildCare should return true if in prison and partner is incapacitated (disabled)', function() {
            input.household.partner = 'yes';

            input.you.personal.hospitalPrisonEtc = 'you';
            input.partner.personal.disabled = 'yes';

            expect(service.getWtcChildCare(true, input)).toEqual(true);
        });

        it('getWtcChildCare should return true if in prison and partner is incapacitated (sev. disabled)', function() {
            input.household.partner = 'yes';

            input.you.personal.hospitalPrisonEtc = 'you';
            input.partner.personal.severelyDisabled = 'yes';

            expect(service.getWtcChildCare(true, input)).toEqual(true);
        });

        it('getWtcChildCare should return true if incapacitated and partner is in prison', function() {
            input.household.partner = 'yes';

            input.you.personal.disabled = 'yes';
            input.you.personal.hospitalPrisonEtc = 'partner';

            expect(service.getWtcChildCare(true, input)).toEqual(true);
        });



        // ********************* getWtcChildCareNum *********************
        it('getWtcChildCareNum should return 0 if not eligible for wtcBasic', function() {
            expect(service.getWtcChildCareNum(false, input)).toEqual(0);
        });

        it('getWtcChildCareNum should return 0 if no children have childcare paid', function() {
            input.household.childCareNum = 0;
            input.household.childCareAmnt = 0.00;
            expect(service.getWtcChildCareNum(true, input)).toEqual(0);
        });

        it('getWtcChildCareNum should return 1 if 1 child has childcare paid', function() {
            input.household.childCareNum = 1;
            input.household.childCareAmnt = 300.00;
            expect(service.getWtcChildCareNum(true, input)).toEqual(1);
        });

        // ********************* getWtcChildCareAmnt *********************
        it('getWtcChildCareAmnt should return 0 if not eligible for wtcBasic', function() {
            expect(service.getWtcChildCareAmnt(false, input)).toEqual(0);
        });

        it('getWtcChildCareAmnt should return 0 if no children have childcare paid', function() {
            input.household.childCareNum = 0;
            input.household.childCareAmnt = 300.00;
            expect(service.getWtcChildCareAmnt(false, input)).toEqual(0);
        });

        it('getWtcChildCareAmnt should return 175 if 1 child has 175 childcare paid', function() {
            input.household.childCareNum = 1;
            input.household.childCareAmnt = 175.00;
            expect(service.getWtcChildCareAmnt(false, input)).toEqual(0);
        });

        it('getWtcChildCareAmnt should return 300 if 2 children have 300 childcare paid', function() {
            input.household.childCareNum = 2;
            input.household.childCareAmnt = 300.00;
            expect(service.getWtcChildCareAmnt(false, input)).toEqual(0);
        });


        // ********************* getCtcChildren *********************
        it('getCtcChildren should return 0 if no eligible children', function() {
            expect(service.getCtcChildren(input)).toEqual(0);
        });

        it('getCtcChildren should return 1 if 1 eligible child', function() {
            input.household.totalChildren = 1;
            input.household.disabledChildren = 0;
            input.household.severelyDisabledChildren = 0;
            expect(service.getCtcChildren(input)).toEqual(1);
        });

        // ********************* getCtcIDA *********************
        it('getCtcIDA should return 0 if no disabled children', function() {
            input.household.totalChildren = 1;
            input.household.disabledChildren = 0;
            input.household.severelyDisabledChildren = 0;
            expect(service.getCtcIDA(input)).toEqual(0);
        });

        it('getCtcIDA should return 1 if 1 disabled child', function() {
            input.household.totalChildren = 2;
            input.household.disabledChildren = 1;
            input.household.severelyDisabledChildren = 0;
            expect(service.getCtcIDA(input)).toEqual(1);
        });

        // ********************* getCtcISDA *********************
        it('getCtcISDA should return 0 if no severely disabled children', function() {
            input.household.totalChildren = 1;
            input.household.disabledChildren = 1;
            input.household.severelyDisabledChildren = 0;
            expect(service.getCtcISDA(input)).toEqual(0);
        });

        it('getCtcISDA should return 1 if 1 severely disabled child', function() {
            input.household.totalChildren = 3;
            input.household.disabledChildren = 2;
            input.household.severelyDisabledChildren = 1;
            expect(service.getCtcISDA(input)).toEqual(1);
        });

        // ********************* calculateTaxCredits *********************
        it('calculateTaxCredits should return 0 for wtc, ctc and income when no data is entered', function() {
            var data = service.calculateTaxCredits(input);
            expect(data.wtc).toEqual('0.00');
            expect(data.ctc).toEqual('0.00');
            expect(data.householdIncome).toEqual(0);
        });

        it('calculateTaxCredits should return ctc:0, wtc:4.48 and income:9500 for the entered criteria', function() {
            input.you.income.earnings=9500;
            input.you.personal.age=25;
            input.you.workAndBenefits.hours=37;

            var data = service.calculateTaxCredits(input);

            expect(data.ctc).toEqual('0.00');
            expect(data.wtc).toEqual('4.49');
            expect(data.householdIncome).toEqual(9500);
        });

        it('calculateTaxCredits should return ctc:470.01, wtc:911.02 and income:10200 for the entered criteria', function() {
            input.household.hasChildren='yes';
            input.household.totalChildren=2;
            input.household.childCareNum=2;
            input.household.childCareAmnt=300;

            input.you.personal.age=30;

            input.you.workAndBenefits.status='selfEmployed';
            input.you.workAndBenefits.hours=20;

            input.you.income.earnings=5500;
            input.you.income.smp=500;
            input.you.income.selfEmployedProfit=1000;
            input.you.income.otherIncome=4500;

            var data = service.calculateTaxCredits(input);

            expect(data.ctc).toEqual('470.01');
            expect(data.wtc).toEqual('911.02');
            expect(data.householdIncome).toEqual(10200);
        });

        it('calculateTaxCredits should return ctc:0, wtc:367.81 and income:2000 for the entered criteria', function() {
            input.household.partner='yes';

            input.you.workAndBenefits.status='notWorking';

            input.you.personal.age=30;

            input.partner.personal.age=25;
            input.partner.workAndBenefits.status='working';
            input.partner.workAndBenefits.hours=40;

            input.partner.income.earnings=2000;

            var data = service.calculateTaxCredits(input);

            expect(data.ctc).toEqual('0.00');
            expect(data.wtc).toEqual('367.81');
            expect(data.householdIncome).toEqual(2000);
        });

        it('calculateTaxCredits should return ctc:256.07, wtc:367.81 and income:2000 for the entered criteria', function() {
            input.household.partner='yes';
            input.household.hasChildren='yes';
            input.household.totalChildren=1;

            input.you.workAndBenefits.status='working';
            input.you.workAndBenefits.hours=15;

            input.you.personal.age=16;

            input.partner.personal.age=16;
            input.partner.workAndBenefits.status='working';
            input.partner.workAndBenefits.hours=16;

            input.partner.income.earnings=2000;

            var data = service.calculateTaxCredits(input);

            expect(data.ctc).toEqual('256.07');
            expect(data.wtc).toEqual('367.81');
            expect(data.householdIncome).toEqual(2000);
        });

        it('calculateTaxCredits should return ctc:256.07, wtc:367.81 and income:2000 for the entered criteria', function() {
            input.household.partner='yes';
            input.household.hasChildren='yes';
            input.household.totalChildren=1;

            input.household.childCareNum=1;
            input.household.childCareAmnt=175;

            input.you.workAndBenefits.status='working';
            input.you.workAndBenefits.hours=14;

            input.you.personal.age=16;

            input.partner.personal.age=16;
            input.partner.workAndBenefits.status='working';
            input.partner.workAndBenefits.hours=16;

            input.partner.income.earnings=2000;

            var data = service.calculateTaxCredits(input);

            expect(data.ctc).toEqual('256.07');
            expect(data.wtc).toEqual('367.81');
            expect(data.householdIncome).toEqual(2000);
        });
        
        it('calculateTaxCredits should return ctc:0, wtc:0 and income:2000 when the total award is less than £26', function() {
            input.household.hasChildren='yes';
            input.household.totalChildren=1;

            input.you.workAndBenefits.status='working';
            input.you.workAndBenefits.hours=16;

            input.you.personal.age=16;

            input.you.income.earnings=19050;

            var data = service.calculateTaxCredits(input);

            expect(data.ctc).toEqual('0.00');
            expect(data.wtc).toEqual('0.00');
            //expect(data.householdIncome).toEqual(19050);
        });

        it('calculateTaxCredits should be formatted to two decimal places', function() {
            input.you.workAndBenefits.status='working';
            input.you.workAndBenefits.hours=16;

            input.household.hasChildren='yes';
            input.household.totalChildren=1;
            input.household.disabledChildren=1;
            input.household.severelyDisabledChildren=1;

            input.you.personal.age=36;

            input.partner.income.earnings=6000;

            var data = service.calculateTaxCredits(input);

            expect(data.ctc).toEqual('596.08');
            expect(data.wtc).toEqual('226.10');
            expect(data.householdIncome).toEqual(6000);
        });

        it('calculateTaxCredits should not be tapered where benefits are given', function() {
            input.household.hasChildren='yes';
            input.you.workAndBenefits.benefits='yes';
            input.household.totalChildren=1;

            input.you.workAndBenefits.status='working';
            input.you.workAndBenefits.hours=16;

            input.you.personal.age=16;

            input.you.income.earnings=50000;

            var data = service.calculateTaxCredits(input);

            expect(data.ctc).toEqual('256.07');
            expect(data.wtc).toEqual('305.48');
            //expect(data.householdIncome).toEqual(50000);
        });

    // ********************* calculateWTC *********************
    it('calcWTC should return 2770.35 for scenario 1A - Single >30 hours', function() {
       expect(2770.35).toEqual(service.calcWTC(true, false, true, 0, 0));
    });

    it('calcWTC should return 4781.5 for scenario 1B - Couple >30 hours', function() {
      expect(4781.5).toEqual(service.calcWTC(true, true, true, 0, 0));
    });

    it('calcWTC should return 4931.15 for scenario 1C - Single, 16-29 hours, disabled', function() {
      expect(4931.15).toEqual(service.calcWTC(true, false, false, 1, 0));
    });

    it('calcWTC should return 5741.45 for scenario 1D - Single, 30 hours, disabled', function() {
      expect(5741.45).toEqual(service.calcWTC(true, false, true, 1, 0));
    });

    it('calcWTC should return 6942.3 for scenario 1E - Couple, 16-29 hours, disabled', function() {
      expect(6942.3).toEqual(service.calcWTC(true, true, false, 1, 0));
    });

    it('calcWTC should return 2770.35 for scenario 1 - Single, 30 hr element', function() {
      expect(2770.35).toEqual(service.calcWTC(true, false, true, 0, 0));
    });

    it('calcWTC should return 1960.05 for scenario 2 - Single, with just the basic element', function() {
      expect(1960.05).toEqual(service.calcWTC(true, false, false, 0, 0));
    });

      // similar to test scenario 2 ....included this to test the case with the income variation
    it('calcWTC should return 1960.05 for scenario 3 - Single, with just the basic element', function() {
      expect(1960.05).toEqual(service.calcWTC(true, false, false, 0, 0));
    });

    it('calcWTC should return 5741.45 for scenario 4 - Single,basic element,30 hr element, disabled', function() {
      expect(5741.45).toEqual(service.calcWTC(true, false, true, 1, 0));
    });

    it('calcWTC should return 4931.15 for scenario 5 - Single,basic element, disabled', function() {
      expect(4931.15).toEqual(service.calcWTC(true, false, false, 1, 0));
    });

    it('calcWTC should return 7018.95 for scenario 6 - Single,basic element,30 hr element disabled', function() {
      expect(7018.95).toEqual(service.calcWTC(true, false, true, 1, 1));
    });

    it('calcWTC should return 6208.65 for scenario 7 - Single,basic element, disabled', function() {
      expect(6208.65).toEqual(service.calcWTC(true, false, false, 1, 1));
    });

    it('calcWTC should return 4781.50 for scenario 8 - Couple,basic element, 30 hr element', function() {
      expect(4781.50).toEqual(service.calcWTC(true, true, true, 0, 0));
    });

    it('calcWTC should return 3971.20 for scenario 9 - Couple,basic element, 30 hr element', function() {
      expect(3971.20).toEqual(service.calcWTC(true, true, false, 0, 0));
    });

  // similar to test scenario 9 ....included this to test the case with the income variation
    it('calcWTC should return 3971.20 for scenario 10 - Couple,basic element, 30 hr element', function() {
      expect(3971.20).toEqual(service.calcWTC(true, true, false, 0, 0));
    });

    it('calcWTC should return 7752.60 for scenario 11 - Couple,basic element, 30 hr element, disabled', function() {
      expect(7752.60).toEqual(service.calcWTC(true, true, true, 1, 0));
    });

    it('calcWTC should return 6942.30 for scenario 12 - Couple,basic element, disabled', function() {
      expect(6942.30).toEqual(service.calcWTC(true, true, false, 1, 0));
    });

    it('calcWTC should return 9030.10 for scenario 13 - Couple,basic element, 30 hr element, disabled,severely disabled', function() {
      expect(9030.10).toEqual(service.calcWTC(true, true, true, 1, 1));
    });

    it('calcWTC should return 8219.80 for scenario 14 - Couple,basic element,disabled,severely disabled', function() {
      expect(8219.80).toEqual(service.calcWTC(true, true, false, 1, 1));
    });

    // calculating the wtccc (WTC+Childcare element)
    it('calcWTCCC should return 0 for scenario 1 -CC No WTC so no WTCCC', function() {
      expect(0).toEqual(service.calcWTCCC(false, false, 1, 175));
    });

    it('calcWTCCC should return 0 for scenario 2 -CC No WTC so no WTCCC', function() {
      expect(0).toEqual(service.calcWTCCC(false, false, 1, 170));
    });

    it('calcWTCCC should return 6370 for scenario 3 -CC with Basic element, childCareNum,childCareCost', function() {
      expect(6370).toEqual(service.calcWTCCC(true, true, 1, 175));
    });

    it('calcWTCCC should return 6387.5 for scenario 4 -CC with Basic element, childCareNum,childCareCost', function() {
      expect(6387.5).toEqual(service.calcWTCCC(true, true, 1, 175.01));
    });

    it('calcWTCCC should return 6333.6 for scenario 5', function() {
      expect(6333.6).toEqual(service.calcWTCCC(true, true, 1, 174.00));
    });

    it('calcWTCCC should return 10920 for scenario 6 -CC with Basic element, childCareNum,childCareCost', function() {
      expect(10920).toEqual(service.calcWTCCC(true, true, 2, 300.00));
    });

    it('calcWTCCC should return 10919.64 for scenario 7 -CC with Basic element, childCareNum,childCareCost', function() {
      expect(10919.64).toEqual(service.calcWTCCC(true, true, 2, 299.99));
    });

    it('calcWTCCC should return 10950.73 for scenario 8 -CC with Basic element, childCareNum,childCareCost', function() {
      expect(10950.73).toEqual(service.calcWTCCC(true, true, 2, 300.01));
    });

    it('calcWTCCC should return 10950.73 for scenario 9 -CC with Basic element, childCareNum,childCareCost', function() {
      expect(10950.73).toEqual(service.calcWTCCC(true, true, 3, 300.01));
    });

  //Taper tests
      it('Taper WTC should return 928.05 for scenario 1A -  basic WTC with income of £6000', function() {
        var wtc = service.taperWTC(6000, 1960.05, 3850, 0.48);
        expect(wtc).toEqual(928.05);
      });

      it('Taper WTC should return 0 for scenario 1B - basic WTC with income of £8000', function() {
        var wtc = service.taperWTC(8000, 1960.05, 3850, 0.48);
        expect(wtc).toEqual(0);
      });

      it('Taper WTCCC should return £1820 for scenario 2A - basic WTCCC with income of £6000 and childcare of £50 p/w', function() {
        var wtccc = service.taperWTCCC(6000, 1820, 1960.05, 3850, 0.48);
        expect(wtccc).toEqual(1820);
      });

      it('Taper WTCCC should return £1788.06 for scenario 2B - basic WTCCC with income of £8000 and childcare of £50 p/w', function() {
        var wtccc = service.taperWTCCC(8000, 1820, 1960.05, 3850, 0.48);
        expect(wtccc).toEqual(1788.06);
      });

      it('Taper WTCCC should return £0 for scenario 2C - basic WTCCC with income of 12000 and childcare of £50 p/w', function() {
        var wtccc = service.taperWTCCC(12000, 1820, 1960.05, 3850, 0.48);
        expect(wtccc).toEqual(0);
      });

      it('Taper CTC should return £2361.30 for scenario 3A - basic CTC for 1 child with income of 13000', function() {
        var ctc = service.taperCTC(13000, 2781.30, 0, 0, 3850, 12125, 0.48);

        expect(ctc).toEqual(2361.30);
      });

      it('Taper CTC should return £2361.30 for scenario 3B - basic CTC for 1 child with income of 13000', function() {
        var ctc = service.taperCTC(13000, 2781.30, 1960.05, 0, 3850, 12125, 0.48);

        expect(ctc).toEqual(2361.30);
      });

      it('Taper CTC should return £0 for scenario 3C - basic CTC for 1 child with income of 22000', function() {
        var ctc = service.taperCTC(22000, 2781.30, 1960.05, 0, 3850, 12125, 0.48);

        expect(ctc).toEqual(0);
      });

      it('Taper CTCF should return £547.5 for scenario 4A - CTCF for 1 child with income of 17000', function() {
        var ctcf = service.taperCTCF(17000, 547.50, 2781.30, 1960.05, 0, 3850, 12125, 0.48);

        expect(ctcf).toEqual(547.5);
      });

      it('Taper CTCF should return £0 for scenario 4A - CTCF for 1 child with income of 22000', function() {
        var ctcf = service.taperCTCF(22000, 547.50, 2781.30, 1960.05, 0, 3850, 12125, 0.48);

        expect(ctcf).toEqual(0);
      });

    // tests for CTC only
    it('calcCTC should return 2781.30 for scenario 1 - 1 Child, 0 disabled, 0 severely disabled', function() {
      expect(2781.30).toEqual(service.calcCTC(1, 0, 0));
    });

    it('calcCTC should return 5923.95 for scenario 2 - 1 Child, 1 disabled, 0 severely disabled', function() {
      expect(5923.95).toEqual(service.calcCTC(1, 1, 0));
    });

    it('calcCTC should return 7201.45 for scenario 3 - 1 Child, 1 disabled, 1 severely disabled', function() {
      expect(7201.45).toEqual(service.calcCTC(1, 1, 1));
    });

    it('calcCTC should return 5562.60 for scenario 4 - 2 Children, 0 disabled, 0 severely disabled', function() {
      expect(5562.60).toEqual(service.calcCTC(2, 0, 0));
    });

    it('calcCTC should return 8705.25 for scenario 5 - 2 Children, 1 disabled, 0 severely disabled', function() {
      expect(8705.25).toEqual(service.calcCTC(2, 1, 0));
    });

    it('calcCTC should return 9982.75 for scenario 6 - 2 Children, 1 disabled, 1 severely disabled', function() {
      expect(9982.75).toEqual(service.calcCTC(2, 1, 1));
    });

    it('calcCTC should return 14402.90 for scenario 7 - 2 Children, 2 disabled, 2 severely disabled', function() {
      expect(14402.90).toEqual(service.calcCTC(2, 2, 2));
    });

    it('calcCTC should return 17184.20 for scenario 8 - 3 Children, 2 disabled, 2 severely disabled', function() {
      expect(17184.20).toEqual(service.calcCTC(3, 2, 2));
    });

    it('calcCTC should return 19049.35 for scenario 9 - 3 Children, 3 disabled, 1 severely disabled', function() {
      expect(19049.35).toEqual(service.calcCTC(3, 3, 1));
    });

    it('calcCTC should return 20326.85 for scenario 10 - 3 Children, 3 disabled, 2 severely disabled', function() {
      expect(20326.85).toEqual(service.calcCTC(3, 3, 2));
    });

    it('calcCTC should return 21604.35 for scenario 11 - 3 Children, 3 disabled, 3 severely disabled', function() {
      expect(21604.35).toEqual(service.calcCTC(3, 3, 3));
    });

    it('calcCTC should return 17771.85 for scenario 12 - 3 Children, 3 disabled, 0 severely disabled', function() {
      expect(17771.85).toEqual(service.calcCTC(3, 3, 0));
    });

    it('calcCTC should return 8343.90 for scenario 13 - 3 Children, 0 disabled, 0 severely disabled', function() {
      expect(8343.90).toEqual(service.calcCTC(3, 0, 0));
    });

    it('getIncomeBracket should return "under" for an income below 10600', function() {
      expect(service.getIncomeBracket(10599.99)).toEqual('under');
    });

    it('getIncomeBracket should return "in" for an income between 10600 and 10800', function() {
      expect(service.getIncomeBracket(10600)).toEqual('in');
    });

    it('getIncomeBracket should return "over" for an income equal to or above 10800', function() {
      expect(service.getIncomeBracket(10800)).toEqual('over');
    });
  });
}());
