(function() {
    "use strict";
    
    var app = require("../module");

    var holdingController = function($scope, $location, $anchorScroll, dataService, holdingValidationService, navigationService, loggingService) {
        
        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'holding', dataService, holdingValidationService, navigationService);

        $scope.beforeUpdateModel = function() {
        	loggingService.logEvent('decision', 'submit', $scope.data.holdingType);
        };

    };

    app.controller('holdingController', ['$scope', '$location', '$anchorScroll', 'dataService', 'holdingValidationService', 'navigationService', 'loggingService', holdingController ]);
}());
