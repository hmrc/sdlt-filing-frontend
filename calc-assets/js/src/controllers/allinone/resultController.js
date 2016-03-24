(function() {
    "use strict";

    var app = require("../module");

    var resultController = function($scope, $location, $anchorScroll, dataService, modelValidationService, navigationService, calculationService) {

        var init = require("../../utilities/initController");
        init($scope, $location, $anchorScroll, 'result', dataService, navigationService);

        $scope.viewDetails = function(resultIndex, taxCalcIndex) {
            $scope.data.resultIndex = resultIndex;
            $scope.data.taxCalcIndex = taxCalcIndex;
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

        var validator = require("../../utilities/validator")();
        var allRentsBelow2000 = validator.checkAllRentsBelow2000($scope.data);
        var prevCalcReqd = false;

        var rent = require("../../utilities/displayLeasedYearRentFields");
        rent = rent().getFunctions($scope.data);
        if (modelValidationService.validate($scope.data).isModelValid) {
            var result = {};

            if ($scope.data.holdingType === 'Freehold') {
                if ($scope.data.propertyType === 'Residential') {
                    if ($scope.effDateOnOrAfter(new Date(2016, 3, 1)) && $scope.data.individual === 'Yes' && $scope.isAdditionalProperty()) {
                        result = calculationService.calcFreeResPremAddProp_201604_Undef($scope.data.premium, true);
                    } else if ($scope.effDateOnOrAfter(new Date(2016, 3, 1)) && $scope.data.individual === 'No') {
                        result = calculationService.calcFreeResPremAddProp_201604_Undef($scope.data.premium, false);
                    } else if ($scope.effDateOnOrAfter(new Date(2014, 11, 4))) {
                        result = calculationService.calcFreeResPrem_201412_Undef($scope.data.premium);
                    } else {
                        result = calculationService.calcFreeResPrem_201203_201412($scope.data.premium);
                    }
                } else  { // propertyType === 'Non-residential') {
                    if($scope.effDateOnOrAfter(new Date(2016, 2, 17))) {
                        result = calculationService.calcFreeNonResPrem_201603_Undef($scope.data.premium);
                    } else {
                        result = calculationService.calcFreeNonResPrem_201203_201603($scope.data.premium);
                    }
                }
            } else { // holdingType === 'Leasehold'
                var rentTax = -1;
                var rentsArray = [parseFloat($scope.data.year1Rent), rent.displayYearTwoRent ? parseFloat($scope.data.year2Rent) : 0, rent.displayYearThreeRent ? parseFloat($scope.data.year3Rent) : 0, rent.displayYearFourRent ? parseFloat($scope.data.year4Rent) : 0, rent.displayYearFiveRent ? parseFloat($scope.data.year5Rent) : 0];
                var npv = calculationService.calculateNPV($scope.data.leaseTerm.years, $scope.data.leaseTerm.days, $scope.data.leaseTerm.daysInPartialYear, rentsArray);
                $scope.data.npv = npv;

                if ($scope.data.propertyType === 'Residential') {
                    if ($scope.effDateOnOrAfter(new Date("April 1, 2016")) && $scope.data.individual === 'Yes' && $scope.isAdditionalProperty()) {
                        result = calculationService.calcLeaseResPremAndRentAddProp_201604_Undef($scope.data.premium, $scope.data.npv, true);
                    } else if ($scope.effDateOnOrAfter(new Date("April 1, 2016")) && $scope.data.individual === 'No') {
                        result = calculationService.calcLeaseResPremAndRentAddProp_201604_Undef($scope.data.premium, $scope.data.npv, false);
                    } else if ($scope.effDateOnOrAfter(new Date("December 4, 2014"))) {
                        result = calculationService.calcLeaseResPremAndRent_201412_Undef($scope.data.premium, $scope.data.npv);
                    } else {
                        result = calculationService.calcLeaseResPremAndRent_201203_201412($scope.data.premium, $scope.data.npv);
                    }
                } else { // propertyType === 'Non-residential'
                    var zeroRate = false;
                    if ($scope.data.premium < 150000 && allRentsBelow2000 && $scope.data.relevantRent < 1000) {
                        zeroRate = true;
                    }
                    if ($scope.effDateOnOrAfter(new Date("March 17, 2016"))) {
                        if ( ($scope.data.premium >= 150000) || (!allRentsBelow2000) || (allRentsBelow2000 && $scope.data.contractPre201603 === 'Yes' && $scope.data.contractVariedPost201603 === 'No') ) {
                            prevCalcReqd = true;
                        }
                        result = calculationService.calcLeaseNonResPremAndRent_201603_Undef($scope.data.premium, $scope.data.npv, zeroRate, prevCalcReqd);
                    } else {
                        result = calculationService.calcLeaseNonResPremAndRent_201203_201603($scope.data.premium, $scope.data.npv, zeroRate);
                    }
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
