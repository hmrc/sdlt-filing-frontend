(function() {
    "use strict";

    var app = require("../module");

    var summaryController = function($scope, $location, dataService, modelValidationService, navigationService) {
        
        var pageName = 'summary';
        navigationService.logView(pageName);
        $scope.data = dataService.getModel();

        var rent = require("../../utilities/displayLeasedYearRentFields");
        rent = rent();
        rent.addFunctionsToScope($scope);

        $scope.validatedModel = modelValidationService.validate($scope.data);

        $scope.submit = function() {
            navigationService.next('result', $scope.data, $location);
        };

        $scope.getDisplayValue = function(value) {
            if(value === undefined || value === 'undefined' || value === '') {
                return '-';
            }
            else {
                return value;
            }
        };

        $scope.getTermOfLeaseDisplayValue = function (years, days) {
            if(years === undefined || years === 'undefined' || years === '' || days === undefined || days === 'undefined' || days === '') {
                return '-';
            }

            var result = "";

            if(years == 1) {
                result = years + " year ";
            }
            else if(years > 1) {
                result = years + " years ";
            }

            if(days == 1) {
                result = result + days + " day";
            }
            else if(days > 1) {
                result = result + days + " days";
            }

            return result;
        };

        $scope.data.highestRent = function() {
            // set unused rent fields to 0
            if (!$scope.displayYearOneRent) $scope.data.year1Rent = 0;
            if (!$scope.displayYearTwoRent) $scope.data.year2Rent = 0;
            if (!$scope.displayYearThreeRent) $scope.data.year3Rent = 0;
            if (!$scope.displayYearFourRent) $scope.data.year4Rent = 0;
            if (!$scope.displayYearFiveRent) $scope.data.year5Rent = 0;

            // calculate highest rent
            var highest = 0;
            highest = highest > $scope.data.year1Rent ? highest : $scope.data.year1Rent;
            highest = highest > $scope.data.year2Rent ? highest : $scope.data.year2Rent;
            highest = highest > $scope.data.year3Rent ? highest : $scope.data.year3Rent;
            highest = highest > $scope.data.year4Rent ? highest : $scope.data.year4Rent;
            highest = highest > $scope.data.year5Rent ? highest : $scope.data.year5Rent;

            return highest;
       };

    };

    app.controller('summaryController', ['$scope', '$location', 'dataService', 'modelValidationService', 'navigationService', summaryController]);
}());
