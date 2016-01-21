(function() {
    "use strict";
    var app = require("../module");
    // define the main controller
    var mainController = function($scope, loggingService) {

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



        $scope.getHelpSetup = function(referrer) {

              //var feedbackForms = require('./feedbackForms.js');
            var feedbackFormsSetup = function() {
                
                var $feedbackForms = $('.form--feedback');

                //we have javascript enabled so change hidden input to reflect this
                $feedbackForms.find('input[name=isJavascript]').attr('value', true);

            };
          
              var showErrorMessage = function() {
                var response = '<p>There was a problem sending your query.</p>' +
                  '<p>Please try again later or email ' +
                  '<a href="mailto:hmrcsupport@tax.service.gov.uk">hmrcsupport@tax.service.gov.uk</a> ' +
                  'if you need technical help with this website.</p>';
                reportErrorContainer().html(response);
                enableSubmitButton();
              },

              reportErrorContainer = function() {
                return $('.report-error__content');
              },

              submitButton = function() {
                return reportErrorContainer().find('.button');
              },

              //TODO: should refactor to use Javascript debounce
              disableSubmitButton = function() {
                submitButton().prop('disabled', true);
              },

              enableSubmitButton = function() {
                submitButton().prop('disabled', false);
              },

              showConfirmation = function(data) {
                reportErrorContainer().html(data.message);
              },

              submit = function(form, url) {
                $.ajax({
                  type: 'POST',
                  url: url,
                  data: $(form).serialize(),
                  beforeSend: function(xhr) {
                    disableSubmitButton();
                    xhr.setRequestHeader('Csrf-Token', 'nocheck');
                  },

                  success: function(data) {
                    showConfirmation(data);
                  },

                  error: function(jqXHR, status) {
                    if (status === 'error' || !jqXHR.responseText) {
                      showErrorMessage();
                    }
                  }
                });
              },

              load = function(url) {
                var $formContainer = $('#report-error-partial-form');
                $formContainer.load(referrer, function( response, status, xhr ) {
                  setupFormValidation();
                  feedbackFormsSetup();
                });
              },

              configureToggle = function() {
                var reportErrorToggle = $('.report-error__toggle');
                
                reportErrorToggle.on('click', function(e) {
                  var $errorContent = $('.report-error__content');
                  if($errorContent.has('form').length === 0) {
                    // show the spinner
                    $errorContent.removeClass('hidden');
                    $errorContent.removeClass('js-hidden');
                    // the form or the form's submission result is not there, load the HTML asynchronously using Ajax
                    // and replace the spinner with the form markup
                    load(decodeURIComponent(referrer));
                  } else {
                    $errorContent.toggleClass('js-hidden');
                  }

                  // Preventing navigation ONLY if element has "href" attribute
                  if (reportErrorToggle.attr("href"))
                    e.preventDefault();
                });
              },

              setupFormValidation = function() {
                var $errorReportForm = $('.report-error__content form');
               
                if($errorReportForm) {
                  //Initialise validation for the feedback form
                  $errorReportForm.validate({
                    errorClass: 'error-notification',
                    errorPlacement: function(error, element) {
                      error.insertBefore(element);
                    },

                    //Highlight invalid input
                    highlight: function(element, errorClass) {
                      $(element).parent().addClass('form-field--error');

                      //TODO: temp fix for form submission bug. Report a problem needs a rewrite
                      $errorReportForm.find('.button').prop('disabled', false);
                    },

                    //Unhighlight valid input
                    unhighlight: function(element, errorClass) {
                      $(element).parent().removeClass('form-field--error');
                    },

                    //When all fields are valid perform AJAX call
                    submitHandler: function(form) {
                      submit(form, $('.report-error__content form').attr('action'));
                    }
                  });
                }
              },


              setup = function() {
                configureToggle();
                setupFormValidation();
              };

            setup();
        };

    };

    // register the main controller
    app.controller('mainController', ['$scope', 'loggingService', mainController]);
}());
