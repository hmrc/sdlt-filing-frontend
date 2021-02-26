(function() {
    "use strict";

    var app = require("../module");

    var marketValueController = function($scope, $location, $anchorScroll, dataService, marketValueValidationService, navigationService) {

        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'market-value', dataService, marketValueValidationService, navigationService);
    };

    app.controller('marketValueController', ['$scope', '$location', '$anchorScroll', 'dataService', 'marketValueValidationService', 'navigationService', marketValueController ]);
}());
