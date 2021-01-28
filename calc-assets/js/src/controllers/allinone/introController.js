(function() {
    "use strict";

    var app = require("../module");

    var introController = function($scope, $location, $anchorScroll, dataService, navigationService) {

        var init = require("../../utilities/initController");
        init($scope, $location, $anchorScroll, 'intro', dataService, navigationService);

        $scope.startNow = function() {
            dataService.updateModel({});
            navigationService.startNow($location);
        };

        var today = Date.now();

        $scope.beforeApril21 = today < new Date('April 1, 2021');

    };

    app.controller('introController', ['$scope', '$location', '$anchorScroll', 'dataService' , 'navigationService', introController ]);
}());
