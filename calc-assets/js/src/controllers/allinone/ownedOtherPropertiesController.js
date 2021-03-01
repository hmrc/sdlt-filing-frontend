(function() {
    "use strict";

    var app = require("../module");

    var ownedOtherPropertiesController = function($scope, $location, $anchorScroll, dataService, ownedOtherPropertiesValidationService, navigationService) {

        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'owned-other-properties', dataService, ownedOtherPropertiesValidationService, navigationService);

    };

    app.controller('ownedOtherPropertiesController', ['$scope', '$location', '$anchorScroll', 'dataService', 'ownedOtherPropertiesValidationService', 'navigationService', ownedOtherPropertiesController ]);
}());
