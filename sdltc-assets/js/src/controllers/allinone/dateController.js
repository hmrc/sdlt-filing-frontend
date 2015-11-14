(function() {
    "use strict";
    
    var app = require("../module");

    var dateController = function($scope, $location, $anchorScroll, dataService, dateValidationService, navigationService) {
        
        var init = require("../../utilities/initController");
        init($scope, $location, $anchorScroll, 'date', dataService, dateValidationService, navigationService);

    };

    app.controller('dateController', ['$scope', '$location', '$anchorScroll', 'dataService', 'dateValidationService', 'navigationService', dateController ]);
}());
