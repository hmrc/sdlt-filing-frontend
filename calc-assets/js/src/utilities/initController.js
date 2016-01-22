(function() {
    "use strict";

    // performs standard controller setup
    module.exports = function(scope, location, scrollToHash, page, dataService, navigationService){

        // log Google Analytics hit
        navigationService.logView(page);

        // copy dataService data model into scope variable 'data'
        scope.data = dataService.getModel();

        scope.jumpTo = function(id) {
            if (location.hash() !== id) {
                location.hash(id);
            }
            scrollToHash();
            $('#' + id).focus();
        };

        // add submit method to scope
        scope.submit = function() {
            navigationService.next(page, scope.data, location);
        };

        //run Get help with this page javascript
        scope.getHelpSetup("/contact/problem_reports_ajax?service=SDLTC", "https://tax.service.gov.uk/calculate-stamp-duty-land-tax/" + page);
    };
}());
