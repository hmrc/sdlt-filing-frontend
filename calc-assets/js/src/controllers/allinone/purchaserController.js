(function() {
    "use strict";
    
    var app = require("../module");

    var purchaserController = function($scope, $location, $anchorScroll, dataService, purchaserValidationService, navigationService, loggingService) {
        
        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'purchaser', dataService, purchaserValidationService, navigationService);

        $scope.beforeUpdateModel = function() {
        	loggingService.logEvent('decision', 'submit', $scope.data.purchaserType);
        };

    };

    app.controller('purchaserController', ['$scope', '$location', '$anchorScroll', 'dataService', 'purchaserValidationService', 'navigationService', 'loggingService', purchaserController ]);
}());