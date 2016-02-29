(function() {
    "use strict";

    var app = require("../module");

    var resultController = function($scope, $location, $anchorScroll, dataService, modelValidationService, navigationService, calculationService) {

        var init = require("../../utilities/initController");
        init($scope, $location, $anchorScroll, 'result', dataService, navigationService);

        $scope.viewDetails = function(latestOrPrevious) {
            $scope.data.latestOrPrevious = latestOrPrevious;
            dataService.updateModel($scope.data);
            navigationService.viewDetails($scope.data, $location);
        };

        $scope.printView = function() {
            navigationService.printView($scope.data, $location);
        };

        $scope.effDateOnOrAfter = function(compDate){
            return $scope.data.effectiveDate >= compDate;
        };

        $scope.isAdditionalProperty = function(){
            return ($scope.data.twoOrMoreProperties === 'Yes' && $scope.data.replaceMainResidence === 'No');
        };

        var rent = require("../../utilities/displayLeasedYearRentFields");
        rent = rent().getFunctions($scope.data);

        if (modelValidationService.validate($scope.data).isModelValid) {
            var result = {};

            if ($scope.data.holdingType === 'Freehold') {
                if ($scope.data.propertyType === 'Residential') {
                    if ($scope.effDateOnOrAfter(new Date(2016, 3, 1)) && $scope.isAdditionalProperty()) {
                        result = calculationService.calcFreeResPremAddProp_201604_Undef($scope.data.premium);
                    } else if ($scope.effDateOnOrAfter(new Date(2014, 11, 4))) {
                        result = calculationService.calcFreeResPrem_201412_Undef($scope.data.premium);
                    } else {
                        result = calculationService.calcFreeResPrem_201203_201412($scope.data.premium);
                    }
                } else if ($scope.data.propertyType === 'Non-residential') {
                    result = calculationService.calcFreeNonResPrem_201203_Undef($scope.data.premium);
                }
            } else if ($scope.data.holdingType === 'Leasehold') {
                var rentTax = -1;
                var rentsArray = [parseFloat($scope.data.year1Rent), rent.displayYearTwoRent ? parseFloat($scope.data.year2Rent) : 0, rent.displayYearThreeRent ? parseFloat($scope.data.year3Rent) : 0, rent.displayYearFourRent ? parseFloat($scope.data.year4Rent) : 0, rent.displayYearFiveRent ? parseFloat($scope.data.year5Rent) : 0];
                var npv = calculationService.calculateNPV($scope.data.leaseTerm.years, $scope.data.leaseTerm.days, $scope.data.leaseTerm.daysInPartialYear, rentsArray);
                
                if ($scope.data.propertyType === 'Residential') {
                    if ($scope.effDateOnOrAfter(new Date("April 1, 2016")) && $scope.isAdditionalProperty()) {
                        result = calculationService.calcLeaseResPremAndRentAddProp_201604_Undef($scope.data.premium, $scope.data.npv);
                    } else if ($scope.effDateOnOrAfter(new Date("December 4, 2014"))) {
                        result = calculationService.calcLeaseResPremAndRent_201412_Undef($scope.data.premium, $scope.data.npv);
                    } else {
                        result = calculationService.calcLeaseResPremAndRent_201203_201412($scope.data.premium, $scope.data.npv);
                    }
                } else if ($scope.data.propertyType === 'Non-residential'){
                    var zeroRate = false;
                    var validator = require("../../utilities/validator")();
                    var anyRentAbove2000 = validator.checkForRentAbove2000([$scope.data.year1Rent, $scope.data.year2Rent, $scope.data.year3Rent, $scope.data.year4Rent, $scope.data.year5Rent]);
                    if ($scope.data.premium < 150000 && anyRentAbove2000) {
                        var relevantRent = ($scope.data.relevantRent === undefined) ? 0 : $scope.data.relevantRent;
                        if(relevantRent < 1000){
                            zeroRate = true;
                        }
                    }
                    result = calculationService.calcLeaseNonResPremAndRent_201203_Undef($scope.data.premium, $scope.data.npv, zeroRate);
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
