(function() {
	'use strict';

    describe('Validator', function () {

    	var Validator = require("../../../src/utilities/validator.js");

    	describe('Calling isPopulated()', function () {

    		it('should return false if sent an empty value', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isPopulated('');
    			expect(result).toEqual(false);
    		});

    		it('should return false if sent an undefined value', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isPopulated(undefined);
    			expect(result).toEqual(false);
    		});

    		it('should return true if sent a value', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isPopulated('hello');
    			expect(result).toEqual(true);
    		});
    	});

    	describe('Calling isNotPopulated()', function () {

    		it('should return true if sent an empty value', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isNotPopulated('');
    			expect(result).toEqual(true);
    		});

    		it('should return true if sent an undefined value', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isNotPopulated(undefined);
    			expect(result).toEqual(true);
    		});

    		it('should return false if sent a value', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isNotPopulated('hello');
    			expect(result).toEqual(false);
    		});
    	});

    	describe('Calling isNotANumber()', function () {

    		it('should return true if sent a value that is not a number', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isNotANumber('hello');
    			expect(result).toEqual(true);
    		});

    		it('should return false if sent a value that is a number', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isNotANumber(12345);
    			expect(result).toEqual(false);
    		});
    	});

    	describe('Calling isInvalidInteger()', function () {

    		it('should return true if sent a value that is not a number', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isInvalidInteger('hello');
    			expect(result).toEqual(true);
    		});

    		it('should return true if sent a value that is an invalid integer', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isInvalidInteger('12345.67');
    			expect(result).toEqual(true);
    		});

    		it('should return false if sent a value that is an integer', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isInvalidInteger('12345');
    			expect(result).toEqual(false);
    		});
    	});

    	describe('Calling isInvalidFloat()', function () {

    		it('should return true if sent a value that is not a number', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isInvalidFloat('hello');
    			expect(result).toEqual(true);
    		});

    		it('should return false if sent a value that is a valid float', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isInvalidFloat('12345.67');
    			expect(result).toEqual(false);
    		});
    	});

    	describe('Calling isInvalidPosOrNegFloat()', function () {

    		it('should return true if sent a value that is not a number', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isInvalidPosOrNegFloat('hello');
    			expect(result).toEqual(true);
    		});

    		it('should return false if sent a value that is a valid positive float', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isInvalidPosOrNegFloat('12345.67');
    			expect(result).toEqual(false);
    		});

    		it('should return false if sent a value that is a valid negative float', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isInvalidPosOrNegFloat('-12345.67');
    			expect(result).toEqual(false);
    		});
    	});

    	describe('Calling isInvalidFloatOneDecimal()', function () {

    		it('should return true if sent a value that is not a number', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isInvalidFloatOneDecimal('hello');
    			expect(result).toEqual(true);
    		});

    		it('should return false if sent a value that is a float to one decimal', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isInvalidFloatOneDecimal('12345.6');
    			expect(result).toEqual(false);
    		});

    		it('should return true if sent a value that is a float to two decimals', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isInvalidFloatOneDecimal('12345.67');
    			expect(result).toEqual(true);
    		});
    	});

    	describe('Calling isInvalidFloatTwoDecimal()', function () {

    		it('should return true if sent a value that is not a number', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isInvalidFloatTwoDecimal('hello');
    			expect(result).toEqual(true);
    		});

    		it('should return false if sent a value that is a float to one decimal', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isInvalidFloatTwoDecimal('12345.6');
    			expect(result).toEqual(false);
    		});

    		it('should return false if sent a value that is a float to two decimals', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isInvalidFloatTwoDecimal('12345.67');
    			expect(result).toEqual(false);
    		});

    		it('should return true if sent a value that is a float to three decimals', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isInvalidFloatTwoDecimal('12345.678');
    			expect(result).toEqual(true);
    		});
    	});
		
		describe('Calling isInvalidParsedDate()', function () {

    		it('should return true if sent "bad date"', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isInvalidParsedDate('bad date');
    			expect(result).toEqual(true);
    		});

    		it('should return false if sent any other value', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isInvalidParsedDate('hello');
    			expect(result).toEqual(false);
    		});
    	});

    	describe('Calling isOutsideIntegerRange()', function () {

    		it('should return true if sent a value below the range', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isOutsideIntegerRange('-1','0','10');
    			expect(result).toEqual(true);
    		});

    		it('should return true if sent a value above the range', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isOutsideIntegerRange('11','0','10');
    			expect(result).toEqual(true);
    		});

    		it('should return false if sent a value inside the range', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isOutsideIntegerRange('1','0','10');
    			expect(result).toEqual(false);
    		});
    	});

    	describe('Calling isOutsideFloatRange()', function () {

    		it('should return true if sent a value below the range', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isOutsideFloatRange('-1.9','0.9','10.9');
    			expect(result).toEqual(true);
    		});

    		it('should return true if sent a value above the range', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isOutsideFloatRange('11.9','0.9','10.9');
    			expect(result).toEqual(true);
    		});

    		it('should return false if sent a value inside the range', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isOutsideFloatRange('1.9','0.9','10.9');
    			expect(result).toEqual(false);
    		});
    	});

    	describe('Calling isLessThanInteger()', function () {

    		it('should return false if sent a value greater than the integer', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isLessThanInteger('9','0');
    			expect(result).toEqual(false);
    		});

    		it('should return false if sent a value equal to the integer', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isLessThanInteger('9','9');
    			expect(result).toEqual(false);
    		});

    		it('should return true if sent a value less than the integer', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isLessThanInteger('1','9');
    			expect(result).toEqual(true);
    		});
    	});

    	describe('Calling isLessThanFloat()', function () {

    		it('should return false if sent a value greater than the float', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isLessThanFloat('9.9','0.9');
    			expect(result).toEqual(false);
    		});

    		it('should return false if sent a value equal to the float', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isLessThanFloat('9.9','9.9');
    			expect(result).toEqual(false);
    		});

    		it('should return true if sent a value less than the float', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isLessThanFloat('1.9','9.9');
    			expect(result).toEqual(true);
    		});
    	});

    	describe('Calling isGreaterThanInteger()', function () {

    		it('should return true if sent a value greater than the integer', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isGreaterThanInteger('9','0');
    			expect(result).toEqual(true);
    		});

    		it('should return false if sent a value equal to the integer', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isGreaterThanInteger('9','9');
    			expect(result).toEqual(false);
    		});

    		it('should return false if sent a value less than the integer', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isGreaterThanInteger('1','9');
    			expect(result).toEqual(false);
    		});
    	});

    	describe('Calling isGreaterThanFloat()', function () {

    		it('should return true if sent a value greater than the float', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isGreaterThanFloat('9.9','0.9');
    			expect(result).toEqual(true);
    		});

    		it('should return false if sent a value equal to the float', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isGreaterThanFloat('9.9','9.9');
    			expect(result).toEqual(false);
    		});

    		it('should return false if sent a value less than the float', function () {
    			var validatorInstance = new Validator();
    			var result = validatorInstance.isGreaterThanFloat('1.9','9.9');
    			expect(result).toEqual(false);
    		});
    	});

    });
}());