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

    };

    app.controller('summaryController', ['$scope', '$location', 'dataService', 'modelValidationService', 'navigationService', summaryController]);
}());
