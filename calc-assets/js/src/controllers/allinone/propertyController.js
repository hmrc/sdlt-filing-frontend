(function() {
    "use strict";
    
    var app = require("../module");

    var propertyController = function($scope, $location, $anchorScroll, dataService, propertyValidationService, navigationService, loggingService) {
        
        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'property', dataService, propertyValidationService, navigationService);

        $scope.beforeUpdateModel = function() {
            loggingService.logEvent('decision', 'submit', $scope.data.holdingType + '.' + $scope.data.propertyType);
        };
    };

    app.controller('propertyController', ['$scope', '$location', '$anchorScroll', 'dataService', 'propertyValidationService', 'navigationService', 'loggingService', propertyController ]);
}());
