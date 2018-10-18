(function() {
  'use strict';

  describe('Data Marshalling Service', function () {

    var service;

    var baseLeaseholdData = {
                  holdingType: "Leasehold",
                  propertyType: "Residential",
                  effectiveDate: new Date(2017, 9, 13),
                  effectiveDateDay: 13,
                  effectiveDateMonth: 10,
                  effectiveDateYear: 2017,
                  individual: "Yes",
                  twoOrMoreProperties: "Yes",
                  replaceMainResidence: "No",
                  sharedOwnership : "N/A",
                  startDate: new Date(2000, 0, 1),
                  startDateDay: 1,
                  startDateMonth: 1,
                  startDateYear: 2000,
                  endDate: new Date(2099, 11, 31),
                  endDateDay: 31,
                  endDateMonth: 12,
                  endDateYear: 2099,
                  leaseTerm: {
                    years: 82,
                    days: 80,
                    daysInPartialYear: 365,
                   },
                  premium: 500000,
                  year1Rent: 1000,
                  year2Rent: 2000,
                  year3Rent: 3000,
                  year4Rent: 4000,
                  year5Rent: 5000,
                  highestRent: 5000
                };

    var baseLeaseholdRequest = {
                  holdingType:"Leasehold",
                  propertyType: "Residential",
                  effectiveDateDay: 13,
                  effectiveDateMonth: 10,
                  effectiveDateYear: 2017,
                  propertyDetails: {
                    individual: "Yes",
                    twoOrMoreProperties: "Yes",
                    replaceMainResidence: "No",
                      sharedOwnership : "N/A"

                  },
                  leaseDetails: {
                    startDateDay: 1,
                    startDateMonth: 1,
                    startDateYear: 2000,
                    endDateDay: 31,
                    endDateMonth: 12,
                    endDateYear: 2099,
                    leaseTerm:  {
                      years: 82,
                      days: 80,
                      daysInPartialYear: 365,
                    },
                    year1Rent: 1000,
                    year2Rent: 2000,
                    year3Rent: 3000,
                    year4Rent: 4000,
                    year5Rent: 5000
                  },
                  premium: 500000,
                  highestRent: 5000
                };

    beforeEach(angular.mock.module("calc.services"));

    beforeEach(inject(function (_dataMarshallingService_) {
      service = _dataMarshallingService_;
    }));

    it('should construct a leasehold, residential calculation request from full data', function() {
        var withoutSharedOwnership =  angular.copy(baseLeaseholdData);
        delete withoutSharedOwnership.sharedOwnership;
        delete baseLeaseholdRequest.propertyDetails.sharedOwnership;

        expect(service.constructCalculationRequest(withoutSharedOwnership)).toEqual(baseLeaseholdRequest);
    });

    it('should construct a leasehold, residential calculation request without property details when effectiveDate is before Apr 2016', function() {

        var earlyLeaseholdData = angular.copy(baseLeaseholdData);
        earlyLeaseholdData.effectiveDate = new Date(2016, 2, 31);
        earlyLeaseholdData.effectiveDateDay = 31;
        earlyLeaseholdData.effectiveDateMonth = 3;
        earlyLeaseholdData.effectiveDateYear = 2016;

        var earlyLeaseholdRequest = angular.copy(baseLeaseholdRequest);
        delete earlyLeaseholdRequest.propertyDetails;
        earlyLeaseholdRequest.effectiveDateDay = 31;
        earlyLeaseholdRequest.effectiveDateMonth = 3;
        earlyLeaseholdRequest.effectiveDateYear = 2016;

        expect(service.constructCalculationRequest(earlyLeaseholdData)).toEqual(earlyLeaseholdRequest);
    });

    it('should only include propertyDetails info on Individual if individual is "No"', function() {

        var notIndividualData = angular.copy(baseLeaseholdData);
        notIndividualData.individual = "No";

        var notIndividualRequest = angular.copy(baseLeaseholdRequest);
        notIndividualRequest.propertyDetails.individual = "No";
        delete notIndividualRequest.propertyDetails.twoOrMoreProperties;
        delete notIndividualRequest.propertyDetails.replaceMainResidence;

        expect(service.constructCalculationRequest(notIndividualData)).toEqual(notIndividualRequest);
    });

    it('should not include propertyDetails info on replaceMainResidence if twoOrMoreProperties is "No"', function() {

        var singlePropertyData = angular.copy(baseLeaseholdData);
        singlePropertyData.individual = "Yes";
        singlePropertyData.twoOrMoreProperties = "No";
        singlePropertyData.sharedOwnership = "No";

        var singlePropertyRequest = angular.copy(baseLeaseholdRequest);
        singlePropertyRequest.propertyDetails.individual = "Yes";
        singlePropertyRequest.propertyDetails.twoOrMoreProperties = "No";
        delete singlePropertyRequest.propertyDetails.replaceMainResidence;

        expect(service.constructCalculationRequest(singlePropertyData)).toEqual(singlePropertyRequest);
    });

    it('should include full propertyDetails info if individual is "Yes" and twoOrMoreProperties is "Yes"', function() {

        var fullPropertyDetailsData = angular.copy(baseLeaseholdData);
        fullPropertyDetailsData.individual = "Yes";
        fullPropertyDetailsData.twoOrMoreProperties = "Yes";
        fullPropertyDetailsData.replaceMainResidence = "No";

        var fullPropertyDetailsRequest = angular.copy(baseLeaseholdRequest);
        fullPropertyDetailsRequest.propertyDetails.individual = "Yes";
        fullPropertyDetailsRequest.propertyDetails.twoOrMoreProperties = "Yes";
        fullPropertyDetailsRequest.propertyDetails.replaceMainResidence = "No";

        expect(service.constructCalculationRequest(fullPropertyDetailsData)).toEqual(fullPropertyDetailsRequest);
    });

    it('should only include the first rental year in a lease term of 1 year', function() {

        var oneYearLeaseholdData = angular.copy(baseLeaseholdData);
        oneYearLeaseholdData.leaseTerm.years = 1;
        oneYearLeaseholdData.leaseTerm.days = 0;

        var oneYearLeaseholdRequest = angular.copy(baseLeaseholdRequest);
        oneYearLeaseholdRequest.leaseDetails.leaseTerm.years = 1;
        oneYearLeaseholdRequest.leaseDetails.leaseTerm.days = 0;
        delete oneYearLeaseholdRequest.leaseDetails.year2Rent;
        delete oneYearLeaseholdRequest.leaseDetails.year3Rent;
        delete oneYearLeaseholdRequest.leaseDetails.year4Rent;
        delete oneYearLeaseholdRequest.leaseDetails.year5Rent;

        expect(service.constructCalculationRequest(oneYearLeaseholdData)).toEqual(oneYearLeaseholdRequest);
    });

    it('should only include the first two rental years in a lease term of 1 year, 1 day', function() {

        var oneYearOneDayLeaseholdData = angular.copy(baseLeaseholdData);
        oneYearOneDayLeaseholdData.leaseTerm.years = 1;
        oneYearOneDayLeaseholdData.leaseTerm.days = 1;

        var oneYearOneDayLeaseholdRequest = angular.copy(baseLeaseholdRequest);
        oneYearOneDayLeaseholdRequest.leaseDetails.leaseTerm.years = 1;
        oneYearOneDayLeaseholdRequest.leaseDetails.leaseTerm.days = 1;
        delete oneYearOneDayLeaseholdRequest.leaseDetails.year3Rent;
        delete oneYearOneDayLeaseholdRequest.leaseDetails.year4Rent;
        delete oneYearOneDayLeaseholdRequest.leaseDetails.year5Rent;

        expect(service.constructCalculationRequest(oneYearOneDayLeaseholdData)).toEqual(oneYearOneDayLeaseholdRequest);
    });

    it('should include all five rental years in a lease term of 5 years, 0 days', function() {

        var fiveYearsLeaseholdData = angular.copy(baseLeaseholdData);
        fiveYearsLeaseholdData.leaseTerm.years = 5;
        fiveYearsLeaseholdData.leaseTerm.days = 0;

        var fiveYearsLeaseholdRequest = angular.copy(baseLeaseholdRequest);
        fiveYearsLeaseholdRequest.leaseDetails.leaseTerm.years = 5;
        fiveYearsLeaseholdRequest.leaseDetails.leaseTerm.days = 0;

        expect(service.constructCalculationRequest(fiveYearsLeaseholdData)).toEqual(fiveYearsLeaseholdRequest);
    });

    it('should include all five rental years in a lease term of 5 years, 1 day', function() {

        var fiveYearsLeaseholdData = angular.copy(baseLeaseholdData);
        fiveYearsLeaseholdData.leaseTerm.years = 5;
        fiveYearsLeaseholdData.leaseTerm.days = 1;

        var fiveYearsLeaseholdRequest = angular.copy(baseLeaseholdRequest);
        fiveYearsLeaseholdRequest.leaseDetails.leaseTerm.years = 5;
        fiveYearsLeaseholdRequest.leaseDetails.leaseTerm.days = 1;

        expect(service.constructCalculationRequest(fiveYearsLeaseholdData)).toEqual(fiveYearsLeaseholdRequest);
    });

    var baseFreeholdData = {
                  holdingType: "Freehold",
                  propertyType: "Residential",
                  effectiveDate: new Date(2017, 9, 13),
                  effectiveDateDay: 13,
                  effectiveDateMonth: 10,
                  effectiveDateYear: 2017,
                  individual: "Yes",
                  twoOrMoreProperties: "Yes",
                  replaceMainResidence: "No",
                  premium: 500000,
                  highestRent: 5000
                };

    var baseFreeholdRequest = {
                  holdingType:"Freehold",
                  propertyType: "Residential",
                  effectiveDateDay: 13,
                  effectiveDateMonth: 10,
                  effectiveDateYear: 2017,
                  propertyDetails: {
                    individual: "Yes",
                    twoOrMoreProperties: "Yes",
                    replaceMainResidence: "No"
                  },
                  premium: 500000,
                  highestRent: 5000
                };


    it('should construct a freehold, residential calculation request from full data', function() {

        var freeholdData = angular.copy(baseFreeholdData);

        var freeholdRequest = angular.copy(baseFreeholdRequest);

        expect(service.constructCalculationRequest(freeholdData)).toEqual(freeholdRequest);
    });

    it('should ignore property details for a calculation request from before Apr 2016', function() {

        var freeholdPreApr16Data = angular.copy(baseFreeholdData);
        freeholdPreApr16Data.effectiveDateDay = 31;
        freeholdPreApr16Data.effectiveDateMonth = 3;
        freeholdPreApr16Data.effectiveDateYear = 2016;
        freeholdPreApr16Data.effectiveDate = new Date(2016, 2, 31);

        var freeholdPreApr16Request = angular.copy(baseFreeholdRequest);
        freeholdPreApr16Request.effectiveDateDay = 31;
        freeholdPreApr16Request.effectiveDateMonth = 3;
        freeholdPreApr16Request.effectiveDateYear = 2016;
        delete freeholdPreApr16Request.propertyDetails;

        expect(service.constructCalculationRequest(freeholdPreApr16Data)).toEqual(freeholdPreApr16Request);
    });

    it('should ignore property details for a non-residential calculation request', function() {

        var freeholdNonResData = angular.copy(baseFreeholdData);
        freeholdNonResData.propertyType = "Non-residential";

        var freeholdNonResRequest = angular.copy(baseFreeholdRequest);
        freeholdNonResRequest.propertyType = "Non-residential";
        delete freeholdNonResRequest.propertyDetails;

        expect(service.constructCalculationRequest(freeholdNonResData)).toEqual(freeholdNonResRequest);
    });

    it('should ignore lease details for a freehold calculation', function() {

        var freeholdLeaseData = angular.copy(baseLeaseholdData);
        freeholdLeaseData.holdingType = "Freehold";

        var freeholdLeaseRequest = angular.copy(baseLeaseholdRequest);
        freeholdLeaseRequest.holdingType = "Freehold";
        delete freeholdLeaseRequest.leaseDetails;

        expect(service.constructCalculationRequest(freeholdLeaseData)).toEqual(freeholdLeaseRequest);
    });

    var leaseHoldNonResRentsUnder2kData = {
                  holdingType: "Leasehold",
                  propertyType: "Non-residential",
                  effectiveDate: new Date(2017, 9, 13),
                  effectiveDateDay: 13,
                  effectiveDateMonth: 10,
                  effectiveDateYear: 2017,
                  individual: "No",
                  startDate: new Date(2000, 0, 1),
                  startDateDay: 1,
                  startDateMonth: 1,
                  startDateYear: 2000,
                  endDate: new Date(2099, 11, 31),
                  endDateDay: 31,
                  endDateMonth: 12,
                  endDateYear: 2099,
                  leaseTerm: {
                    years: 82,
                    days: 80,
                    daysInPartialYear: 365,
                   },
                  premium: 149999,
                  year1Rent: 1000,
                  year2Rent: 1100,
                  year3Rent: 1200,
                  year4Rent: 1300,
                  year5Rent: 1400,
                  highestRent: 1400,
                  contractPre201603: "Yes",
                  contractVariedPost201603: "No",
                  relevantRent: 999
                };

      var leaseHoldNonResRentsUnder2kRequest = {
                  holdingType:"Leasehold",
                  propertyType: "Non-residential",
                  effectiveDateDay: 13,
                  effectiveDateMonth: 10,
                  effectiveDateYear: 2017,
                  leaseDetails: {
                    startDateDay: 1,
                    startDateMonth: 1,
                    startDateYear: 2000,
                    endDateDay: 31,
                    endDateMonth: 12,
                    endDateYear: 2099,
                    leaseTerm:  {
                      years: 82,
                      days: 80,
                      daysInPartialYear: 365,
                    },
                    year1Rent: 1000,
                    year2Rent: 1100,
                    year3Rent: 1200,
                    year4Rent: 1300,
                    year5Rent: 1400
                  },
                  premium: 149999,
                  highestRent: 1400,
                  relevantRentDetails: {
                    contractPre201603: "Yes",
                    contractVariedPost201603: "No",
                    relevantRent: 999
                  }
                };

    it('should include full relevant rent details for leasehold non-residential, prem <150000 and all rents <2000', function() {

        var leaseholdRelRentData = angular.copy(leaseHoldNonResRentsUnder2kData);

        var leaseholdRelRentRequest = angular.copy(leaseHoldNonResRentsUnder2kRequest);

        expect(service.constructCalculationRequest(leaseholdRelRentData)).toEqual(leaseholdRelRentRequest);
    });

    it('should not include relevant rent amount or contract varied flag if contract was not signed pre-Mar 2016', function() {

        var noEarlyContractData = angular.copy(leaseHoldNonResRentsUnder2kData);
        noEarlyContractData.contractPre201603 = "No";

        var noEarlyContractRequest = angular.copy(leaseHoldNonResRentsUnder2kRequest);
        noEarlyContractRequest.relevantRentDetails.contractPre201603 = "No";
        delete noEarlyContractRequest.relevantRentDetails.contractVariedPost201603;
        delete noEarlyContractRequest.relevantRentDetails.relevantRent;

        expect(service.constructCalculationRequest(noEarlyContractData)).toEqual(noEarlyContractRequest);
    });

    it('should not include relevant rent amount if contract varied since Mar 2016', function() {

        var contractChangedData = angular.copy(leaseHoldNonResRentsUnder2kData);
        contractChangedData.contractPre201603 = "Yes";
        contractChangedData.contractVariedPost201603 = "Yes";

        var contractChangedRequest = angular.copy(leaseHoldNonResRentsUnder2kRequest);
        contractChangedRequest.relevantRentDetails.contractPre201603 = "Yes";
        contractChangedRequest.relevantRentDetails.contractVariedPost201603 = "Yes";
        delete contractChangedRequest.relevantRentDetails.relevantRent;

        expect(service.constructCalculationRequest(contractChangedData)).toEqual(contractChangedRequest);
    });

    it('should include minimal relevant rent details for leasehold non-residential, prem <150000, all rents <2000, effectiveDate <Mar 16, 2016', function() {

        var leaseholdPreMar16RelRentData = angular.copy(leaseHoldNonResRentsUnder2kData);
        leaseholdPreMar16RelRentData.effectiveDateDay = 15;
        leaseholdPreMar16RelRentData.effectiveDateMonth = 3;
        leaseholdPreMar16RelRentData.effectiveDateYear = 2016;
        leaseholdPreMar16RelRentData.effectiveDate = new Date(2016, 2, 15);

        var leaseholdPreMar16RelRentRequest = angular.copy(leaseHoldNonResRentsUnder2kRequest);
        leaseholdPreMar16RelRentRequest.effectiveDateDay = 15;
        leaseholdPreMar16RelRentRequest.effectiveDateMonth = 3;
        leaseholdPreMar16RelRentRequest.effectiveDateYear = 2016;
        delete leaseholdPreMar16RelRentRequest.relevantRentDetails.contractPre201603;
        delete leaseholdPreMar16RelRentRequest.relevantRentDetails.contractVariedPost201603;

        expect(service.constructCalculationRequest(leaseholdPreMar16RelRentData)).toEqual(leaseholdPreMar16RelRentRequest);
    });

    it('should not include relevant rent details for residential property', function() {

        var leaseholdResData = angular.copy(leaseHoldNonResRentsUnder2kData);
        leaseholdResData.propertyType = "Residential";

        var leaseholdResRequest = angular.copy(leaseHoldNonResRentsUnder2kRequest);
        leaseholdResRequest.propertyType = "Residential";
        leaseholdResRequest.propertyDetails = {
          individual: "No"
        };
        delete leaseholdResRequest.relevantRentDetails;

        expect(service.constructCalculationRequest(leaseholdResData)).toEqual(leaseholdResRequest);
    });

    it('should not include relevant rent details for premium of 150000', function() {

        var leaseholdHighPremData = angular.copy(leaseHoldNonResRentsUnder2kData);
        leaseholdHighPremData.premium = 150000;

        var leaseholdHighPremRequest = angular.copy(leaseHoldNonResRentsUnder2kRequest);
        leaseholdHighPremRequest.premium = 150000;
        delete leaseholdHighPremRequest.relevantRentDetails;

        expect(service.constructCalculationRequest(leaseholdHighPremData)).toEqual(leaseholdHighPremRequest);
    });

    it('should not include relevant rent details for one year rent of 2000', function() {

        var leaseholdHighRentData = angular.copy(leaseHoldNonResRentsUnder2kData);
        leaseholdHighRentData.year4Rent = 2000;
        leaseholdHighRentData.highestRent = 2000;

        var leaseholdHighRentRequest = angular.copy(leaseHoldNonResRentsUnder2kRequest);
        leaseholdHighRentRequest.leaseDetails.year4Rent = 2000;
        leaseholdHighRentRequest.highestRent = 2000;
        delete leaseholdHighRentRequest.relevantRentDetails;

        expect(service.constructCalculationRequest(leaseholdHighRentData)).toEqual(leaseholdHighRentRequest);
    });

    it('should not include relevant rent details for a freehold property', function() {

        var freeholdData = angular.copy(leaseHoldNonResRentsUnder2kData);
        freeholdData.holdingType = "Freehold";

        var freeholdRequest = angular.copy(leaseHoldNonResRentsUnder2kRequest);
        freeholdRequest.holdingType = "Freehold";
        delete freeholdRequest.leaseDetails;
        delete freeholdRequest.relevantRentDetails;

        expect(service.constructCalculationRequest(freeholdData)).toEqual(freeholdRequest);
    });

    var ftbData = angular.copy(baseLeaseholdData);
    ftbData.effectiveDate = new Date(2019, 10, 30);
    ftbData.effectiveDateDay = 30;
    ftbData.effectiveDateMonth = 11;
    ftbData.effectiveDateYear = 2019;
    ftbData.twoOrMoreProperties = "No";
    delete ftbData.replaceMainResidence;

    var ftbRequest = angular.copy(baseLeaseholdRequest);
    ftbRequest.effectiveDateDay = 30;
    ftbRequest.effectiveDateMonth = 11;
    ftbRequest.effectiveDateYear = 2019;
    ftbRequest.propertyDetails = {
      individual : "Yes",
      twoOrMoreProperties : "No"
    };

    it('should include first time buyer details for a first time, main residence property', function() {

        var ftbYesData = angular.copy(ftbData);
        ftbYesData.ownedOtherProperties = "No";
        ftbYesData.mainResidence = "Yes";
        ftbYesData.sharedOwnership = "No";

        var ftbYesRequest = angular.copy(ftbRequest);
        ftbYesRequest.firstTimeBuyer = "Yes";
        ftbYesRequest.propertyDetails.sharedOwnership = "No";

        expect(service.constructCalculationRequest(ftbYesData)).toEqual(ftbYesRequest);
    });

    it('should include first time buyer details for a first time, non-main residence property', function() {

        var ftbNonMainResData = angular.copy(ftbData);
        ftbNonMainResData.ownedOtherProperties = "No";
        ftbNonMainResData.mainResidence = "No";
        delete ftbNonMainResData.sharedOwnership;

        var ftbNonMainResRequest = angular.copy(ftbRequest);
        ftbNonMainResRequest.firstTimeBuyer = "No";

        expect(service.constructCalculationRequest(ftbNonMainResData)).toEqual(ftbNonMainResRequest);
    });

    it('should include first time buyer details for a non-first time buyer', function() {

        var ftbNotFirstPropertyData = angular.copy(ftbData);
        ftbNotFirstPropertyData.ownedOtherProperties = "Yes";
        delete ftbNotFirstPropertyData.sharedOwnership;

        var ftbNotFirstPropertyRequest = angular.copy(ftbRequest);
        ftbNotFirstPropertyRequest.firstTimeBuyer = "No";

        expect(service.constructCalculationRequest(ftbNotFirstPropertyData)).toEqual(ftbNotFirstPropertyRequest);
    });

    it('should not include first time buyer details for a non-residential property', function() {

        var ftbNonResidentialPropertyData = angular.copy(ftbData);
        ftbNonResidentialPropertyData.propertyType = "Non-residential";
        ftbNonResidentialPropertyData.ownedOtherProperties = "Yes";

        var ftbNonResidentialPropertyRequest = angular.copy(ftbRequest);
        ftbNonResidentialPropertyRequest.propertyType = "Non-residential";
        delete ftbNonResidentialPropertyRequest.propertyDetails;

        expect(service.constructCalculationRequest(ftbNonResidentialPropertyData)).toEqual(ftbNonResidentialPropertyRequest);
    });

    it('should not include first time buyer details for a non-individual property', function() {

        var ftbNonIndividualPropertyData = angular.copy(ftbData);
        ftbNonIndividualPropertyData.individual = "No";
        ftbNonIndividualPropertyData.ownedOtherProperties = "Yes";

        var ftbNonIndividualPropertyRequest = angular.copy(ftbRequest);
        ftbNonIndividualPropertyRequest.propertyDetails = {
          individual : "No"
        };

        expect(service.constructCalculationRequest(ftbNonIndividualPropertyData)).toEqual(ftbNonIndividualPropertyRequest);
    });

    it('should not include first time buyer details for an individual with two or more properties', function() {

        var ftb2OrMorePropertiesData = angular.copy(ftbData);
        ftb2OrMorePropertiesData.individual = "Yes";
        ftb2OrMorePropertiesData.twoOrMoreProperties = "Yes";
        ftb2OrMorePropertiesData.replaceMainResidence = "Yes";
        ftb2OrMorePropertiesData.ownedOtherProperties = "Yes";

        var ftb2OrMorePropertiesRequest = angular.copy(ftbRequest);
        ftb2OrMorePropertiesRequest.propertyDetails = {
          individual : "Yes",
          twoOrMoreProperties : "Yes",
          replaceMainResidence : "Yes"
        };

        expect(service.constructCalculationRequest(ftb2OrMorePropertiesData)).toEqual(ftb2OrMorePropertiesRequest);
    });

    it('should not include first time buyer details for a date of 21/11/2017', function() {

        var ftbEarlyData = angular.copy(ftbData);
        ftbEarlyData.effectiveDate = new Date(2017, 10, 21);
        ftbEarlyData.effectiveDateDay = 21;
        ftbEarlyData.effectiveDateMonth = 11;
        ftbEarlyData.effectiveDateYear = 2017;
        ftbEarlyData.ownedOtherProperties = "Yes";

        var ftbEarlyRequest = angular.copy(ftbRequest);
        ftbEarlyRequest.effectiveDateDay = 21;
        ftbEarlyRequest.effectiveDateMonth = 11;
        ftbEarlyRequest.effectiveDateYear = 2017;

        expect(service.constructCalculationRequest(ftbEarlyData)).toEqual(ftbEarlyRequest);
    });

    it('should include first time buyer details for a date of 22/11/2017', function() {

        var ftbFirstDayData = angular.copy(ftbData);
        ftbFirstDayData.effectiveDate = new Date(2017, 10, 22);
        ftbFirstDayData.effectiveDateDay = 22;
        ftbFirstDayData.effectiveDateMonth = 11;
        ftbFirstDayData.effectiveDateYear = 2017;
        ftbFirstDayData.ownedOtherProperties = "Yes";

        var ftbFirstDayRequest = angular.copy(ftbRequest);
        ftbFirstDayRequest.effectiveDateDay = 22;
        ftbFirstDayRequest.effectiveDateMonth = 11;
        ftbFirstDayRequest.effectiveDateYear = 2017;
        ftbFirstDayRequest.firstTimeBuyer = "No";

        expect(service.constructCalculationRequest(ftbFirstDayData)).toEqual(ftbFirstDayRequest);
    });

      it('should include first time buyer details for a first time, main-residence, shared ownership and current value', function() {

          var ftbSharedOwnership = angular.copy(ftbData);
          ftbSharedOwnership.ownedOtherProperties = "No";
          ftbSharedOwnership.mainResidence = "Yes";
          ftbSharedOwnership.sharedOwnership = "Yes";
          ftbSharedOwnership.currentValue = "Yes";

          var ftbSharedOwnershipResRequest = angular.copy(ftbRequest);
          ftbSharedOwnershipResRequest.firstTimeBuyer = "Yes";
          ftbSharedOwnershipResRequest.propertyDetails.sharedOwnership = "Yes";
          ftbSharedOwnershipResRequest.propertyDetails.currentValue = "Yes";

          expect(service.constructCalculationRequest(ftbSharedOwnership)).toEqual(ftbSharedOwnershipResRequest);
      });

      it('should include first time buyer details for a first time, main-residence, shared ownership and should not include current value', function() {

          var ftbSharedOwnership = angular.copy(ftbData);
          ftbSharedOwnership.ownedOtherProperties = "No";
          ftbSharedOwnership.mainResidence = "Yes";
          ftbSharedOwnership.sharedOwnership = "Yes";
          ftbSharedOwnership.currentValue = "No";

          var ftbSharedOwnershipResRequest = angular.copy(ftbRequest);
          ftbSharedOwnershipResRequest.firstTimeBuyer = "Yes";
          ftbSharedOwnershipResRequest.propertyDetails.sharedOwnership = "Yes";
          ftbSharedOwnershipResRequest.propertyDetails.currentValue = "No";

          expect(service.constructCalculationRequest(ftbSharedOwnership)).toEqual(ftbSharedOwnershipResRequest);
      });
  });
}());
