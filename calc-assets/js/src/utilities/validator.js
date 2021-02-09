(function() {
    "use strict";

    module.exports = function(){

        var integerRegex = /^[0-9]+$/;
        var floatRegex = /^(\d+)?([.]?\d{0,2})?$/;
        var posOrNegFloatRegex = /^-?[0-9]+(\.[0-9]{0,2})?$/;

        // Populated
        var isPopulated = function(value) {
            return !isNotPopulated(value);
        };

        var isNotPopulated = function(value) {
            return (value === '' || value === undefined || value.length < 1);
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

        var isGreaterThanOrEqualToDate = function(value, date) {
            return new Date(value) >= new Date(date);
        };

        var isGreaterThanInteger = function(value, integer) {
            return parseInt(value) > parseInt(integer);
        };

        var isGreaterThanFloat = function(value, float) {
            return parseFloat(value) > parseFloat(float);
        };

        var checkAllRentsBelow2000 = function(rentData) {
            // for(var i = 0; i < rentArray.length; i++){
            //     if(rentArray[i] >= 2000){
            //         return false;
            //     }
            // }
            if (rentData.year1Rent  >= 2000) return false;
            if (rentData.year2Rent  >= 2000) return false;
            if (rentData.year3Rent  >= 2000) return false;
            if (rentData.year4Rent  >= 2000) return false;
            if (rentData.year5Rent  >= 2000) return false;
            return true;
        };

        var effectiveDateWithinFTBRange = function(effectiveDate) {
          return effectiveDate >= new Date('November 22, 2017');
        };

        var effectiveDateJuly2020 = function(effectiveDate) {
            return effectiveDate < new Date('July 08, 2020');
        };

        var effectiveDateMarch2021 = function(effectiveDate) {
            return effectiveDate > new Date('March 31, 2021');
        };

        return {
            isPopulated : isPopulated,
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
            isGreaterThanOrEqualToDate : isGreaterThanOrEqualToDate,
            isGreaterThanInteger : isGreaterThanInteger,
            isGreaterThanFloat : isGreaterThanFloat,
            checkAllRentsBelow2000 : checkAllRentsBelow2000,
            effectiveDateWithinFTBRange: effectiveDateWithinFTBRange,
            effectiveDateIsBeforeJuly2020: effectiveDateJuly2020,
            effectiveDateIsAfterMarch2021: effectiveDateMarch2021
        };

    };
}());
