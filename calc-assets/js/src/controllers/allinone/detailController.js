(function() {
    "use strict";

    var app = require("../module");

    var detailController = function($scope, $location, $anchorScroll, dataService, modelValidationService, navigationService) {

        var init = require("../../utilities/initController");
        init($scope, $location, $anchorScroll, 'detail', dataService, navigationService);

        if (!modelValidationService.validate($scope.data).isModelValid) {
            $location.path('summary');
        }

        $scope.printView = function() {
            navigationService.printView($scope.data, $location);
        };

    };

    app.controller('detailController', ['$scope', '$location', '$anchorScroll', 'dataService', 'modelValidationService', 'navigationService', detailController ]);
}());
