(function() {
    "use strict";

    var app = require("../module");

    // define the exitSurvey controller
    var exitSurveyController = function($scope, $location, $anchorScroll, dataService, navigationService, loggingService, $http) {

        var init = require("../../utilities/initController");
        init($scope, $location, $anchorScroll, 'exit-Survey', dataService, navigationService);

        $scope.submitExitSurvey = function() {
              $http.post("/calculate-stamp-duty-land-tax/submitExitSurvey", $scope.data.survey).
                         error(function(data, status, headers, config) {
                           loggingService.logEvent("error", "survey", "status: "+status+", server: "+headers('Server'));
                         });
        };
    };

    // register the exitSurvey controller
    app.controller('exitSurveyController', ['$scope', '$location', '$anchorScroll', 'dataService',  'navigationService', 'loggingService', '$http', exitSurveyController]);
}());