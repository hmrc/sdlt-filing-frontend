(function() {
    "use strict";

    var app = require("../module");

    // define the summary controller
    var summaryController = function($scope, $location, $anchorScroll, dataService, modelValidationService, navigationService) {
        
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
            else if(value === 'yes') {
                return 'Yes';
            }
            else if(value === 'no') {
                return 'No';
            }
            else if(value === 'selfEmployed') {
                return 'Self-employed';
            }
            else if(value === 'notWorking') {
                return 'Not working';
            }
            else if(value === 'working') {
                return 'Working';
            }
            else if(value === 'you') {
                return 'You';
            }
            else if(value === 'partner') {
                return 'Your partner';
            }
            else if(value === 'neither') {
                return 'Neither';
            }
            else {
                return value;
            }
        };

    };

    // register the summary controller
    app.controller('summaryController', ['$scope', '$location', '$anchorScroll', 'dataService', 'modelValidationService', 'navigationService', summaryController]);
}());
