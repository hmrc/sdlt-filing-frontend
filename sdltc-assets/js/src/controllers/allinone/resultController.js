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
            var result = {
                freehold  : {
                    resdential : {
                        from : {},
                        before : {}
                    },
                    nonResdential : {}
                },
                leasehold  : {
                    resdential : {},
                    nonResdential : {}
                }
            };
            if ($scope.data.holdingType === 'Freehold') {
                if ($scope.data.propertyType === 'Residential') {
                    result.freehold.resdential.from = calculationService.calculateResidentialPremiumSlice($scope.data.premium);
                    result.freehold.resdential.before = calculationService.calculateResidentialPremiumSlab($scope.data.premium);                    
                }
                else if ($scope.data.propertyType === 'Non-residential'){
                    result.freehold.nonResdential = calculationService.calculateNonResidentialPremiumSlab($scope.data.premium, 0); 
                }
            }
            else if ($scope.data.holdingType === 'Leasehold') {
                 if ($scope.data.propertyType === 'Residential') {
                    result.leasehold.resdential = calculationService.calculateResidentialLeaseSlab(1000000, 2000);                    
                }
                else if ($scope.data.propertyType === 'Non-residential'){
                    result.leasehold.nonResdential = calculationService.calculateNonResidentialLeaseSlab(1000000, 2000); 
                }
            }
            $scope.data.result = result;
            dataService.updateModel($scope.data);
        }
        else {
            $location.path('summary');
        }

    };

    app.controller('resultController', ['$scope', '$location', 'dataService', 'modelValidationService', 'navigationService', 'calculationService', resultController ]);
}());
