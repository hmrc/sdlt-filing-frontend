(function() {
    "use strict";

    var app = require("../module");

    var currentValueController = function($scope, $location, $anchorScroll, dataService, currentValueValidationService, navigationService) {

        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'current-value', dataService, currentValueValidationService, navigationService);

        $scope.ftbLimit = function () {
            var value;
            if($scope.data.effectiveDate >= new Date(2022, 8, 23)) {
                value = "625,000"
            } else {
                value = "500,000"
            }
            return value;
        }

    };

    app.controller('currentValueController', ['$scope', '$location', '$anchorScroll', 'dataService', 'currentValueValidationService', 'navigationService', currentValueController ]);
}());
