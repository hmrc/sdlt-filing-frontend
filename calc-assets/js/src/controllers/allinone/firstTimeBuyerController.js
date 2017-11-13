(function() {
    "use strict";

    var app = require("../module");

    var propertyController = function($scope, $location, $anchorScroll, dataService, firstTimeBuyerValidationService, navigationService, loggingService) {

        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'first-time-buyer', dataService, firstTimeBuyerValidationService, navigationService);

        $scope.beforeUpdateModel = function() {
            loggingService.logEvent('decision', 'submit', $scope.data.firstTimeBuyer);
        };

    };

    app.controller('firstTimeBuyerController', ['$scope', '$location', '$anchorScroll', 'dataService', 'firstTimeBuyerValidationService', 'navigationService', 'loggingService', propertyController ]);
}());
