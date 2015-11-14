(function() {
    "use strict";

    var app = require("../module");

    // define the result controller
    var resultController = function($scope, $location, $anchorScroll, dataService, modelValidationService, navigationService, transformationService, calculationService) {
        
        var pageName = 'result';
        navigationService.logView(pageName);
        $scope.data = dataService.getModel();

        if(modelValidationService.validate($scope.data).isModelValid) {
            var transformData = transformationService.transform($scope.data);
            $scope.result = calculationService.calculateTaxCredits(transformData);
        }
        else {
            $location.path('summary');
        }

    };

    // register the result controller
    app.controller('resultController', ['$scope', '$location', '$anchorScroll', 'dataService', 'modelValidationService', 'navigationService', 'transformationService', 'calculationService', resultController ]);
}());
