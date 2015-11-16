(function() {
    "use strict";

    var app = require("../module");

    var printController = function($scope, $location, dataService, modelValidationService, navigationService) {
        
        var pageName = 'print';
        navigationService.logView(pageName);
        $scope.data = dataService.getModel();

        if (!modelValidationService.validate($scope.data).isModelValid) {
            $location.path('summary');
        }        
    };

    app.controller('printController', ['$scope', '$location', 'dataService', 'modelValidationService', 'navigationService', printController ]);
}());
