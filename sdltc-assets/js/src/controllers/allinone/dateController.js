(function() {
    "use strict";
    
    var app = require("../module");

    var dateController = function($scope, $location, $anchorScroll, dataService, dateValidationService, navigationService) {
        
        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'date', dataService, dateValidationService, navigationService);

        var dateHelper = require("../../utilities/dateHelper.js");
        $scope.updateEffectiveDate = function() {
            $scope.data.effectiveDate = dateHelper.parseUIDate($scope.data.effectiveDateYear, $scope.data.effectiveDateMonth, $scope.data.effectiveDateDay);
        };
    };

    app.controller('dateController', ['$scope', '$location', '$anchorScroll', 'dataService', 'dateValidationService', 'navigationService', dateController ]);
}());
