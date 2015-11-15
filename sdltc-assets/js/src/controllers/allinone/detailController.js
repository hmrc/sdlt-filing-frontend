(function() {
    "use strict";

    var app = require("../module");

    var detailController = function($scope, $location, dataService, modelValidationService, navigationService) {
        
        var pageName = 'detail';
        navigationService.logView(pageName);
        $scope.data = dataService.getModel();

        if (!modelValidationService.validate($scope.data).isModelValid) {
            $location.path('summary');
        }        
    };

    app.controller('detailController', ['$scope', '$location', 'dataService', 'modelValidationService', 'navigationService', detailController ]);
}());
