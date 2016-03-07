(function() {
    "use strict";

    var app = require("../module");

    var printController = function($scope, $location, $anchorScroll, dataService, modelValidationService, navigationService) {

        var validator = require("../../utilities/validator")();

        var init = require("../../utilities/initController");
        init($scope, $location, $anchorScroll, 'print', dataService, navigationService);

        if (modelValidationService.validate($scope.data).isModelValid) {
            var rent = require("../../utilities/displayLeasedYearRentFields");
            rent = rent();
            rent.addFunctionsToScope($scope);            
        }   
        else {
            $location.path('summary');
        } 

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

        // $scope.getHeading = function() {
        //     if($scope.effDateAfterCutOff()) {
        //         return "Results based on SDLT rules before 4 December 2014";
        //     } else {
        //         return "Result";
        //     }
        // };
          
    };

    app.controller('printController', ['$scope', '$location', '$anchorScroll', 'dataService', 'modelValidationService', 'navigationService', printController ]);
}());
