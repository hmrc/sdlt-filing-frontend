(function() {
    "use strict";

    var app = require("../module");

    var marketValueController = function($scope, $location, $anchorScroll, dataService, marketValueValidationService, navigationService, loggingService) {

        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'market-value', dataService, marketValueValidationService, navigationService);

        $scope.beforeUpdateModel = function() {
                if($scope.data.paySDLT === "Upfront" && $scope.data.marketPropValue) {
                    loggingService.logEvent('decision', 'submit', "MarketValue.UpfrontMarketValue");
                }

                if($scope.data.paySDLT === "Stages" && $scope.data.stagePropValue) {
                    loggingService.logEvent('decision', 'submit', "MarketValue.StagesShareValue");
                }

            };
        };

    app.controller('marketValueController', ['$scope', '$location', '$anchorScroll', 'dataService', 'marketValueValidationService', 'navigationService', 'loggingService', marketValueController ]);
}());
