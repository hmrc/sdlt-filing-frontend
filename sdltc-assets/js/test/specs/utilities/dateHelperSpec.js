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
                var result = dateHelper.parseUIDate('11.9', '1', '11');
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
    });
}());
