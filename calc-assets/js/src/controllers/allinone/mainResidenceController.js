(function() {
    "use strict";

    var app = require("../module");

    var mainResidenceController = function($scope, $location, $anchorScroll, dataService, mainResidenceValidationService, navigationService, loggingService) {

        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'main-residence', dataService, mainResidenceValidationService, navigationService);

        $scope.beforeUpdateModel = function() {
            loggingService.logEvent('decision', 'submit', $scope.data.mainResidence);
        };

    };

    app.controller('mainResidenceController', ['$scope', '$location', '$anchorScroll', 'dataService', 'mainResidenceValidationService', 'navigationService', 'loggingService', mainResidenceController ]);
}());
