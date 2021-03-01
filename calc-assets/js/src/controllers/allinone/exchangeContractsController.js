(function() {
    "use strict";

    var app = require("../module");

    var exchangeContractsController = function($scope, $location, $anchorScroll, dataService, exchangeContractsValidationService, navigationService) {
        
        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'exchange-contracts', dataService, exchangeContractsValidationService, navigationService);

    };

    app.controller('exchangeContractsController', ['$scope', '$location', '$anchorScroll', 'dataService', 'exchangeContractsValidationService', 'navigationService', exchangeContractsController ]);
}());
