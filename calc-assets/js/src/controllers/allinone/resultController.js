(function() {
    "use strict";

    var app = require("../module");

    var resultController = function($scope, $location, $anchorScroll, dataService, modelValidationService, navigationService, calculationService) {

        var init = require("../../utilities/initController");
        init($scope, $location, $anchorScroll, 'result', dataService, navigationService);

        $scope.viewDetails = function() {
            navigationService.viewDetails($scope.data, $location);
        };

        $scope.printView = function() {
            navigationService.printView($scope.data, $location);
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
                    result.freehold.residential.from = calculationService.calculateResidentialPremiumSlice($scope.data.premium);
                    result.freehold.residential.before = calculationService.calculateResidentialPremiumSlab($scope.data.premium);                    
                }
                else if ($scope.data.propertyType === 'Non-residential'){
                    result.freehold.nonResidential = calculationService.calculateNonResidentialPremiumSlab($scope.data.premium, 0); 
                }
            }
            else if ($scope.data.holdingType === 'Leasehold') {
                var rentTax = -1;
                var rentsArray = [rent.displayYearOneRent ? $scope.data.year1Rent : 0, rent.displayYearTwoRent ? $scope.data.year2Rent : 0, rent.displayYearThreeRent ? $scope.data.year3Rent : 0, rent.displayYearFourRent ? $scope.data.year4Rent : 0, rent.displayYearFiveRent ? $scope.data.year5Rent : 0];
                var npv = calculationService.calculateNPV($scope.data.leaseTerm.years, $scope.data.leaseTerm.days, $scope.data.leaseTerm.daysInPartialYear, rentsArray);
                if ($scope.data.propertyType === 'Residential') {

                    var fromPremiumTax = -1;
                    var beforePremiumTax = -1;
                    result.leasehold.residential.from = calculationService.calculateResidentialPremiumSlice($scope.data.premium);
                    fromPremiumTax = result.leasehold.residential.from.totalSDLT;

                    result.leasehold.residential.before = calculationService.calculateResidentialPremiumSlab($scope.data.premium); 
                    beforePremiumTax = result.leasehold.residential.before.taxDue;

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
                    var relevantRent = ($scope.data.relevantRent === undefined) ? 0 : $scope.data.relevantRent;
                    premiumTax = calculationService.calculateNonResidentialPremiumSlab($scope.data.premium, relevantRent).taxDue;
                    rentTax = calculationService.calculateNonResidentialLeaseSlice(npv).totalSDLT;

                    result.leasehold.nonResidential.npv = npv;
                    result.leasehold.nonResidential.rentTax = rentTax;
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

    };

    app.controller('resultController', ['$scope', '$location', '$anchorScroll', 'dataService', 'modelValidationService', 'navigationService', 'calculationService', resultController ]);
}());
