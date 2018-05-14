(function() {
    "use strict";
    var app = require("../module");
    // define the main controller
    var mainController = function($scope, loggingService) {

        var expanded = false;

        $scope.jumpTo = function(id) {
            var selector = '#' + id;
            $(selector).focus();
        };

        $scope.getURBannerClass = function() {
            var cookieData = GOVUK.getCookie("mdtpurr");
                if (cookieData == null && onResultPage()){
                    return "banner-panel banner-panel--show";
                } else {
                    return "banner-panel";
                }
        };

        function onResultPage() {
            var page = window.location.href.split("/").slice(-1)[0];
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

        $scope.addFocusToLabel = function() {
            var label = $(this).closest('label')[0];
            $(label).addClass('add-focus selected');
        };

        $scope.removeFocusFromLabel = function() {
            $(this).closest('label').removeClass('add-focus');

            if (!$(this).is(':checked')) {
                $(this).closest('label').removeClass('selected');
            }
        };

        $scope.toggleFocus = function() {
            if ($(this).attr('type') === 'radio') {
                $(this).closest('label').siblings().removeClass('add-focus selected');
            }

            $(this).closest('label').toggleClass('add-focus selected', $(this).prop('checked'));
        };

        // re-apply radio/checkbox styling
        $('#main').on(
            'focus click', 
            'label.block-label input[type=radio], label.block-label input[type=checkbox]',
            $scope.addFocusToLabel
        ).on(
            'change',
            'label.block-label input[type=radio], label.block-label input[type=checkbox]',
            $scope.toggleFocus
        ).on(
            'blur',
            'label.block-label input[type=radio], label.block-label input[type=checkbox]',
            $scope.removeFocusFromLabel
        );

    };

    // register the main controller
    app.controller('mainController', ['$scope', 'loggingService', mainController]);
}());
