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

        // re-apply radio/checkbox styling
        $('#main').on(
            'focus click', 
            'label.block-label input[type=radio], label.block-label input[type=checkbox]',
            function() {
                var label = $(this).closest('label')[0];
                $(label).addClass('add-focus selected');
            }
        ).on(
            'change',
            'label.block-label input[type=radio], label.block-label input[type=checkbox]',
            function() {
                if ($(this).attr('type') === 'radio') {
                    $(this).closest('label').siblings().removeClass('add-focus selected');
                }

                $(this).closest('label').toggleClass('add-focus selected', $(this).prop('checked'));
            }
        ).on(
            'blur',
            'label.block-label input[type=radio], label.block-label input[type=checkbox]',
            function() {
                $(this).closest('label').removeClass('add-focus');

                if (!$(this).is(':checked')) {
                    $(this).closest('label').removeClass('selected');
                }
            }
        );

    };

    // register the main controller
    app.controller('mainController', ['$scope', 'loggingService', mainController]);
}());
