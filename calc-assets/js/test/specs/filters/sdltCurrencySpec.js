(function() {
    'use strict';

    describe('Calc Currency', function() {

    	// load the controller's module
        beforeEach(angular.mock.module("calc.filters"));

        var filter;

        beforeEach(inject(function(_$filter_) {
            filter = _$filter_;
        }));

        it('should return - if sent an undefined value', function () {
        	var amount, result;

        	result = filter('calcCurrency')(amount, '£');

        	expect(result).toEqual('-');
        });

        it('should return - if sent the value "undefined"', function () {
        	var amount = 'undefined', result;

        	result = filter('calcCurrency')(amount, '£');

        	expect(result).toEqual('-');
        });

        it('should return - if sent an empty value', function () {
        	var amount = '', result;

        	result = filter('calcCurrency')(amount, '£');

        	expect(result).toEqual('-');
        });

        it('should return a value with currency next to it if sent a whole number', function () {
        	var amount = '201', result;

        	result = filter('calcCurrency')(amount, '£');

        	expect(result).toEqual('£201');
        });

        it('should return a value with currency next to it if sent a number with a decimal', function () {
        	var amount = '200.20', result;

        	result = filter('calcCurrency')(amount, '$');

        	expect(result).toEqual('$200.20');
        });

        it('should return a value with no currency next to it if currency is undefined', function () {
        	var amount = '200', result;

        	result = filter('calcCurrency')(amount, undefined);

        	expect(result).toEqual('200');
        });

        it('should return a value with no currency next to it if currency is set as "undefined"', function () {
        	var amount = '200.20', result;

        	result = filter('calcCurrency')(amount, 'undefined');

        	expect(result).toEqual('200.20');
        });

        it('should return a value with no currency next to it if currency is an empty value', function () {
        	var amount = '201', result;

        	result = filter('calcCurrency')(amount, '');

        	expect(result).toEqual('201');
        });
    });
}());