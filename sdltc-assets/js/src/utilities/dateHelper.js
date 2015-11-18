(function() {
    "use strict";

    function isValidDate(date, year, month, day) {

        var failedNumberCheck,
            failedDateCheck,
            yearPattern = /^\d{4}$/;

        failedNumberCheck = isNaN(date);
        failedDateCheck = date == 'Invalid Date';

        if (failedNumberCheck || failedDateCheck) {
            return false;
        }

        if (!yearPattern.test(year)) {
            return false;
        }

        if (date.getFullYear() !== parseInt(year) || 
            date.getMonth() !== (parseInt(month) - 1) || 
            date.getDate() !== parseInt(day)) {
            return false;
        }

        return true;
    }

    var parseUIDate = function parseUIDate(year, month, day) {

        var dateString,
            date;

        year = year || 'empty';
        month = month || 'empty';
        day = day || 'empty';

        dateString = year + month + day;

        // mandatory check
        if (dateString === 'emptyemptyempty') {
            return '';
        }

        date = new Date(year, month - 1, day);

        return isValidDate(date, year, month, day) ? date : 'bad date';
    };

    var calculateTermOfLease = function calculateTermOfLease(effectiveDate, leaseStartDate, leaseEndDate) {

        var startDate = leaseStartDate;
        if (effectiveDate > leaseStartDate) {
            startDate = effectiveDate;
        }
        var endDate = leaseEndDate;

        var numYears = 0;
        var numDays = 0;
        var numDaysInPartialYear = 0;

        function getEndOfFutureYear(startDate, numYears) {
            var futureDate = new Date(startDate.getFullYear() + numYears, startDate.getMonth(), startDate.getDate());
            futureDate.setDate(futureDate.getDate() -1);
            return futureDate;
        }

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
