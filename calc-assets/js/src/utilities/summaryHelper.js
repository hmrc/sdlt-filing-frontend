(function() {
    "use strict";

    var Validator = require("./validator.js");

    var validator = new Validator();

    var displayFreehold = function(data) {
        if(data === undefined) return false;
        return data.holdingType === "Freehold";
    };

    var displayLeasehold = function(data) {
        if(data === undefined) return false;
        return data.holdingType === "Leasehold";
    };

    var displayTermOfLease = function(data) {
        if(data === undefined) return false;
        return displayLeasehold(data) && data.leaseTerm !== undefined;
    };

    var displayExchangeContracts = function(data) {
        if(data === undefined) return false;
        var allRentsBelow2000 = validator.checkAllRentsBelow2000(data);
        return (data.holdingType === 'Leasehold' && 
            data.propertyType === 'Non-residential' && 
            data.premium < 150000 && 
            allRentsBelow2000 && 
            validator.isGreaterThanOrEqualToDate(data.effectiveDate, new Date(2016, 2, 17)));
    };

    var displayContractVaried = function(data) {
        if(data === undefined) return false;
        return (displayExchangeContracts(data) && data.contractPre201603 === 'Yes');
    };

    var displayRelevantRent = function(data) {
        if(data === undefined) return false;
        var allRentsBelow2000 = validator.checkAllRentsBelow2000(data);
        var commonChecks = (data.holdingType === 'Leasehold' && data.propertyType === 'Non-residential' && data.premium < 150000 && allRentsBelow2000);

        if (commonChecks && validator.isLessThanDate(data.effectiveDate, new Date(2016, 2, 17))) {
            return true;
        } else if (commonChecks && validator.isGreaterThanOrEqualToDate(data.effectiveDate, new Date(2016, 2, 17)) && data.contractPre201603 === 'Yes' && data.contractVariedPost201603 === 'No') {
            return true;
        }
        return false;
    };

    var displayIndividual = function(data) {
        if(data === undefined) return false;
        return data.propertyType === "Residential" && validator.isGreaterThanOrEqualToDate(data.effectiveDate, new Date(2016, 3, 1));
    };

    var displayAdditionalProperty = function(data) {
        if(data === undefined) return false;
        return (displayIndividual(data) && data.individual === "Yes");
    };

    var displayOwnedOtherProperties = function(data) {
        if(data === undefined) return false;
        return (data.propertyType === 'Residential' && data.individual === 'Yes'
            && data.twoOrMoreProperties == 'No'
            && validator.effectiveDateWithinFTBRange(data.effectiveDate)
            && (validator.effectiveDateIsAfterJuly2020(data.effectiveDate)
            ||  validator.effectiveDateIsAfterMarch2021(data.effectiveDate)));
    };

    var displayMainResidence = function(data) {
        if(displayOwnedOtherProperties(data)) {
            return data.ownedOtherProperties === 'No';
        } else {
            return false;
        }
    };

    var displaySharedOwnership = function(data) {
        if(displayMainResidence(data) && data.holdingType === 'Leasehold') {
            return data.mainResidence === 'Yes';
        } else {
            return false;
        }
    };

    var displayCurrentValue = function(data) {
        if(displaySharedOwnership(data)) {
            return data.sharedOwnership === 'Yes';
        } else {
            return false;
        }
    };

    var displayPaySDLT = function(data) {
        if(displayCurrentValue(data)) {
            return data.currentValue === '£500,000 or less';
        } else {
            return false;
        }
    };

    var displayReplaceMainResidence = function(data) {
        if(data === undefined) return false;
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
                question   : "Freehold or leasehold",
                answer     : (scope.data !== undefined) ? getDisplayValue(scope.data.holdingType) : undefined,
                link       : "#holding",
                id         : "holdingType",
                isValid    : validatedModel.isHoldingValid,
                hiddenText : "Is property freehold or leasehold?"
            },
            {
                question   : "Residential or non-residential",
                answer     : (scope.data !== undefined) ? getDisplayValue(scope.data.propertyType) : undefined,
                link       : "#property",
                id         : "propertyType",
                isValid    : validatedModel.isPropertyValid,
                hiddenText : "Is property residential or non-residential?"
            },
            {
                question   : "Effective date of transaction",
                answer     : (scope.data !== undefined) ? getDisplayValue(scope.data.effectiveDate) : undefined,
                link       : "#date",
                id         : "effectiveDate",
                isValid    : validatedModel.isEffectiveDateValid,
                hiddenText : "Effective date of your transaction?",
                type       : "Date"
            },
            {
                question   : displayIndividual(scope.data) ? "Individual" : undefined,
                answer     : (scope.data !== undefined) ? getDisplayValue(scope.data.individual) : undefined,
                link       : "#purchaser",
                id         : "individual",
                isValid    : validatedModel.isIndividualValid,
                hiddenText : "Are you purchasing the property as an individual?"
            },
            {
                question   : displayAdditionalProperty(scope.data) ? "Additional residential property" : undefined,
                answer     : (scope.data !== undefined) ? scope.data.twoOrMoreProperties : undefined,
                link       : "#additional-property",
                id         : "twoOrMoreProperties",
                isValid    : validatedModel.isTwoOrMorePropertiesValid,
                hiddenText : "Will you own two or more properties?"
            },
            {
                question   : displayReplaceMainResidence(scope.data) ? "Replacing main residence" : undefined,
                answer     : (scope.data !== undefined) ? scope.data.replaceMainResidence : undefined,
                link       : "#additional-property",
                id         : "replaceMainResidence",
                isValid    : validatedModel.isReplaceMainResidenceValid,
                hiddenText : "Are you replacing a main residence?"
            },
            {
                question   : displayOwnedOtherProperties(scope.data) ? "Owned other property" : undefined,
                answer     : (scope.data !== undefined) ? scope.data.ownedOtherProperties : undefined,
                link       : "#owned-other-properties",
                id         : "ownedOtherProperties",
                isValid    : validatedModel.isOwnedOtherPropertiesValid,
                hiddenText : "Have you ever owned any other property?"
            },
            {
                question   : displayMainResidence(scope.data) ? "Main residence" : undefined,
                answer     : (scope.data !== undefined) ? scope.data.mainResidence : undefined,
                link       : "#main-residence",
                id         : "mainResidence",
                isValid    : validatedModel.isMainResidenceValid,
                hiddenText : "Will this property be your main residence?"
            },
            {
                question   : displaySharedOwnership(scope.data) ? "Shared ownership" : undefined,
                answer     : (scope.data !== undefined) ? scope.data.sharedOwnership : undefined,
                link       : "#shared-ownership",
                id         : "sharedOwnership",
                isValid    : validatedModel.isSharedOwnershipValid,
                hiddenText : "Are you buying the property through a shared ownership scheme?"
            },
            {
                question   : displayCurrentValue(scope.data) ? "Market value" : undefined,
                answer     : (scope.data !== undefined) ? scope.data.currentValue : undefined,
                link       : "#current-value",
                id         : "currentValue",
                isValid    : validatedModel.isCurrentValueValid,
                hiddenText : "Is the current market value of the property £500,000 or less?"
            },

            {
                question   : displayPaySDLT(scope.data) ? "Pay SDLT" : undefined,
                answer     : displayPaySDLT(scope.data) ? scope.data.paySDLT : undefined,
                link       : "#market-value",
                id         : "market-value",
                isValid    : validatedModel.isMarketValueValid,
                hiddenText : "Pay SDLT"
            },
            {
                question   : displayFreehold(scope.data) ? "Purchase price" : undefined,
                answer     : displayFreehold(scope.data) ? scope.data.premium : undefined,
                link       : "#purchase-price",
                id         : "purchasePrice",
                isValid    : validatedModel.isPurchasePriceValid,
                hiddenText : "Purchase price?",
                type       : "Currency"
            },
            {
                question   : displayLeasehold(scope.data) ? "Start date as specified in lease" : undefined,
                answer     : displayLeasehold(scope.data) ? scope.data.startDate : undefined,
                link       : "#lease-dates",
                id         : "leaseStartDate",
                isValid    : validatedModel.isStartDateValid,
                hiddenText : "Start date as specified in lease?",
                type       : "Date"
            },
            {
                question   : displayLeasehold(scope.data) ? "End date as specified in lease" : undefined,
                answer     : displayLeasehold(scope.data) ? scope.data.endDate : undefined,
                link       : "#lease-dates",
                id         : "leaseEndDate",
                isValid    : validatedModel.isEndDateValid,
                hiddenText : "End date as specified in lease?",
                type       : "Date"
            },
            {
                question   : displayTermOfLease(scope.data) ? "Term of lease" : undefined,
                answer     : displayTermOfLease(scope.data) ? getDisplayValue(scope.data.leaseTerm.years) + " years " + getDisplayValue(scope.data.leaseTerm.days) + " days" : undefined,
                link       : undefined,
                id         : "leaseTerm",
                isValid    : "",
                hiddenText : undefined
            },
            {
                question   : displayLeasehold(scope.data) ? "Premium" : undefined,
                answer     : displayLeasehold(scope.data) ? scope.data.premium : undefined,
                link       : "#premium",
                id         : "premium",
                isValid    : validatedModel.isPremiumValid,
                hiddenText : "Premium payable?",
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
                question   : displayLeasehold(scope.data) ? "Highest 12 monthly rent" : undefined,
                answer     : displayLeasehold(scope.data) ? scope.data.highestRent : undefined,
                link       : undefined,
                id         : "highestRent",
                isValid    : "",
                hiddenText : undefined,
                type       : "Currency"
            },
            {
                question   : (displayExchangeContracts(scope.data)) ? "Exchange of contracts before 17 March 2016" : undefined,
                answer     : (displayExchangeContracts(scope.data)) ? scope.data.contractPre201603 : undefined,
                link       : "#exchange-contracts",
                id         : "contractPre201603",
                isValid    : validatedModel.isContractPre201603Valid,
                hiddenText : "Exchange of contracts before 17 March 2016?"
            },
            {
                question   : (displayContractVaried(scope.data)) ? "Contract changed on or after 17 March 2016" : undefined,
                answer     : (displayContractVaried(scope.data)) ? scope.data.contractVariedPost201603 : undefined,
                link       : "#exchange-contracts",
                id         : "contractVariedPost201603",
                isValid    : validatedModel.isContractVariedPost201603Valid,
                hiddenText : "Contract changed on or after 17 March 2016?"
            },
            {
                question   : (displayRelevantRent(scope.data)) ? "Relevant rental figure" : undefined,
                answer     : (displayRelevantRent(scope.data)) ? scope.data.relevantRent : undefined,
                link       : "#relevant-rent",
                id         : "relevantRent",
                isValid    : validatedModel.isRelevantRentValid,
                hiddenText : "Relevant rental figure?",
                type       : "Currency"
            }
        ];
        var result = [];
        for(var i = 0; i < template.length; i++) {
            if(template[i].question !== undefined) {
                result.push(template[i]);
            }
        }
        
        return result;
    };
    
    module.exports = {
        summaryHelper : summaryHelper
    };
}());
