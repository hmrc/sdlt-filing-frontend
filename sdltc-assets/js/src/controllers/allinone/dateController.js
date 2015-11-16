(function() {
    "use strict";
    
    var app = require("../module");

    var dateController = function($scope, $location, $anchorScroll, dataService, dateValidationService, navigationService) {
        
        var init = require("../../utilities/initController");
        init($scope, $location, $anchorScroll, 'date', dataService, dateValidationService, navigationService);

        $scope.updateEffectiveDate = function() {

            var dateString = $scope.data.effectiveDateYear + $scope.data.effectiveDateMonth + $scope.data.effectiveDateDay;

            if (dateString === '') {
                $scope.data.effectiveDate = '';
            } else {
                // this can result in an invalid date but it's OK as data will only be persisted if validation passes
                var date = new Date(
                    $scope.data.effectiveDateYear, 
                    $scope.data.effectiveDateMonth - 1, // month is zero indexed
                    $scope.data.effectiveDateDay);

                $scope.data.effectiveDate = isNaN(date) ? 'bad date' : date;
            }
        };
    };

    app.controller('dateController', ['$scope', '$location', '$anchorScroll', 'dataService', 'dateValidationService', 'navigationService', dateController ]);
}());
