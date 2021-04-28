(function() {
    "use strict";

    var app = require("../module");

    var nonUKResidentController = function($scope, $location, $anchorScroll, dataService, nonUKResidentValidationService, navigationService) {
        
        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'non-uk-resident', dataService, nonUKResidentValidationService, navigationService);

    };

    app.controller('nonUKResidentController', ['$scope', '$location', '$anchorScroll', 'dataService', 'nonUKResidentValidationService', 'navigationService', nonUKResidentController ]);
}());
