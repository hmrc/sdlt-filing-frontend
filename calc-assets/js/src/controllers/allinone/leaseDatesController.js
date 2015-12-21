(function() {
    "use strict";
    
    var app = require("../module");

    var leaseDatesController = function($scope, $location, $anchorScroll, dataService, leaseDatesValidationService, navigationService) {

        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'lease-dates', dataService, leaseDatesValidationService, navigationService);

        var dateHelper = require("../../utilities/dateHelper.js");
        $scope.updateStartDate = function() {
            $scope.data.startDate = dateHelper.parseUIDate($scope.data.startDateYear, $scope.data.startDateMonth, $scope.data.startDateDay);
        };
        $scope.updateEndDate = function() {
            $scope.data.endDate = dateHelper.parseUIDate($scope.data.endDateYear, $scope.data.endDateMonth, $scope.data.endDateDay);
        };
        $scope.beforeUpdateModel = function() {
            $scope.data.leaseTerm = dateHelper.calculateTermOfLease($scope.data.effectiveDate, $scope.data.startDate, $scope.data.endDate);
        };
        
    };

    app.controller('leaseDatesController', ['$scope', '$location', '$anchorScroll', 'dataService', 'leaseDatesValidationService', 'navigationService', leaseDatesController ]);
}());
