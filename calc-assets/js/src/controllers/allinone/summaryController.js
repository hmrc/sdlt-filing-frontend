(function() {
    "use strict";

    var app = require("../module");

    var summaryController = function($scope, $location, $anchorScroll, dataService, modelValidationService, navigationService) {

        var validator = require("../../utilities/validator")();

        var init = require("../../utilities/initController");
        init($scope, $location, $anchorScroll, 'summary', dataService, navigationService);

        var rent = require("../../utilities/displayLeasedYearRentFields");
        rent = rent();
        rent.addFunctionsToScope($scope);

        // calculate highest rent
        var highest = parseFloat("0");
        if ($scope.displayYearOneRent && highest < parseFloat($scope.data.year1Rent)) highest = $scope.data.year1Rent;
        if ($scope.displayYearTwoRent && highest < parseFloat($scope.data.year2Rent)) highest = $scope.data.year2Rent;
        if ($scope.displayYearThreeRent && highest < parseFloat($scope.data.year3Rent)) highest = $scope.data.year3Rent;
        if ($scope.displayYearFourRent && highest < parseFloat($scope.data.year4Rent)) highest = $scope.data.year4Rent;
        if ($scope.displayYearFiveRent && highest < parseFloat($scope.data.year5Rent)) highest = $scope.data.year5Rent;
        $scope.data.highestRent = highest;

        // update data service with highest rent
        dataService.updateModel($scope.data);


        $scope.validatedModel = modelValidationService.validate($scope.data);

        $scope.submit = function() {
            navigationService.next('result', $scope.data, $location);
        };

        $scope.displayExchangeContracts = function() {
            var allRentsBelow2000 = validator.checkAllRentsBelow2000($scope.data);
            return ($scope.data.holdingType === 'Leasehold' && 
                $scope.data.propertyType === 'Non-residential' && 
                $scope.data.premium < 150000 && 
                allRentsBelow2000 && 
                validator.isGreaterThanOrEqualToDate($scope.data.effectiveDate, new Date(2016, 2, 17)));
        };

        $scope.displayContractVaried = function() {
            return ($scope.displayExchangeContracts() && $scope.data.contractPre201603 === 'Yes');
        };

        $scope.displayRelevantRent = function() {
            var allRentsBelow2000 = validator.checkAllRentsBelow2000($scope.data);
            var commonChecks = ($scope.data.holdingType === 'Leasehold' && $scope.data.propertyType === 'Non-residential' && $scope.data.premium < 150000 && allRentsBelow2000);

            if (commonChecks && validator.isLessThanDate($scope.data.effectiveDate, new Date(2016, 2, 17))) {
                return true;
            } else if (commonChecks && validator.isGreaterThanOrEqualToDate($scope.data.effectiveDate, new Date(2016, 2, 17)) && $scope.data.contractPre201603 === 'Yes' && $scope.data.contractVariedPost201603 === 'No') {
                return true;
            }
            return false;
        };

        $scope.displayAdditionalProperty = function() {
            return $scope.data.propertyType === "Residential" && validator.isGreaterThanOrEqualToDate($scope.data.effectiveDate, new Date(2016, 3, 1));
        };

        $scope.displayReplaceMainResidence = function() {
            return ($scope.displayAdditionalProperty() && $scope.data.twoOrMoreProperties === 'Yes');
        };

        $scope.getDisplayValue = function(value) {
            if(value === undefined || value === 'undefined' || value === '') {
                return '-';
            }
            else {
                return value;
            }
        };

    };

    app.controller('summaryController', ['$scope', '$location', '$anchorScroll', 'dataService', 'modelValidationService', 'navigationService', summaryController]);
}());
