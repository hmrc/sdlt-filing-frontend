(function() {
    "use strict";
    
    var app = require("../module");

    var leaseDatesController = function($scope, $location, $anchorScroll, dataService, leaseDatesValidationService, navigationService) {
        
        var dateHelper = require("../../utilities/dateHelper.js");
        var init = require("../../utilities/initController");
        init($scope, $location, $anchorScroll, 'lease-dates', dataService, leaseDatesValidationService, navigationService);

        $scope.updateStartDate = function() {
            $scope.data.startDate = dateHelper.parseUIDate($scope.data.startDateYear, $scope.data.startDateMonth, $scope.data.startDateDay);
        };

        $scope.updateEndDate = function() {
            $scope.data.endDate = dateHelper.parseUIDate($scope.data.endDateYear, $scope.data.endDateMonth, $scope.data.endDateDay);
        };
    };

    app.controller('leaseDatesController', ['$scope', '$location', '$anchorScroll', 'dataService', 'leaseDatesValidationService', 'navigationService', leaseDatesController ]);
}());
