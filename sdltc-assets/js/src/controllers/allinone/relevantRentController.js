(function() {
    "use strict";
    
    var app = require("../module");

    var relevantRentController = function($scope, $location, $anchorScroll, dataService, relevantRentValidationService, navigationService) {
        
        var init = require("../../utilities/initController");
        init($scope, $location, $anchorScroll, 'relevant-rent', dataService, relevantRentValidationService, navigationService);

    };

    app.controller('relevantRentController', ['$scope', '$location', '$anchorScroll', 'dataService', 'relevantRentValidationService', 'navigationService', relevantRentController ]);
}());
