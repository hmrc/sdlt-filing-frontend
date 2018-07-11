(function() {
    "use strict";

    var app = require("../module");

    // define the exitSurvey controller
    var exitSurveyController = function($rootScope, $scope, $location, $anchorScroll, dataService, navigationService, loggingService, messagesService, $http) {

        var init = require("../../utilities/initController");
        init($rootScope, $scope, $location, $anchorScroll, 'exit-Survey', dataService, navigationService, messagesService);

        $scope.submitExitSurvey = function() {
              $http.post("/tax-credits-calculator/submitExitSurvey", $scope.data.survey).
                         error(function(data, status, headers, config) {
                           loggingService.logEvent("error", "survey", "status: "+status+", server: "+headers('Server'));
                         });
        };
    };

    // register the exitSurvey controller
    app.controller('exitSurveyController', ['$rootScope', '$scope', '$location', '$anchorScroll', 'dataService',  'navigationService', 'loggingService', 'messagesService', '$http', exitSurveyController]);
}());