(function() {
    "use strict";

    var app = require("../module");

    var mainResidenceController = function($scope, $location, $anchorScroll, dataService, mainResidenceValidationService, navigationService) {

        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'main-residence', dataService, mainResidenceValidationService, navigationService);
    };

    app.controller('mainResidenceController', ['$scope', '$location', '$anchorScroll', 'dataService', 'mainResidenceValidationService', 'navigationService', mainResidenceController ]);
}());
