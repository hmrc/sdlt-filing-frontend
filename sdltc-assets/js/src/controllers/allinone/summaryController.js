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

        $scope.getDisplayValue = function(value) {
            if(value === undefined || value === 'undefined' || value === '') {
                return '-';
            }
            else {
                return value;
            }
        };

    };

    app.controller('summaryController', ['$scope', '$location', 'dataService', 'modelValidationService', 'navigationService', summaryController]);
}());
