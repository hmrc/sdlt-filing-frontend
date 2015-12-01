(function() {
    "use strict";
    
    var app = require("../module");

    var rentController = function($scope, $location, $anchorScroll, dataService, rentValidationService, navigationService) {
        
        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'rent', dataService, rentValidationService, navigationService);

        var rent = require("../../utilities/displayLeasedYearRentFields");
        rent = rent();
        rent.addFunctionsToScope($scope);

        // if they have missed required questions redirect to summary
        if(!$scope.displayYearOneRent) {
            $location.path('summary');
        }

    };

    app.controller('rentController', ['$scope', '$location', '$anchorScroll', 'dataService', 'rentValidationService', 'navigationService', rentController ]);
}());
