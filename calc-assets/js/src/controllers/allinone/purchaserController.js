(function() {
    "use strict";
    
    var app = require("../module");

    var purchaserController = function($scope, $location, $anchorScroll, dataService, purchaserValidationService, navigationService) {
        
        var init = require("../../utilities/initFormController");
        init($scope, $location, $anchorScroll, 'purchaser', dataService, purchaserValidationService, navigationService);
    };

    app.controller('purchaserController', ['$scope', '$location', '$anchorScroll', 'dataService', 'purchaserValidationService', 'navigationService', purchaserController ]);
}());