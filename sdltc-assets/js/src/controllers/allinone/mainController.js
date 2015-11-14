(function() {
    "use strict";
    var app = require("../module");
    // define the main controller
    var mainController = function($scope) {

        $scope.jumpTo = function(id) {
            var selector = '#' + id;
            $(selector).focus();
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
    app.controller('mainController', ['$scope', mainController]);
}());
