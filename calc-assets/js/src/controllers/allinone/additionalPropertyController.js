(function() {
    "use strict";

    var app = require("../module");

    var additionalPropertyController = function($scope, $location, $anchorScroll, dataService, additionalPropertyValidationService, navigationService, loggingService) {
        
        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'additional-property', dataService, additionalPropertyValidationService, navigationService);

        $scope.beforeUpdateModel = function() {
            loggingService.logEvent('decision', 'submit', "Two or more properties: "+$scope.data.twoOrMoreProperties);
        };

    };

    app.controller('additionalPropertyController', ['$scope', '$location', '$anchorScroll', 'dataService', 'additionalPropertyValidationService', 'navigationService', 'loggingService', additionalPropertyController ]);
}());
