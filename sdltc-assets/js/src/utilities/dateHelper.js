(function() {
    "use strict";

    function isValidDate(date) {
        var numberCheck = !isNaN(date);
        var dateCheck = date != 'Invalid Date';

        return numberCheck && dateCheck;
    }

    var parseUIDate = function parseUIDate(year, month, day) {

        year = year || 'empty';
        month = month || 'empty';
        day = day || 'empty';

        var dateString = year + month + day;

        if (dateString === 'emptyemptyempty') {
            return '';
        } else {
            // this can result in an invalid date but it's OK as data will only be persisted if validation passes
            var date = new Date(year, month - 1, day);

            return  isValidDate(date) ? date : 'bad date';
        }
    };

    module.exports = {
        parseUIDate : parseUIDate
    };
}());
