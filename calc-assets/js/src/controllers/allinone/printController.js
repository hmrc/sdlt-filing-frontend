(function() {
    "use strict";

    var app = require("../module");

    var printController = function($scope, $location, $anchorScroll, dataService, modelValidationService, navigationService) {

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
        $scope.effDateAfterCutOff = function(){
            var cutOffDate = new Date("December 4, 2014");
            return $scope.data.effectiveDate >= cutOffDate;
        };

        $scope.effDateAfterAprilCutOff = function(){
            var cutOffDate = new Date("April 1, 2016");
            return $scope.data.effectiveDate >= cutOffDate;
        };

        $scope.isAdditionalProperty = function(){
            return ($scope.data.twoOrMoreProperties === 'Yes' && $scope.data.replaceMainResidence === 'No') && $scope.effDateAfterAprilCutOff();
        };

        $scope.getHeading = function() {
            if($scope.effDateAfterCutOff()) {
                return "Results based on SDLT rules before 4 December 2014";
            } else {
                return "Result";
            }
        };
          
    };

    app.controller('printController', ['$scope', '$location', '$anchorScroll', 'dataService', 'modelValidationService', 'navigationService', printController ]);
}());
