(function() {
    "use strict";

    var app = require("../module");

    var resultController = function($scope, $location, $anchorScroll, dataService, modelValidationService, navigationService, calculationService) {
        
        var pageName = 'result';
        navigationService.logView(pageName);
        $scope.data = dataService.getModel();

        if (modelValidationService.validate($scope.data).isModelValid) {
            $scope.result = {};
            if ($scope.data.holdingType === 'Freehold') {
                if ($scope.data.propertyType === 'Residential') {
                    $scope.result = calculationService.calculateResidentialPremiumSlab($scope.data.premium);
                    $scope.result.fromTaxDue = calculationService.calculateResidentialPremiumSlice($scope.data.premium).totalSDLT;
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

    app.controller('resultController', ['$scope', '$location', '$anchorScroll', 'dataService', 'modelValidationService', 'navigationService', 'calculationService', resultController ]);
}());
