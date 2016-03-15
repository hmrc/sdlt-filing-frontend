(function() {
    "use strict";

    var app = require("../module");

    var summaryController = function($scope, $location, $anchorScroll, dataService, modelValidationService, navigationService, loggingService) {

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

        var summaryHelper = require('../../utilities/summaryHelper.js');
        $scope.data.summary = summaryHelper.summaryHelper($scope, $scope.validatedModel);

        $scope.submit = function() {
            navigationService.next('result', $scope.data, $location);
        };

        $scope.logEvent = loggingService.logEvent;

    };

    app.controller('summaryController', ['$scope', '$location', '$anchorScroll', 'dataService', 'modelValidationService', 'navigationService', 'loggingService', summaryController]);
}());
