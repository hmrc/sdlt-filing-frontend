(function() {
    "use strict";

    var app = require("../module");

    var nonUKResidentController = function($scope, $location, $anchorScroll, dataService, nonUKResidentValidationService, navigationService, loggingService) {
        
        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'non-uk-resident', dataService, nonUKResidentValidationService, navigationService);

        $scope.beforeUpdateModel = function() {
            loggingService.logEvent('decision', 'submit', $scope.data.nonUKResident);
        };

        $scope.beforeApril21 = Date.now() < new Date('April 1, 2021');

    };

    app.controller('nonUKResidentController', ['$scope', '$location', '$anchorScroll', 'dataService', 'nonUKResidentValidationService', 'navigationService', 'loggingService', nonUKResidentController ]);
}());
