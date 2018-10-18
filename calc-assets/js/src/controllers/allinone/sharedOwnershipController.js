(function() {
    "use strict";

    var app = require("../module");

    var sharedOwnershipController = function($scope, $location, $anchorScroll, dataService, sharedOwnershipValidationService, navigationService, loggingService) {

        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'shared-ownership', dataService, sharedOwnershipValidationService, navigationService);

        $scope.beforeUpdateModel = function() {
            loggingService.logEvent('decision', 'submit', $scope.data.sharedOwnership);
        };

    };

    app.controller('sharedOwnershipController', ['$scope', '$location', '$anchorScroll', 'dataService', 'sharedOwnershipValidationService', 'navigationService', 'loggingService', sharedOwnershipController ]);
}());
