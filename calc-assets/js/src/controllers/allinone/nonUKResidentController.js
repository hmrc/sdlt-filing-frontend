(function() {
    "use strict";

    var app = require("../module");

    var nonUKResidentController = function($scope, $location, $anchorScroll, dataService, nonUKResidentValidationService, navigationService, loggingService) {
        
        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'non-uk-resident', dataService, nonUKResidentValidationService, navigationService);

        $scope.beforeUpdateModel = function() {
            if($scope.data.nonUKResident === "No") {
                loggingService.logEvent('decision', 'submit', "NonUKResident.UKResident");
            } else {
                loggingService.logEvent('decision', 'submit', "NonUKResident.NonUKResident");
            }
        };

        $scope.beforeApril21 = Date.now() < new Date('April 1, 2021');

    };

    app.controller('nonUKResidentController', ['$scope', '$location', '$anchorScroll', 'dataService', 'nonUKResidentValidationService', 'navigationService', 'loggingService', nonUKResidentController ]);
}());
