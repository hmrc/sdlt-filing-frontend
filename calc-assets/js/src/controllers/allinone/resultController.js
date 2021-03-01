(function() {
    "use strict";

    var app = require("../module");

    var resultController = function($scope, $location, $anchorScroll, dataService, modelValidationService, navigationService, dataMarshallingService, $http) {

        var init = require("../../utilities/initController");
        init($scope, $location, $anchorScroll, 'result', dataService, navigationService);

        $scope.viewDetails = function(resultIndex, taxCalcIndex) {
            $scope.data.resultIndex = resultIndex;
            $scope.data.taxCalcIndex = taxCalcIndex;
            dataService.updateModel($scope.data);
            navigationService.viewDetails($scope.data, $location);
        };

        $scope.printView = function() {
            navigationService.printView($scope.data, $location);
        };

        var validator = require("../../utilities/validator")();
        $scope.data.result = null;
        $scope.responseReceived = false;
        $scope.errorResponse = false;

        var rent = require("../../utilities/displayLeasedYearRentFields");
        rent = rent().getFunctions($scope.data);
        if (modelValidationService.validate($scope.data).isModelValid) {
          var submission = dataMarshallingService.constructCalculationRequest($scope.data);
          $http.post("/calculate-stamp-duty-land-tax/calculate", submission).
             success(function(data, status, headers, config) {
               $scope.data.result = data.result;
               dataService.updateModel($scope.data);
               $scope.responseReceived = true;
             }).
             error(function(data, status, headers, config) {
               $scope.errorResponse = true;
               $scope.responseReceived = true;
             });
        }
        else {
            $location.path('summary');
        }
    };

    app.controller('resultController', ['$scope', '$location', '$anchorScroll', 'dataService', 'modelValidationService', 'navigationService', 'dataMarshallingService', '$http', resultController ]);
}());
