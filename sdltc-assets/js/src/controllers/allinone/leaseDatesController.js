(function() {
    "use strict";
    
    var app = require("../module");

    var leaseDatesController = function($scope, $location, $anchorScroll, dataService, leaseDatesValidationService, navigationService) {
        
        var init = require("../../utilities/initController");
        init($scope, $location, $anchorScroll, 'lease-dates', dataService, leaseDatesValidationService, navigationService);

    };

    app.controller('leaseDatesController', ['$scope', '$location', '$anchorScroll', 'dataService', 'leaseDatesValidationService', 'navigationService', leaseDatesController ]);
}());
