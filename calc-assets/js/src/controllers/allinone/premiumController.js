(function() {
    "use strict";
    
    var app = require("../module");

    var premiumController = function($scope, $location, $anchorScroll, dataService, premiumValidationService, navigationService) {
        
        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'premium', dataService, premiumValidationService, navigationService);

    };

    app.controller('premiumController', ['$scope', '$location', '$anchorScroll', 'dataService', 'premiumValidationService', 'navigationService', premiumController ]);
}());
