(function() {
    "use strict";

    var app = require("../module");

    var exchangeContractsController = function($scope, $location, $anchorScroll, dataService, exchangeContractsValidationService, navigationService, loggingService) {
        
        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'exchange-contracts', dataService, exchangeContractsValidationService, navigationService);

        $scope.beforeUpdateModel = function() {
            if($scope.data.contractPre201603 === "No") {
                loggingService.logEvent('decision', 'submit', "ExchangeContracts.Post20160316");
            } else {
                if($scope.data.contractVariedPost201603 === "Yes") {
                    loggingService.logEvent('decision', 'submit', "ExchangeContracts.Pre20160317.VariedAfter");
                } else {
                    loggingService.logEvent('decision', 'submit', "ExchangeContracts.Pre20160317.NotVariedAfter");
                }
            }
        };

    };

    app.controller('exchangeContractsController', ['$scope', '$location', '$anchorScroll', 'dataService', 'exchangeContractsValidationService', 'navigationService', 'loggingService', exchangeContractsController ]);
}());
