(function() {
    "use strict";

    var app = require("../module");

    var detailController = function($scope, $location, $anchorScroll, dataService, modelValidationService, navigationService) {

        var validator = require("../../utilities/validator")();

        var init = require("../../utilities/initController");
        init($scope, $location, $anchorScroll, 'detail', dataService, navigationService);

        if (!modelValidationService.validate($scope.data).isModelValid) {
            $location.path('summary');
        }

        $scope.printView = function() {
            navigationService.printView($scope.data, $location);
        };

        $scope.isAdditionalProperty = function() {
            return $scope.data.propertyType === "Residential" &&
                validator.isGreaterThanOrEqualToDate($scope.data.effectiveDate, new Date(2016, 3, 1)) && // = 01/04/2016 !
                $scope.data.twoOrMoreProperties == "Yes" &&
                $scope.data.replaceMainResidence == "No";
        };
    };

    app.controller('detailController', ['$scope', '$location', '$anchorScroll', 'dataService', 'modelValidationService', 'navigationService', detailController ]);
}());
