(function() {
    "use strict";
    
    var app = require("../module");

    var rentController = function($scope, $location, $anchorScroll, dataService, rentValidationService, navigationService) {
        
        var init = require("../../utilities/initController");
        init($scope, $location, $anchorScroll, 'rent', dataService, rentValidationService, navigationService);

    };

    app.controller('rentController', ['$scope', '$location', '$anchorScroll', 'dataService', 'rentValidationService', 'navigationService', rentController ]);
}());
