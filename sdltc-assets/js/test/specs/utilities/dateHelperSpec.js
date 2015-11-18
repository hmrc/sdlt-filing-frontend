(function() {
    'use strict';

    describe('Date Helper', function () {

        var dateHelper = require("../../../src/utilities/dateHelper.js");

        describe('Calling parseUIDate', function () {
            it('should return an empty string when no parameters are specified', function() {
                var result = dateHelper.parseUIDate();
                expect(result).toEqual('');
            });

            it('should return an empty string when undefined parameters are specified', function() {
                var result = dateHelper.parseUIDate(undefined, undefined, undefined);
                expect(result).toEqual('');
            });

            it('should return an empty string when null parameters are specified', function() {
                var result = dateHelper.parseUIDate(null, null, null);
                expect(result).toEqual('');
            });

            it('should return "bad date" when a non numeric year is specified', function() {
                var result = dateHelper.parseUIDate('year', null, null);
                expect(result).toEqual('bad date');
            });

            it('should return "bad date" when a non numeric month is specified', function() {
                var result = dateHelper.parseUIDate(null, 'month', null);
                expect(result).toEqual('bad date');
            });

            it('should return "bad date" when a non numeric day is specified', function() {
                var result = dateHelper.parseUIDate(null, null, 'day');
                expect(result).toEqual('bad date');
            });

            it('should return "bad date" when an invalid year is specified', function() {
                var result = dateHelper.parseUIDate('1111.9', '1', '11');
                expect(result).toEqual('bad date');
            });

            it('should return "bad date" when an invalid month is specified', function() {
                var result = dateHelper.parseUIDate('2000', '13', '1');
                expect(result).toEqual('bad date');
            });

            it('should return "bad date" when an invalid day is specified', function() {
                var result = dateHelper.parseUIDate('2000', '1', '32');
                expect(result).toEqual('bad date');
            });

            it('should return "bad date" when an invalid date is specified', function() {
                var result = dateHelper.parseUIDate('2001', '2', '29');
                expect(result).toEqual('bad date');
            });
            
            it('should return a date when a valid parameters are specified', function() {
                var result = dateHelper.parseUIDate('2000', '2', '29');
                expect(result).toEqual(new Date(2000, 1, 29));
            });
        });

            
        // ********************* calculateTermOfLease *********************
        describe('Calling calculateTermOfLease', function () {

            it('should return 0 yr 364 Days for effective 01/01/2014, lease 01/01/2015 to 30/12/2015', function() {
                effectiveDate = new Date(2014, 0, 1);
                leaseStartDate = new Date(2015, 0, 1);
                leaseEndDate = new Date(2015, 11, 30);
                termOfLeaseResults.years = 0;
                termOfLeaseResults.days = 364;
                termOfLeaseResults.daysInPartialYear = 365;
                expect(dateHelper.calculateTermOfLease(effectiveDate, leaseStartDate, leaseEndDate)).toEqual(termOfLeaseResults);
            });

            it('should return 0 yr 364 Days for effective 01/01/2015, lease 01/01/2014 to 30/12/2015', function() {
                effectiveDate = new Date(2015, 0, 1);
                leaseStartDate = new Date(2014, 0, 1);
                leaseEndDate = new Date(2015, 11, 30);
                termOfLeaseResults.years = 0;
                termOfLeaseResults.days = 364;
                termOfLeaseResults.daysInPartialYear = 365;
                expect(dateHelper.calculateTermOfLease(effectiveDate, leaseStartDate, leaseEndDate)).toEqual(termOfLeaseResults);
            });

            it('should return 1 yr 0 Days for effective 01/01/2014, lease 01/01/2015 to 31/12/2015', function() {
                effectiveDate = new Date(2014, 0, 1);
                leaseStartDate = new Date(2015, 0, 1);
                leaseEndDate = new Date(2015, 11, 31);
                termOfLeaseResults.years = 1;
                termOfLeaseResults.days = 0;
                termOfLeaseResults.daysInPartialYear = 0;
                expect(dateHelper.calculateTermOfLease(effectiveDate, leaseStartDate, leaseEndDate)).toEqual(termOfLeaseResults);
            });

            it('should return 1 yr 0 Days for effective 01/01/2015, lease 01/01/2014 to 31/12/2015', function() {
                effectiveDate = new Date(2015, 0, 1);
                leaseStartDate = new Date(2014, 0, 1);
                leaseEndDate = new Date(2015, 11, 31);
                termOfLeaseResults.years = 1;
                termOfLeaseResults.days = 0;
                termOfLeaseResults.daysInPartialYear = 0;
                expect(dateHelper.calculateTermOfLease(effectiveDate, leaseStartDate, leaseEndDate)).toEqual(termOfLeaseResults);
            });

            it('should return 1 yr 1 Days for effective 01/01/2014, lease 01/01/2015 to 01/01/2016', function() {
                effectiveDate = new Date(2014, 0, 1);
                leaseStartDate = new Date(2015, 0, 1);
                leaseEndDate = new Date(2016, 0, 1);
                termOfLeaseResults.years = 1;
                termOfLeaseResults.days = 1;
                termOfLeaseResults.daysInPartialYear = 366;
                expect(dateHelper.calculateTermOfLease(effectiveDate, leaseStartDate, leaseEndDate)).toEqual(termOfLeaseResults);
            });

            it('should return 1 yr 1 Days for effective 01/01/2015, lease 01/01/2014 to 01/01/2016', function() {
                effectiveDate = new Date(2015, 0, 1);
                leaseStartDate = new Date(2014, 0, 1);
                leaseEndDate = new Date(2016, 0, 1);
                termOfLeaseResults.years = 1;
                termOfLeaseResults.days = 1;
                termOfLeaseResults.daysInPartialYear = 366;
                expect(dateHelper.calculateTermOfLease(effectiveDate, leaseStartDate, leaseEndDate)).toEqual(termOfLeaseResults);
            });

            it('should return 1 yr 0 Days for effective 01/03/2014, lease 01/03/2015 to 29/02/2016', function() {
                effectiveDate = new Date(2014, 2, 1);
                leaseStartDate = new Date(2015, 2, 1);
                leaseEndDate = new Date(2016, 1, 29);
                termOfLeaseResults.years = 1;
                termOfLeaseResults.days = 0;
                termOfLeaseResults.daysInPartialYear = 0;
                expect(dateHelper.calculateTermOfLease(effectiveDate, leaseStartDate, leaseEndDate)).toEqual(termOfLeaseResults);
            });

            it('should return 1 yr 0 Days for effective 01/03/2015, lease 01/03/2014 to 29/02/2016', function() {
                effectiveDate = new Date(2015, 2, 1);
                leaseStartDate = new Date(2014, 2, 1);
                leaseEndDate = new Date(2016, 1, 29);
                termOfLeaseResults.years = 1;
                termOfLeaseResults.days = 0;
                termOfLeaseResults.daysInPartialYear = 0;
                expect(dateHelper.calculateTermOfLease(effectiveDate, leaseStartDate, leaseEndDate)).toEqual(termOfLeaseResults);
            });

            it('should return 5 yr 0 Days for effective 01/03/2014, lease 01/03/2015 to 29/02/2020', function() {
                effectiveDate = new Date(2014, 2, 1);
                leaseStartDate = new Date(2015, 2, 1);
                leaseEndDate = new Date(2020, 1, 29);
                termOfLeaseResults.years = 5;
                termOfLeaseResults.days = 0;
                termOfLeaseResults.daysInPartialYear = 0;
                expect(dateHelper.calculateTermOfLease(effectiveDate, leaseStartDate, leaseEndDate)).toEqual(termOfLeaseResults);
            });

            it('should return 5 yr 0 Days for effective 01/03/2015, lease 01/03/2014 to 29/02/2020', function() {
                effectiveDate = new Date(2015, 2, 1);
                leaseStartDate = new Date(2014, 2, 1);
                leaseEndDate = new Date(2020, 1, 29);
                termOfLeaseResults.years = 5;
                termOfLeaseResults.days = 0;
                termOfLeaseResults.daysInPartialYear = 0;
                expect(dateHelper.calculateTermOfLease(effectiveDate, leaseStartDate, leaseEndDate)).toEqual(termOfLeaseResults);
            });

        });

    });
}());
