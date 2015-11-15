(function() {
    "use strict";

    var app = require("../module");

    var resultController = function($scope, $location, dataService, modelValidationService, navigationService, calculationService) {
        
        var pageName = 'result';
        navigationService.logView(pageName);
        $scope.data = dataService.getModel();

        $scope.viewDetails = function() {
            navigationService.viewDetails($scope.data, $location);
        };

        if (modelValidationService.validate($scope.data).isModelValid) {
            $scope.result = {};
            if ($scope.data.holdingType === 'Freehold') {
                if ($scope.data.propertyType === 'Residential') {
                    $scope.result.from = calculationService.calculateResidentialPremiumSlice($scope.data.premium);
                    $scope.result.before = calculationService.calculateResidentialPremiumSlab($scope.data.premium);                    
                }
                else if ($scope.data.propertyType === 'Non-residential'){
                    $scope.result = calculationService.calculateNonResidentialPremiumSlab($scope.data.premium, 0); 
                }
            }
            else if ($scope.data.holdingType === 'Leasehold') {
                 if ($scope.data.propertyType === 'Residential') {
                    $scope.result = calculationService.calculateResidentialLeaseSlab(1000000, 2000);                    
                }
                else if ($scope.data.propertyType === 'Non-residential'){
                    $scope.result = calculationService.calculateNonResidentialLeaseSlab(1000000, 2000); 
                }
            }
            
        }
        else {
            $location.path('summary');
        }

    };

    app.controller('resultController', ['$scope', '$location', 'dataService', 'modelValidationService', 'navigationService', 'calculationService', resultController ]);
}());
