(function() {
    "use strict";

    var app = require("../module");

    var sharedOwnershipController = function($scope, $location, $anchorScroll, dataService, sharedOwnershipValidationService, navigationService) {

        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'shared-ownership', dataService, sharedOwnershipValidationService, navigationService);

    };

    app.controller('sharedOwnershipController', ['$scope', '$location', '$anchorScroll', 'dataService', 'sharedOwnershipValidationService', 'navigationService', sharedOwnershipController ]);
}());
