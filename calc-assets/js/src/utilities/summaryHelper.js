(function() {
    "use strict";

    var validator = require("validator.js");

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

    var summaryHelper = function(scope, modelValidationService) {
        var template = [
            {
                question   : "Freehold / leasehold",
                answer     : scope.data.holdingType,
                link       : "#holding",
                id         : "HoldingType",
                isValid    : modelValidationService.isHoldingValid,
                hiddenText : "Is property freehold or leasehold?"
            },
            {
                question : "Property type",
                answer   : scope.data.propertyType,
                link     : "#property",
                id       : "PropertyType",
                isValid  : modelValidationService.isPropertyValid,
                hiddenText : "Is property residential or non-residential?"
            },
            {
                question : "Effective date of transaction",
                answer   : scope.data.effectiveDate,
                link     : "#effective-date",
                id       : "EffectiveDate",
                isValid  : modelValidationService.isEffectiveDateValid,
                hiddenText : "Effective date of your transaction?"
            },
            {
                question : displayAdditionalProperty(scope.data) ? "Additional residential property" : undefined,
                answer   : scope.data.twoOrMoreProperties,
                link     : "#additional-property",
                id       : "TwoOrMoreProperties",
                isValid  : modelValidationService.isTwoOrMorePropertiesValid,
                hiddenText : "Will you own two or more properties?"
            },
            {
                question : displayReplaceMainResidence(scope.data) ? "Replacing main residence" : undefined,
                answer   : scope.data.replaceMainResidence,
                link     : "#additional-property",
                id       : "ReplaceMainResidence",
                isValid  : modelValidationService.isReplaceMainResidenceValid,
                hiddenText : "Are you replacing a main residence?"
            },
            {
                question : (scope.data.propertyType === "Freehold") ? "Purchase price" : undefined,
                answer   : scope.data.premium,
                link     : "#purchase-price",
                id       : "PurchasePrice",
                isValid  : modelValidationService.isPurchasePriceValid,
                hiddenText : "Purchase price?"
            },
            {
                question : (scope.data.propertyType === "Leasehold") ? "Start date as specified in lease" : undefined,
                answer   : scope.data.startDate,
                link     : "#lease-dates",
                id       : "LeaseStartDate",
                isValid  : modelValidationService.isStartDateValid,
                hiddenText : "Start date as specified in lease?"
            },
            {
                question : (scope.data.propertyType === "Leasehold") ? "End date as specified in lease" : undefined,
                answer   : scope.data.endDate,
                link     : "#lease-dates",
                id       : "LeaseEndDate",
                isValid  : modelValidationService.isEndDateValid,
                hiddenText : "End date as specified in lease?"
            },
            {
                question : (scope.data.propertyType === "Leasehold") ? "Term of lease" : undefined,
                answer   : scope.data.leaseTerm.years + " years " + scope.data.leaseTerm.days + " days",
                link     : undefined,
                id       : "LeaseTerm",
                isValid  : "",
                hiddenText : undefined
            },
            {
                question : (scope.data.propertyType === "Leasehold") ? "Total premium payable" : undefined,
                answer   : scope.data.premium,
                link     : "#premium",
                id       : "Premium",
                isValid  : modelValidationService.isPremiumValid,
                hiddenText : "Total premium payable?"
            },
            {
                question : (scope.displayYearOneRent) ? "Year 1 rent" : undefined,
                answer   : scope.data.year1Rent,
                link     : "#rent",
                id       : "Year1Rent",
                isValid  : modelValidationService.isYear1RentValid,
                hiddenText : "Year 1 rent?"
            },
            {
                question : (scope.displayYearTwoRent) ? "Year 2 rent" : undefined,
                answer   : scope.data.year2Rent,
                link     : "#rent",
                id       : "Year2Rent",
                isValid  : modelValidationService.isYear2RentValid,
                hiddenText : "Year 2 rent?"
            },
            {
                question : (scope.displayYearThreeRent) ? "Year 3 rent" : undefined,
                answer   : scope.data.year3Rent,
                link     : "#rent",
                id       : "Year3Rent",
                isValid  : modelValidationService.isYear3RentValid,
                hiddenText : "Year 3 rent?"
            },
            {
                question : (scope.displayYearFourRent) ? "Year 4 rent" : undefined,
                answer   : scope.data.year4Rent,
                link     : "#rent",
                id       : "Year4Rent",
                isValid  : modelValidationService.isYear4RentValid,
                hiddenText : "Year 4 rent?"
            },
            {
                question : (scope.displayYearFiveRent) ? "Year 5 rent" : undefined,
                answer   : scope.data.year5Rent,
                link     : "#rent",
                id       : "Year5Rent",
                isValid  : modelValidationService.isYear5RentValid,
                hiddenText : "Year 5 rent?"
            },
            {
                question : (scope.data.propertyType === "Leasehold") ? "Highest 12 monthly rent" : undefined,
                answer   : scope.data.highestRent,
                link     : undefined,
                id       : "HighestRent",
                isValid  : "",
                hiddenText : undefined
            },
            {
                question : (displayExchangeContracts(scope.data)) ? "Exchange of contracts before 17 March 2016" : undefined,
                answer   : scope.data.contractPre201603,
                link     : "#exchange-contracts",
                id       : "ContractPre201603",
                isValid  : modelValidationService.isContractPre201603Valid,
                hiddenText : "Exchange of contracts before 17 March 2016?"
            },
            {
                question : (displayContractVaried(scope.data)) ? "Contract changed on or after 17 March 2016" : undefined,
                answer   : scope.data.contractVariedPost201603,
                link     : "#exchange-contracts",
                id       : "ContractVariedPost201603",
                isValid  : modelValidationService.isContractVariedPost201603Valid,
                hiddenText : "Contract changed on or after 17 March 2016?"
            },
            {
                question : (displayRelevantRent(scope.data)) ? "Relevant rental figure" : undefined,
                answer   : scope.data.relevantRent,
                link     : "#relevant-rent",
                id       : "RelevantRent",
                isValid  : modelValidationService.isRelevantRentValid,
                hiddenText : "Relevant rental figure?"
            }
        ];

        return template.filter(function(item) {
            return item.question !== undefined;
        });
    };

}());
