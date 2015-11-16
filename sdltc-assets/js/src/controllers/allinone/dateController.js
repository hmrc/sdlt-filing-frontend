(function() {
    "use strict";
    
    var app = require("../module");

    var dateController = function($scope, $location, $anchorScroll, dataService, dateValidationService, navigationService) {
        
        var dateHelper = require("../../utilities/dateHelper.js");
        var init = require("../../utilities/initController");
        init($scope, $location, $anchorScroll, 'date', dataService, dateValidationService, navigationService);

        $scope.updateEffectiveDate = function() {
            $scope.data.effectiveDate = dateHelper.parseUIDate($scope.data.effectiveDateYear, $scope.data.effectiveDateMonth, $scope.data.effectiveDateDay);
        };
    };

    app.controller('dateController', ['$scope', '$location', '$anchorScroll', 'dataService', 'dateValidationService', 'navigationService', dateController ]);
}());
