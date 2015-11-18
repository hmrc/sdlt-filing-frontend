(function() {
    "use strict";

    var app = require("../module");

    var resultController = function($scope, $location, dataService, modelValidationService, navigationService, calculationService) {
        
        var pageName = 'result';
        navigationService.logView(pageName);
        $scope.data = dataService.getModel();

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
                var premiumTax = -1;
                var rentsArray = [rent.displayYearOneRent ? $scope.data.year1Rent : 0, rent.displayYearTwoRent ? $scope.data.year2Rent : 0, rent.displayYearThreeRent ? $scope.data.year3Rent : 0, rent.displayYearFourRent ? $scope.data.year4Rent : 0, rent.displayYearFiveRent ? $scope.data.year5Rent : 0];
                var npv = calculationService.calculateNPV($scope.data.leaseTerm.years, $scope.data.leaseTerm.days, $scope.data.leaseTerm.daysInPartialYear, rentsArray);
                if ($scope.data.propertyType === 'Residential') {
                    rentTax = calculationService.calculateResidentialLeaseSlab($scope.data.premium, npv).taxDue;
                    premiumTax = calculationService.calculateResidentialPremiumSlab($scope.data.premium).taxDue;

                    result.leasehold.residential.npv = npv;
                    result.leasehold.residential.rentTax = rentTax;
                    result.leasehold.residential.premiumTax = premiumTax;
                    result.leasehold.residential.totalTax = rentTax + premiumTax;
                }
                else if ($scope.data.propertyType === 'Non-residential'){
                    rentTax = calculationService.calculateNonResidentialLeaseSlab($scope.data.premium, npv).taxDue;
                    premiumTax = calculationService.calculateNonResidentialPremiumSlab($scope.data.premium, $scope.data.relevantRent).taxDue;

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

    app.controller('resultController', ['$scope', '$location', 'dataService', 'modelValidationService', 'navigationService', 'calculationService', resultController ]);
}());
