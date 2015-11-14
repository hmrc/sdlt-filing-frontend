(function() {
    "use strict";
    
    var app = require("../module");

    var holdingController = function($scope, $location, $anchorScroll, dataService, holdingValidationService, navigationService) {
        
        var init = require("../../utilities/initController");
        init($scope, $location, $anchorScroll, 'holding', dataService, holdingValidationService, navigationService);

    };

    app.controller('holdingController', ['$scope', '$location', '$anchorScroll', 'dataService', 'holdingValidationService', 'navigationService', holdingController ]);
}());
