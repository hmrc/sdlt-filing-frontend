(function() {
    "use strict";

    var app = require("../module");

    var resultController = function($scope, $location, $anchorScroll, dataService, modelValidationService, navigationService, calculationService) {

        var init = require("../../utilities/initController");
        init($scope, $location, $anchorScroll, 'result', dataService, navigationService);


        $scope.data.resultTest = [
            {
                resultHeading : "Results based on SDLT rules from 1 April 2016",
                resultHint : undefined,
                totalTax : 6000,
                npv : undefined,
                taxCalcs : [
                    {
                        taxFor : "premium",
                        calcType : "slice",
                        detailHeading : "This is a breakdown of how the amount of SDLT was calculated based on the rules from 1 April 2016",
                        taxDue : 6000,
                        slices : [
                            {"from": 0, "to": 125000, "rate": 3, "taxDue": 3000},
                            {"from": 125000, "to": 250000, "rate": 5, "taxDue": 2000},
                            {"from": 250000, "to": 925000, "rate": 8, "taxDue": 1000},
                            {"from": 925000, "to": 1500000, "rate": 13, "taxDue": 0},
                            {"from": 1500000, "to": -1, "rate": 15, "taxDue": 0}
                        ]
                    }
                ],
                rentTax : undefined
            },
            {
                resultHeading : "Results based on SDLT rules before 1 April 2016",
                resultHint : "You may be entitled to pay SDLT using the old rules if you exchanged contracts before 26 November 2015.",
                totalTax : 3000,
                npv : undefined,
                taxCalcs : [
                    {
                        taxFor : "premium",
                        calcType : "slice",
                        detailHeading : "This is a breakdown of how the amount of SDLT was calculated based on the rules before 1 April 2016",
                        taxDue : 3000,
                        slices : [
                            {"from": 0, "to": 125000, "rate": 0, "taxDue": 0},
                            {"from": 125000, "to": 250000, "rate": 2, "taxDue": 1000},
                            {"from": 250000, "to": 925000, "rate": 5, "taxDue": 2000},
                            {"from": 925000, "to": 1500000, "rate": 10, "taxDue": 0},
                            {"from": 1500000, "to": -1, "rate": 12, "taxDue": 0}
                        ]
                    }
                ],
                rentTax : undefined
            }
        ];



        

        $scope.viewDetails = function(resultIndex, taxCalcIndex) {
            $scope.data.resultIndex = resultIndex;
            $scope.data.taxCalcIndex = taxCalcIndex;
            dataService.updateModel($scope.data);
            navigationService.viewDetails($scope.data, $location);
        };

        $scope.printView = function() {
            navigationService.printView($scope.data, $location);
        };

        $scope.effDateAfterAprilCutOff = function(){
            var cutOffDate = new Date("April 1, 2016");
            return $scope.data.effectiveDate >= cutOffDate;
        };

        $scope.isAdditionalProperty = function(){
            return ($scope.data.twoOrMoreProperties === 'Yes' && $scope.data.replaceMainResidence === 'No') && $scope.effDateAfterAprilCutOff();
        };

        var rent = require("../../utilities/displayLeasedYearRentFields");
        rent = rent().getFunctions($scope.data);

        if (modelValidationService.validate($scope.data).isModelValid) {
            var result = {
                freehold  : {
                    residential : {
                        from : {},
                        before : {}
                    },
                    nonResidential : {}
                },
                leasehold  : {
                    residential : {
                        totalTax : -1,
                        npv : -1,
                        rentTax : -1,
                        premiumTax : -1
                    },
                    nonResidential : {
                        totalTax : -1,
                        npv : -1,
                        rentTax : -1,
                        premiumTax : -1
                    }
                }
            };
            if ($scope.data.holdingType === 'Freehold') {
                if ($scope.data.propertyType === 'Residential') {
                    if($scope.isAdditionalProperty()) {
                        result.freehold.residential.from = calculationService.calculate201604SecondHomeSlice($scope.data.premium);
                        result.freehold.residential.before = calculationService.calculateResidentialPremiumSlice($scope.data.premium);
                    } else {
                        result.freehold.residential.from = calculationService.calculateResidentialPremiumSlice($scope.data.premium);
                        result.freehold.residential.before = calculationService.calculateResidentialPremiumSlab($scope.data.premium);
                    }
                }
                else if ($scope.data.propertyType === 'Non-residential'){
                    result.freehold.nonResidential = calculationService.calculateNonResidentialPremiumSlab($scope.data.premium, true); 
                }
            }
            else if ($scope.data.holdingType === 'Leasehold') {
                var rentTax = -1;
                var rentsArray = [parseFloat($scope.data.year1Rent), rent.displayYearTwoRent ? parseFloat($scope.data.year2Rent) : 0, rent.displayYearThreeRent ? parseFloat($scope.data.year3Rent) : 0, rent.displayYearFourRent ? parseFloat($scope.data.year4Rent) : 0, rent.displayYearFiveRent ? parseFloat($scope.data.year5Rent) : 0];
                var npv = calculationService.calculateNPV($scope.data.leaseTerm.years, $scope.data.leaseTerm.days, $scope.data.leaseTerm.daysInPartialYear, rentsArray);
                

                if ($scope.data.propertyType === 'Residential') {
                    var fromPremiumTax;
                    var beforePremiumTax;

                    if($scope.isAdditionalProperty()) {
                        result.leasehold.residential.from = calculationService.calculate201604SecondHomeSlice($scope.data.premium);
                        fromPremiumTax = result.leasehold.residential.from.totalSDLT;

                        result.leasehold.residential.before = calculationService.calculateResidentialPremiumSlice($scope.data.premium); 
                        beforePremiumTax = result.leasehold.residential.before.totalSDLT;
                    } else {
                        result.leasehold.residential.from = calculationService.calculateResidentialPremiumSlice($scope.data.premium);
                        fromPremiumTax = result.leasehold.residential.from.totalSDLT;

                        result.leasehold.residential.before = calculationService.calculateResidentialPremiumSlab($scope.data.premium); 
                        beforePremiumTax = result.leasehold.residential.before.taxDue;
                    }

                    
                    rentTax = calculationService.calculateResidentialLeaseSlice(npv).totalSDLT;

                    result.leasehold.residential.npv = npv;
                    result.leasehold.residential.rentTax = rentTax;
                    result.leasehold.residential.from.premiumTax = fromPremiumTax;
                    result.leasehold.residential.from.totalTax = rentTax + fromPremiumTax;
                    result.leasehold.residential.before.premiumTax = beforePremiumTax;
                    result.leasehold.residential.before.totalTax = rentTax + beforePremiumTax;
                }
                else if ($scope.data.propertyType === 'Non-residential'){
                    var premiumTax = -1;
                    var zeroRate = false;
                    var validator = require("../../utilities/validator")();
                    var checkRelevant = validator.relevantRentCheck([$scope.data.year1Rent, $scope.data.year2Rent, $scope.data.year3Rent, $scope.data.year4Rent, $scope.data.year5Rent]);
                    if ($scope.data.premium < 150000 && checkRelevant) {
                        var relevantRent = ($scope.data.relevantRent === undefined) ? 0 : $scope.data.relevantRent;
                        if(relevantRent < 1000){
                            zeroRate = true;
                        }
                    }
                    var premiumTaxBreakdown = calculationService.calculateNonResidentialPremiumSlab($scope.data.premium, zeroRate);
                    premiumTax = premiumTaxBreakdown.taxDue;
                    var premiumTaxRate = premiumTaxBreakdown.rate;
                    rentTax = calculationService.calculateNonResidentialLeaseSlice(npv).totalSDLT;

                    result.leasehold.nonResidential.npv = npv;
                    result.leasehold.nonResidential.rentTax = rentTax;
                    result.leasehold.nonResidential.premiumTaxRate = premiumTaxRate;
                    result.leasehold.nonResidential.premiumTax = premiumTax;
                    result.leasehold.nonResidential.totalTax = rentTax + premiumTax;
                }
            }
            $scope.data.result = result;
            dataService.updateModel($scope.data);
        }
        else {
            $location.path('summary');
        }


        $scope.effDateAfterCutOff = function(){
            var cutOffDate = new Date("December 4, 2014");
            return $scope.data.effectiveDate >= cutOffDate;
        };


        $scope.getHeading = function() {
            if($scope.effDateAfterCutOff()) {
                return "Results based on SDLT rules before 4 December 2014";
            }
        };

    };

    app.controller('resultController', ['$scope', '$location', '$anchorScroll', 'dataService', 'modelValidationService', 'navigationService', 'calculationService', resultController ]);
}());
