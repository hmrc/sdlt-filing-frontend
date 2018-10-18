(function() {
    "use strict";

    var app = require("../module");

    var currentValueController = function($scope, $location, $anchorScroll, dataService, currentValueValidationService, navigationService, loggingService) {

        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'current-value', dataService, currentValueValidationService, navigationService);

        $scope.beforeUpdateModel = function() {
            loggingService.logEvent('decision', 'submit', $scope.data.currentValue);
        };

    };

    app.controller('currentValueController', ['$scope', '$location', '$anchorScroll', 'dataService', 'currentValueValidationService', 'navigationService', 'loggingService', currentValueController ]);
}());
