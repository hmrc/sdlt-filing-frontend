(function() {
    "use strict";

    var validator = require("validator.js");

    var rent = require("displayLeasedYearRentFields.js");
    rent = rent();
    rent.addFunctionsToScope($scope);

    var displayExchangeContracts = function(data) {
        var allRentsBelow2000 = validator.checkAllRentsBelow2000(data);
        return (data.holdingType === 'Leasehold' && 
            data.propertyType === 'Non-residential' && 
            data.premium < 150000 && 
            allRentsBelow2000 && 
            validator.isGreaterThanOrEqualToDate(data.effectiveDate, new Date(2016, 2, 17)));
    };

    var displayContractVaried = function(data) {
        return (displayExchangeContracts() && data.contractPre201603 === 'Yes');
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
        return (displayAdditionalProperty() && data.twoOrMoreProperties === 'Yes');
    };

    var getDisplayValue = function(value) {
        if(value === undefined || value === 'undefined' || value === '') {
            return '-';
        }
        else {
            return value;
        }
    };

    var summaryHelper = function(data, modelValidationService) {
        var template = [
            {
                question   : "Freehold / leasehold",
                answer     : data.holdingType,
                link       : "#holding",
                id         : "HoldingType",
                isValid    : modelValidationService.isHoldingValid,
                hiddenText : "Is property freehold or leasehold?"
            },
            {
                question : "Property type",
                answer   : data.propertyType,
                link     : "#property",
                id       : "PropertyType",
                isValid  : modelValidationService.isPropertyValid,
                hiddenText : "Is property residential or non-residential?"
            },
            {
                question : "Effective date of transaction",
                answer   : data.effectiveDate,
                link     : "#effective-date",
                id       : "EffectiveDate",
                isValid  : modelValidationService.isEffectiveDateValid,
                hiddenText : "Effective date of your transaction?"
            },
            {
                question : "Additional residential property",
                answer   : displayAdditionalProperty(data) ? data.twoOrMoreProperties : undefined,
                link     : "#additional-property",
                id       : "TwoOrMoreProperties",
                isValid  : modelValidationService.isTwoOrMorePropertiesValid,
                hiddenText : "Will you own two or more properties?"
            },
            {
                question : "Replacing main residence",
                answer   : displayReplaceMainResidence(data) ? data.replaceMainResidence : undefined,
                link     : "#additional-property",
                id       : "ReplaceMainResidence",
                isValid  : modelValidationService.isReplaceMainResidenceValid,
                hiddenText : "Are you replacing a main residence?"
            },
            {
                question : "Purchase price",
                answer   : (data.propertyType === "Freehold") ? data.premium : undefined,
                link     : "#purchase-price",
                id       : "PurchasePrice",
                isValid  : modelValidationService.isPurchasePriceValid,
                hiddenText : "Purchase price?"
            },
            {
                question : "Start date as specified in lease",
                answer   : (data.propertyType === "Leasehold") ? data.startDate : undefined,
                link     : "#lease-dates",
                id       : "LeaseStartDate",
                isValid  : modelValidationService.isStartDateValid,
                hiddenText : "Start date as specified in lease?"
            },
            {
                question : "End date as specified in lease",
                answer   : (data.propertyType === "Leasehold") ? data.endDate : undefined,
                link     : "#lease-dates",
                id       : "LeaseEndDate",
                isValid  : modelValidationService.isEndDateValid,
                hiddenText : "End date as specified in lease?"
            },
            {
                question : "Term of lease",
                answer   : (data.propertyType === "Leasehold") ? data.leaseTerm.years + " years " + data.leaseTerm.days + " days" : undefined,
                link     : undefined,
                id       : "LeaseTerm",
                isValid  : "",
                hiddenText : undefined
            },
            {
                question : "Total premium payable",
                answer   : (data.propertyType === "Leasehold") ? data.premium : undefined,
                link     : "#premium",
                id       : "Premium",
                isValid  : modelValidationService.isPremiumValid,
                hiddenText : "Total premium payable?"
            },
            {
                question : "Year 1 rent",
                answer   : data.year1Rent,
                link     : "#rent",
                id       : "Year1Rent",
                isValid  : modelValidationService.isYear1RentValid,
                hiddenText : "Year 1 rent?"
            },
            {
                question : "Year 2 rent",
                answer   : data.year2Rent,
                link     : "#rent",
                id       : "Year2Rent",
                isValid  : modelValidationService.isYear2RentValid,
                hiddenText : "Year 2 rent?"
            },
            {
                question : "Year 3 rent",
                answer   : data.year3Rent,
                link     : "#rent",
                id       : "Year3Rent",
                isValid  : modelValidationService.isYear3RentValid,
                hiddenText : "Year 3 rent?"
            },
            {
                question : "Year 4 rent",
                answer   : data.year4Rent,
                link     : "#rent",
                id       : "Year4Rent",
                isValid  : modelValidationService.isYear4RentValid,
                hiddenText : "Year 4 rent?"
            },
            {
                question : "Year 5 rent",
                answer   : data.year5Rent,
                link     : "#rent",
                id       : "Year5Rent",
                isValid  : modelValidationService.isYear5RentValid,
                hiddenText : "Year 5 rent?"
            },
            {
                question : "Highest 12 monthly rent",
                answer   : data.highestRent,
                link     : undefined,
                id       : "HighestRent",
                isValid  : "",
                hiddenText : undefined
            },
            {
                question : "Exchange of contracts before 17 March 2016",
                answer   : data.contractPre201603,
                link     : "#exchange-contracts",
                id       : "ContractPre201603",
                isValid  : modelValidationService.isContractPre201603Valid,
                hiddenText : "Exchange of contracts before 17 March 2016?"
            },
            {
                question : "Contract changed on or after 17 March 2016",
                answer   : data.contractVariedPost201603,
                link     : "#exchange-contracts",
                id       : "ContractVariedPost201603",
                isValid  : modelValidationService.isContractVariedPost201603Valid,
                hiddenText : "Contract changed on or after 17 March 2016?"
            },
            {
                question : "Relevant rental figure",
                answer   : data.relevantRent,
                link     : "#relevant-rent",
                id       : "RelevantRent",
                isValid  : modelValidationService.isRelevantRentValid,
                hiddenText : "Relevant rental figure?"
            }
        ];

        return template.filter(function(item) {
            return item.answer !== undefined || item.answer !== "";
        });
    }

}());
