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
        var rentFields = require("../../utilities/displayLeasedYearRentFields");
        $scope.beforeUpdateModel = function() {
            $scope.data.leaseTerm = dateHelper.calculateTermOfLease($scope.data.effectiveDate, $scope.data.startDate, $scope.data.endDate);
            var yearsToDisplay = rentFields().getFunctions($scope.data);
            setDisplayYears(yearsToDisplay);

        };

        function setDisplayYears(yearsToDisplay) {

            if(!yearsToDisplay.displayYearOneRent){
                $scope.data.year1Rent = undefined;
            }
            if(!yearsToDisplay.displayYearTwoRent){
                $scope.data.year2Rent = undefined;
            }
            if(!yearsToDisplay.displayYearThreeRent){
                $scope.data.year3Rent = undefined;
            }
            if(!yearsToDisplay.displayYearFourRent){
                $scope.data.year4Rent = undefined;
            }
            if(!yearsToDisplay.displayYearFiveRent){
                $scope.data.year5Rent = undefined;
            }
        }
        
    };

    app.controller('leaseDatesController', ['$scope', '$location', '$anchorScroll', 'dataService', 'leaseDatesValidationService', 'navigationService', leaseDatesController ]);
}());
