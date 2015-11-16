(function() {
    "use strict";

    var app = require("../module");

    var summaryController = function($scope, $location, dataService, modelValidationService, navigationService) {
        
        var pageName = 'summary';
        navigationService.logView(pageName);
        $scope.data = dataService.getModel();

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

    };

    app.controller('summaryController', ['$scope', '$location', 'dataService', 'modelValidationService', 'navigationService', summaryController]);
}());
