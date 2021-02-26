(function() {
    "use strict";

    var app = require("../module");

    var additionalPropertyController = function($scope, $location, $anchorScroll, dataService, additionalPropertyValidationService, navigationService) {
        
        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'additional-property', dataService, additionalPropertyValidationService, navigationService);

    };

    app.controller('additionalPropertyController', ['$scope', '$location', '$anchorScroll', 'dataService', 'additionalPropertyValidationService', 'navigationService', additionalPropertyController ]);
}());
