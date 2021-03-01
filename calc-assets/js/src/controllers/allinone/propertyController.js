(function() {
    "use strict";

    var app = require("../module");

    var propertyController = function($scope, $location, $anchorScroll, dataService, propertyValidationService, navigationService) {
        
        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'property', dataService, propertyValidationService, navigationService);
    };

    app.controller('propertyController', ['$scope', '$location', '$anchorScroll', 'dataService', 'propertyValidationService', 'navigationService', propertyController ]);
}());
