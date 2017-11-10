(function() {
  "use strict";

  var app = require("./module");

  app.service('dataMarshallingService', function(){

    var validator = require("../utilities/validator")();

    var constructCalculationRequest = function(data){
      var model = {};

      model.holdingType = data.holdingType;
      model.propertyType = data.propertyType;
      model.effectiveDateDay = data.effectiveDateDay;
      model.effectiveDateMonth = data.effectiveDateMonth;
      model.effectiveDateYear = data.effectiveDateYear;
      model.highestRent = data.highestRent;
      model.premium = data.premium;

      if(data.propertyType === 'Residential' && data.effectiveDate >= new Date('April 1, 2016')) {
        model.propertyDetails = constructPropertyDetails(data);
      }

      if(data.holdingType === "Leasehold") {
        model.leaseDetails = constructLeaseDetails(data);
      }

      if(data.holdingType === "Leasehold" && data.propertyType === 'Non-residential' && data.premium < 150000 && validator.checkAllRentsBelow2000(data)) {
        if(data.effectiveDate >= new Date('March 16, 2016')) {
          model.relevantRentDetails = constructRelevantRentDetails(data, true);
        } else {
          model.relevantRentDetails = constructRelevantRentDetails(data, false);
        }
      }

      return model;
    };

    function constructPropertyDetails(data) {
      var propertyDetails = {};
        propertyDetails.individual = data.individual;
        if (data.individual === 'Yes') {
          propertyDetails.twoOrMoreProperties = data.twoOrMoreProperties;
          if (data.twoOrMoreProperties === 'Yes') {
            propertyDetails.replaceMainResidence = data.replaceMainResidence;
          }
        }
        return propertyDetails;
    }

    function constructLeaseDetails(data) {
      var leaseDetails = {};
      leaseDetails.startDateDay = data.startDateDay;
      leaseDetails.startDateMonth = data.startDateMonth;
      leaseDetails.startDateYear = data.startDateYear;
      leaseDetails.endDateDay = data.endDateDay;
      leaseDetails.endDateMonth = data.endDateMonth;
      leaseDetails.endDateYear = data.endDateYear;
      leaseDetails.leaseTerm = data.leaseTerm;

      var rentsToInclude = yearsOfRentToInclude(data.leaseTerm);
      leaseDetails.year1Rent = data.year1Rent;
      if(rentsToInclude >= 2) {leaseDetails.year2Rent = data.year2Rent;}
      if(rentsToInclude >= 3) {leaseDetails.year3Rent = data.year3Rent;}
      if(rentsToInclude >= 4) {leaseDetails.year4Rent = data.year4Rent;}
      if(rentsToInclude >= 5) {leaseDetails.year5Rent = data.year5Rent;}
      return leaseDetails;
    }

    function yearsOfRentToInclude(leaseTerm) {
      if(leaseTerm.years >= 5) {
        return 5;
      } else if (leaseTerm.days > 0) {
        return leaseTerm.years + 1;
      } else {
        return leaseTerm.years;
      }
    }

    function constructRelevantRentDetails(data, includeFilters) {
      var relRentDetails = {};
      if(!includeFilters) {
        relRentDetails.relevantRent = data.relevantRent;
      } else {
        relRentDetails.contractPre201603 = data.contractPre201603;
        if(data.contractPre201603 === "Yes") {
          relRentDetails.contractVariedPost201603 = data.contractVariedPost201603;
          if(data.contractVariedPost201603 === "No") {
            relRentDetails.relevantRent = data.relevantRent;
          }
        }
      }
      return relRentDetails;
    }

    return {
      constructCalculationRequest : constructCalculationRequest
    };
  });
}());
