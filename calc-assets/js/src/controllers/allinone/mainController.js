(function() {
    "use strict";
    var app = require("../module");
    // define the main controller
    var mainController = function($scope, $window, loggingService, cookieService) {

        var expanded = false;

        $scope.jumpTo = function(id) {
            var selector = '#' + id;
            $(selector).focus();
        };

        $scope.getFeedbackSurveyClass = function(){
                  if(onResultPage()){
                    return "feedback-survey--show";
                  }else{
                    return "visually-hidden";
                  }
                };

        function onResultPage() {
            var page = $window.location.href.split("/").slice(-1)[0];
            return page == "result";
        }


        $scope.optionalHelp = {};
        
        $scope.toggleHelp = function(helpId, summary) {
            var currentValue = $scope.optionalHelp[helpId] || false,
                action = currentValue ? 'hide' : 'show';
            
            $scope.optionalHelp[helpId] = !currentValue;
            loggingService.logEvent('help', action, summary);
        };

        $scope.displayHelp = function(helpId) {
            return $scope.optionalHelp[helpId];
        };

        $scope.getHelpGA = function() {
            var action = expanded ? "hide": "show";
            loggingService.logEvent('getHelp', action, "/calculate-stamp-duty-land-tax/"+document.location.href.split('/').pop());
            expanded = !expanded;
        };

    };

    // register the main controller
    app.controller('mainController', ['$scope', '$window', 'loggingService', 'cookieService', mainController]);
}());
