(function() {
    "use strict";

    var app = require("../module");

    var introController = function($scope, $location, dataService, navigationService) {
        
        var pageName = 'intro';
        navigationService.logView(pageName);

        $scope.startNow = function() {
            dataService.updateModel({});
            navigationService.startNow($location);
        };
    };

    app.controller('introController', ['$scope', '$location', 'dataService' , 'navigationService', introController ]);
}());
