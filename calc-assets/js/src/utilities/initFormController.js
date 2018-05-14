(function() {
    "use strict";

    // performs standard controller setup
    module.exports = function(scope, location, scrollToHash, page, dataService, validationService, navigationService){

        // log Google Analytics hit
        navigationService.logView(page);

        // copy dataService data model into scope variable 'data'
        scope.data = dataService.getModel();

        // set scope variable 'state' to have an object with a hasError function that returns false
        scope.state = {
            isValid: true,
            hasError: function() {
                // empty so that on page load no fieldset class is set
                return "";
            }
        };

         var pageLocation = window.location.href.split("/").slice(-1)[0];
         if(pageLocation == "result"){ scope.isResultPage = true; } else { scope.isResultPage = false; }

        scope.jumpTo = function(id) {
            if (location.hash() !== id) {
                location.hash(id);
            }
            scrollToHash();
            $('#' + id).focus();
        };
        
        // hide error summary
        $('#pageErrors').hide();

        // add submit method to scope
        scope.submit = function() {
            scope.state = validationService.validate(scope.data);

            if (scope.state.isValid) {
                if (angular.isDefined(scope.beforeUpdateModel)) {       
                    scope.beforeUpdateModel();
                }
                dataService.updateModel(scope.data);
                navigationService.next(page, scope.data, location);
            } else {
                $('#pageErrors').show().focus();
            }
        };
    };
}());
