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

    var getEndOfFutureYear = function getEndOfFutureYear(startDate, numYears) {
        var futureDate = new Date(startDate.getFullYear() + numYears, startDate.getMonth(), startDate.getDate());
        futureDate.setDate(futureDate.getDate() -1);
        return futureDate;
    };

    var calculateTermOfLease = function calculateTermOfLease(startDate, endDate) {
           
        var numYears = 0;
        var numDays = 0;
        var numDaysInPartialYear = 0;

        numYears = 1;
        var comparisonDate = getEndOfFutureYear(startDate, numYears);
        while (comparisonDate <= endDate) {
            numYears++;
            comparisonDate = getEndOfFutureYear(startDate, numYears);
        }
        // we went past the end date so need to go back 1 year
        numYears--;

        // count the number of partial days, i.e. keep adding 1 day till we get past the end date
        numDays = 1;
        comparisonDate = getEndOfFutureYear(startDate, numYears);
        comparisonDate.setDate(comparisonDate.getDate() + 1);
        while (comparisonDate <= endDate) {
            numDays += 1;
            comparisonDate.setDate(comparisonDate.getDate() + 1);
        }
        // we went past the end date so need to go back 1 day
        numDays--;

        // need to calculate number of days in partial year (is it 365 or 366)
        if (numDays > 0) {
            var partialYearEndDate = getEndOfFutureYear(startDate, numYears + 1);
            // set comparison date to end date of last full year in term
            comparisonDate = getEndOfFutureYear(startDate, numYears);
            numDaysInPartialYear = 1;
            comparisonDate.setDate(comparisonDate.getDate() + 1);
            while (comparisonDate <= partialYearEndDate) {
                numDaysInPartialYear += 1;
                comparisonDate.setDate(comparisonDate.getDate() + 1);
            }
            numDaysInPartialYear--;
        }

        var termOfLease = {
            years : numYears,
            days : numDays,
            daysInPartialYear : numDaysInPartialYear
        };
        return termOfLease;
    };

    module.exports = {
        parseUIDate : parseUIDate,
        calculateTermOfLease : calculateTermOfLease
    };
}());
