(function() {
    "use strict";
    
    var app = require("../module");

    var premiumController = function($scope, $location, $anchorScroll, dataService, premiumValidationService, navigationService) {
        
        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'premium', dataService, premiumValidationService, navigationService);

    	if($scope.data.propertyType === 'Residential') {
    		$scope.showPremiumHelp = true;
    	} else if ($scope.data.propertyType === 'Non-residential' && $scope.data.effectiveDate >= new Date(2016,2,17)) {
    		$scope.showPremiumHelp = true;
    	} else {
    		$scope.showPremiumHelp = false;
    	}

    };


    app.controller('premiumController', ['$scope', '$location', '$anchorScroll', 'dataService', 'premiumValidationService', 'navigationService', premiumController ]);
}());
