(function() {
    "use strict";
    
    var app = require("../module");

    var rentController = function($scope, $location, $anchorScroll, dataService, rentValidationService, navigationService) {
        
        var init = require("../../utilities/initController");
        init($scope, $location, $anchorScroll, 'rent', dataService, rentValidationService, navigationService);

        var rent = require("../../utilities/displayLeasedYearRentFields");
        rent = rent();
        rent.addFunctionsToScope($scope);

        // if they have missed required questions redirect to summary
        if(!$scope.displayYearOneRent) {
            $location.path('summary');
        }

        $scope.data.highestRent = function() {
        	var highest = 0;

        	highest = highest > $scope.data.year1Rent ? highest : $scope.data.year1Rent;
        	highest = highest > $scope.data.year2Rent ? highest : $scope.data.year2Rent;
        	highest = highest > $scope.data.year3Rent ? highest : $scope.data.year3Rent;
        	highest = highest > $scope.data.year4Rent ? highest : $scope.data.year4Rent;
        	highest = highest > $scope.data.year5Rent ? highest : $scope.data.year5Rent;

       	return highest;
       };

    };

    app.controller('rentController', ['$scope', '$location', '$anchorScroll', 'dataService', 'rentValidationService', 'navigationService', rentController ]);
}());
