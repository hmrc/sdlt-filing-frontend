(function() {
    "use strict";

    var app = require("../module");

    var additionalPropertyController = function($scope, $location, $anchorScroll, dataService, additionalPropertyValidationService, navigationService, loggingService) {
        
        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'additional-property', dataService, additionalPropertyValidationService, navigationService);

        $scope.beforeUpdateModel = function() {
            if($scope.data.twoOrMoreProperties === "No") {
                loggingService.logEvent('decision', 'submit', "AdditonalProperty.SingleProperty");
            } else {
                if($scope.data.replaceMainResidence === "Yes") {
                    loggingService.logEvent('decision', 'submit', "AdditonalProperty.MultiplePoperties.MainResidence");
                } else {
                    loggingService.logEvent('decision', 'submit', "AdditonalProperty.MultiplePoperties.NotMainResidence");
                }
            }
        };

    };

    app.controller('additionalPropertyController', ['$scope', '$location', '$anchorScroll', 'dataService', 'additionalPropertyValidationService', 'navigationService', 'loggingService', additionalPropertyController ]);
}());
