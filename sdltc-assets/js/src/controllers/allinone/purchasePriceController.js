(function() {
    "use strict";
    
    var app = require("../module");

    var purchasePriceController = function($scope, $location, $anchorScroll, dataService, purchasePriceValidationService, navigationService) {
        
        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'purchase-price', dataService, purchasePriceValidationService, navigationService);

    };

    app.controller('purchasePriceController', ['$scope', '$location', '$anchorScroll', 'dataService', 'purchasePriceValidationService', 'navigationService', purchasePriceController ]);
}());
