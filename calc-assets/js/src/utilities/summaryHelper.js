(function() {
    "use strict";

    var Validator = require("./validator.js");

    var validator = new Validator();

    var displayExchangeContracts = function(data) {
        var allRentsBelow2000 = validator.checkAllRentsBelow2000(data);
        return (data.holdingType === 'Leasehold' && 
            data.propertyType === 'Non-residential' && 
            data.premium < 150000 && 
            allRentsBelow2000 && 
            validator.isGreaterThanOrEqualToDate(data.effectiveDate, new Date(2016, 2, 17)));
    };

    var displayContractVaried = function(data) {
        return (displayExchangeContracts(data) && data.contractPre201603 === 'Yes');
    };

    var displayRelevantRent = function(data) {
        var allRentsBelow2000 = validator.checkAllRentsBelow2000(data);
        var commonChecks = (data.holdingType === 'Leasehold' && data.propertyType === 'Non-residential' && data.premium < 150000 && allRentsBelow2000);

        if (commonChecks && validator.isLessThanDate(data.effectiveDate, new Date(2016, 2, 17))) {
            return true;
        } else if (commonChecks && validator.isGreaterThanOrEqualToDate(data.effectiveDate, new Date(2016, 2, 17)) && data.contractPre201603 === 'Yes' && data.contractVariedPost201603 === 'No') {
            return true;
        }
        return false;
    };

    var displayAdditionalProperty = function(data) {
        return data.propertyType === "Residential" && validator.isGreaterThanOrEqualToDate(data.effectiveDate, new Date(2016, 3, 1));
    };

    var displayReplaceMainResidence = function(data) {
        return (displayAdditionalProperty(data) && data.twoOrMoreProperties === 'Yes');
    };

    var getDisplayValue = function(value) {
        if(value === undefined || value === 'undefined' || value === '') {
            return '-';
        }
        else {
            return value;
        }
    };

    var summaryHelper = function(scope, validatedModel) {
        var template = [
            {
                question   : "Freehold / leasehold",
                answer     : getDisplayValue(scope.data.holdingType),
                link       : "#holding",
                id         : "holdingType",
                isValid    : validatedModel.isHoldingValid,
                hiddenText : "Is property freehold or leasehold?"
            },
            {
                question   : "Property type",
                answer     : getDisplayValue(scope.data.propertyType),
                link       : "#property",
                id         : "propertyType",
                isValid    : validatedModel.isPropertyValid,
                hiddenText : "Is property residential or non-residential?"
            },
            {
                question   : "Effective date of transaction",
                answer     : getDisplayValue(scope.data.effectiveDate),
                link       : "#date",
                id         : "effectiveDate",
                isValid    : validatedModel.isEffectiveDateValid,
                hiddenText : "Effective date of your transaction?",
                type       : "Date"
            },
            {
                question   : displayAdditionalProperty(scope.data) ? "Additional residential property" : undefined,
                answer     : scope.data.twoOrMoreProperties,
                link       : "#additional-property",
                id         : "twoOrMoreProperties",
                isValid    : validatedModel.isTwoOrMorePropertiesValid,
                hiddenText : "Will you own two or more properties?"
            },
            {
                question   : displayReplaceMainResidence(scope.data) ? "Replacing main residence" : undefined,
                answer     : scope.data.replaceMainResidence,
                link       : "#additional-property",
                id         : "replaceMainResidence",
                isValid    : validatedModel.isReplaceMainResidenceValid,
                hiddenText : "Are you replacing a main residence?"
            },
            {
                question   : (scope.data.holdingType === "Freehold") ? "Purchase price" : undefined,
                answer     : scope.data.premium,
                link       : "#purchase-price",
                id         : "purchasePrice",
                isValid    : validatedModel.isPurchasePriceValid,
                hiddenText : "Purchase price?",
                type       : "Currency"
            },
            {
                question   : (scope.data.holdingType === "Leasehold") ? "Start date as specified in lease" : undefined,
                answer     : (scope.data.holdingType === "Leasehold") ? scope.data.startDate : undefined,
                link       : "#lease-dates",
                id         : "leaseStartDate",
                isValid    : validatedModel.isStartDateValid,
                hiddenText : "Start date as specified in lease?",
                type       : "Date"
            },
            {
                question   : (scope.data.holdingType === "Leasehold") ? "End date as specified in lease" : undefined,
                answer     : (scope.data.holdingType === "Leasehold") ? scope.data.endDate : undefined,
                link       : "#lease-dates",
                id         : "leaseEndDate",
                isValid    : validatedModel.isEndDateValid,
                hiddenText : "End date as specified in lease?",
                type       : "Date"
            },
            {
                question   : (scope.data.holdingType === "Leasehold") ? "Term of lease" : undefined,
                answer     : (scope.data.holdingType === "Leasehold") ? getDisplayValue(scope.data.leaseTerm.years) + " years " + getDisplayValue(scope.data.leaseTerm.days) + " days" : undefined,
                link       : undefined,
                id         : "leaseTerm",
                isValid    : "",
                hiddenText : undefined
            },
            {
                question   : (scope.data.holdingType === "Leasehold") ? "Total premium payable" : undefined,
                answer     : scope.data.premium,
                link       : "#premium",
                id         : "premium",
                isValid    : validatedModel.isPremiumValid,
                hiddenText : "Total premium payable?",
                type       : "Currency"
            },
            {
                question   : (scope.displayYearOneRent) ? "Year 1 rent" : undefined,
                answer     : (scope.displayYearOneRent) ? scope.data.year1Rent : undefined,
                link       : "#rent",
                id         : "year1Rent",
                isValid    : validatedModel.isYear1RentValid,
                hiddenText : "Year 1 rent?",
                type       : "Currency"
            },
            {
                question   : (scope.displayYearTwoRent) ? "Year 2 rent" : undefined,
                answer     : (scope.displayYearTwoRent) ? scope.data.year2Rent : undefined,
                link       : "#rent",
                id         : "year2Rent",
                isValid    : validatedModel.isYear2RentValid,
                hiddenText : "Year 2 rent?",
                type       : "Currency"
            },
            {
                question   : (scope.displayYearThreeRent) ? "Year 3 rent" : undefined,
                answer     : (scope.displayYearThreeRent) ? scope.data.year3Rent : undefined,
                link       : "#rent",
                id         : "year3Rent",
                isValid    : validatedModel.isYear3RentValid,
                hiddenText : "Year 3 rent?",
                type       : "Currency"
            },
            {
                question   : (scope.displayYearFourRent) ? "Year 4 rent" : undefined,
                answer     : (scope.displayYearFourRent) ? scope.data.year4Rent : undefined,
                link       : "#rent",
                id         : "year4Rent",
                isValid    : validatedModel.isYear4RentValid,
                hiddenText : "Year 4 rent?",
                type       : "Currency"
            },
            {
                question   : (scope.displayYearFiveRent) ? "Year 5 rent" : undefined,
                answer     : (scope.displayYearFiveRent) ? scope.data.year5Rent : undefined,
                link       : "#rent",
                id         : "year5Rent",
                isValid    : validatedModel.isYear5RentValid,
                hiddenText : "Year 5 rent?",
                type       : "Currency"
            },
            {
                question   : (scope.data.holdingType === "Leasehold") ? "Highest 12 monthly rent" : undefined,
                answer     : (scope.data.holdingType === "Leasehold") ? scope.data.highestRent : undefined,
                link       : undefined,
                id         : "highestRent",
                isValid    : "",
                hiddenText : undefined,
                type       : "Currency"
            },
            {
                question   : (displayExchangeContracts(scope.data)) ? "Exchange of contracts before 17 March 2016" : undefined,
                answer     : scope.data.contractPre201603,
                link       : "#exchange-contracts",
                id         : "contractPre201603",
                isValid    : validatedModel.isContractPre201603Valid,
                hiddenText : "Exchange of contracts before 17 March 2016?"
            },
            {
                question   : (displayContractVaried(scope.data)) ? "Contract changed on or after 17 March 2016" : undefined,
                answer     : scope.data.contractVariedPost201603,
                link       : "#exchange-contracts",
                id         : "contractVariedPost201603",
                isValid    : validatedModel.isContractVariedPost201603Valid,
                hiddenText : "Contract changed on or after 17 March 2016?"
            },
            {
                question   : (displayRelevantRent(scope.data)) ? "Relevant rental figure" : undefined,
                answer     : scope.data.relevantRent,
                link       : "#relevant-rent",
                id         : "relevantRent",
                isValid    : validatedModel.isRelevantRentValid,
                hiddenText : "Relevant rental figure?",
                type       : "Currency"
            }
        ];
        return template.filter(function(item) {
            return item.question !== undefined;
        });
    };
    
    module.exports = {
        summaryHelper : summaryHelper
    };
}());
