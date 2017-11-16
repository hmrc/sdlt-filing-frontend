(function() {
    "use strict";

    var app = require("../module");

    var ownedOtherPropertiesController = function($scope, $location, $anchorScroll, dataService, ownedOtherPropertiesValidationService, navigationService, loggingService) {

        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'owned-other-properties', dataService, ownedOtherPropertiesValidationService, navigationService);

        $scope.beforeUpdateModel = function() {
            loggingService.logEvent('decision', 'submit', $scope.data.ownedOtherProperties);
        };

    };

    app.controller('ownedOtherPropertiesController', ['$scope', '$location', '$anchorScroll', 'dataService', 'ownedOtherPropertiesValidationService', 'navigationService', 'loggingService', ownedOtherPropertiesController ]);
}());
