(function() {
    "use strict";

    var app = require("../module");

    var printController = function($scope, $location, $anchorScroll, dataService, modelValidationService, navigationService) {

        var init = require("../../utilities/initController");
        init($scope, $location, $anchorScroll, 'print', dataService, navigationService);

        if (modelValidationService.validate($scope.data).isModelValid) {
            var rent = require("../../utilities/displayLeasedYearRentFields");
            rent = rent();
            rent.addFunctionsToScope($scope);            
        }   
        else {
            $location.path('summary');
        } 
          
    };

    app.controller('printController', ['$scope', '$location', '$anchorScroll', 'dataService', 'modelValidationService', 'navigationService', printController ]);
}());
