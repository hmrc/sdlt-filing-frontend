(function() {
    "use strict";

    var app = require("../module");

    var currentValueController = function($scope, $location, $anchorScroll, dataService, currentValueValidationService, navigationService) {

        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'current-value', dataService, currentValueValidationService, navigationService);

    };

    app.controller('currentValueController', ['$scope', '$location', '$anchorScroll', 'dataService', 'currentValueValidationService', 'navigationService', currentValueController ]);
}());
