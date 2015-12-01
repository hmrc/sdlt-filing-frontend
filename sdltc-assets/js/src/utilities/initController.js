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
    };
}());
