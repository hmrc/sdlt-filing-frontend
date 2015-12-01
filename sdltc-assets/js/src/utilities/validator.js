(function() {
    "use strict";

    module.exports = function(){

        var integerRegex = /^[0-9]+$/;
        var floatRegex = /^(\d+)?([.]?\d{0,2})?$/;
        var posOrNegFloatRegex = /^-?[0-9]+(\.[0-9]{0,2})?$/;

        // Populated
        var isNotPopulated = function(value) {
            return !(value && value !== undefined && value.length > 0);
        };

        // Format
        var isNotANumber = function(value) { return isNaN(value); };

        var isInvalidInteger = function(value) {
            return isNaN(value) || !value.match(integerRegex);
        };

        var isInvalidFloat = function(value) {
            return isNaN(value) || !value.match(floatRegex);
        };

        var isInvalidPosOrNegFloat = function(value) {
            return isNaN(value) || !value.match(posOrNegFloatRegex);
        };

        var isInvalidFloatOneDecimal = function(value) {
            return (parseFloat(value).toFixed(1) != parseFloat(value));
        };

        var isInvalidFloatTwoDecimal = function(value) {
            return (parseFloat(value).toFixed(2) != parseFloat(value));
        };

        var isInvalidParsedDate = function(value) {
            return value === 'bad date';
        };

        // Range
        var isOutsideIntegerRange = function(value, min, max) {
            return parseInt(value) < parseInt(min) || parseInt(value) > parseInt(max);
        };

        var isOutsideFloatRange = function(value, min, max) {
            return parseFloat(value) < parseFloat(min) || parseFloat(value) > parseFloat(max);
        };

        // Less/Greater than
        var isLessThanInteger = function(value, integer) {
            return parseInt(value) < parseInt(integer);
        };

        var isLessThanFloat = function(value, float) {
            return parseFloat(value) < parseFloat(float);
        };

        var isLessThanDate = function(value, date) {
            return new Date(value) < new Date(date);
        };

        var isGreaterThanInteger = function(value, integer) {
            return parseInt(value) > parseInt(integer);
        };

        var isGreaterThanFloat = function(value, float) {
            return parseFloat(value) > parseFloat(float);
        };

        return {
            isNotPopulated : isNotPopulated,
            isNotANumber : isNotANumber,
            isInvalidInteger : isInvalidInteger,
            isInvalidFloat : isInvalidFloat,
            isInvalidPosOrNegFloat : isInvalidPosOrNegFloat,
            isInvalidFloatOneDecimal : isInvalidFloatOneDecimal,
            isInvalidFloatTwoDecimal : isInvalidFloatTwoDecimal,
            isInvalidParsedDate : isInvalidParsedDate,
            isOutsideIntegerRange : isOutsideIntegerRange,
            isOutsideFloatRange : isOutsideFloatRange,
            isLessThanInteger : isLessThanInteger,
            isLessThanFloat : isLessThanFloat,
            isLessThanDate : isLessThanDate,
            isGreaterThanInteger : isGreaterThanInteger,
            isGreaterThanFloat : isGreaterThanFloat
        };

    };
}());
