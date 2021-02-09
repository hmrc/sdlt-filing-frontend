(function() {
    "use strict";

	var app = require("./module");

	var navigationService = function() {

        var validator = require("../utilities/validator")();
		var logView = function(pageName) {
    		ga('set', 'page', '/calculate-stamp-duty-land-tax/' + pageName);
    		ga('send', 'pageview', { 'anonymizeIp': true });
    		return pageName;
    	};

        var startNow = function(locationService) {
            locationService.path('holding');
        };

        var redirectToSummary = function(locationService) {
            locationService.path('summary');
        };

        var redirectToNext = function(locationService, nextView) {
            locationService.hash(null).path(nextView);
        };

        var gotoPrintView = function(model, locationService) {
            locationService.path('print');
        };

        var gotoDetailsView = function(model, locationService) {
            locationService.path('detail');
        };

    	var gotoNextView = function(currentView, model, locationService) {

            if (currentView === 'holding') {
				redirectToNext(locationService, 'property');
			}
            else if (currentView === 'property') {
                redirectToNext(locationService, 'date');
            }
            else if (currentView === 'date') {
                if(model.propertyType === "Residential") {
                    var dateHelper = require("../utilities/dateHelper");
                    var effectiveDate = dateHelper.parseUIDate(model.effectiveDateYear, model.effectiveDateMonth, model.effectiveDateDay);

                    if(validator.isLessThanDate(effectiveDate, new Date(2016, 3, 1))) {
                        redirectBasedOnHoldingType(model, locationService);
                    } else if(validator.effectiveDateIsAfterMarch2021(effectiveDate)){
                        redirectToNext(locationService, 'non-uk-resident');
                    } else {
                        redirectToNext(locationService, 'purchaser');
                    }
                } else {
                    redirectBasedOnHoldingType(model, locationService);
                }
            }
            else if (currentView === 'non-uk-resident') {
                redirectToNext(locationService, 'purchaser');
            }
            else if (currentView === 'purchase-price') {
                redirectToNext(locationService, 'summary');
            }
            else if (currentView === 'purchaser') {
                if (model.individual === "Yes") {
                    redirectToNext(locationService, 'additional-property');
                } else {
                    redirectBasedOnHoldingType(model, locationService);
                }
            }
            else if (currentView === "additional-property") {
                redirectBasedOnFTBExclusionCriteria(model, locationService);
            }
            else if (currentView === "owned-other-properties") {
                if(model.ownedOtherProperties === "No") {
                    redirectToNext(locationService, 'main-residence');
                } else {
                    redirectBasedOnHoldingType(model, locationService);
                }
            }
            else if (currentView === "main-residence") {
                if(model.mainResidence === "Yes" && model.holdingType === "Leasehold") {
                    redirectToNext(locationService, 'shared-ownership');
                } else {
                redirectBasedOnHoldingType(model, locationService);
                }
            }
            else if (currentView === "shared-ownership") {
                if(model.sharedOwnership === "Yes") {
                    redirectToNext(locationService, 'current-value');
                } else {
                    redirectBasedOnHoldingType(model, locationService);
                }
            }
            else if (currentView === "current-value") {
                if(model.currentValue === "£500,000 or less") {
                    redirectToNext(locationService, 'market-value');
                } else {
                    redirectBasedOnHoldingType(model, locationService);
                }
            }
            else if (currentView === "market-value") {
                if(model.paySDLT === "Using market value election" || model.paySDLT === "Stages") {
                    redirectToNext(locationService, 'lease-dates');
                }
            }
            else if (currentView === 'lease-dates') {
                if(model.premium !== undefined && model.currentValue === '£500,000 or less' && model.sharedOwnership === 'Yes') {
                    redirectToNext(locationService, 'rent');
                } else {
                redirectToNext(locationService, 'premium');
                }
            }
            else if (currentView === 'premium') {
                redirectToNext(locationService, 'rent');
            }
            else if (currentView === 'rent') {
                var allRentsBelow2000 = validator.checkAllRentsBelow2000(model);
                if (model.propertyType === 'Non-residential' && model.premium < 150000 && allRentsBelow2000) {
                    if(model.effectiveDate > new Date(2016, 2, 16)) {
                        redirectToNext(locationService, 'exchange-contracts');
                    } else {
                        redirectToNext(locationService, 'relevant-rent');
                    }
                } 
                else {
                    redirectToNext(locationService, 'summary');
                }
            }
            else if(currentView === 'exchange-contracts') {
                if(model.contractPre201603 === 'Yes' && model.contractVariedPost201603 === 'No') {
                    redirectToNext(locationService, 'relevant-rent');
                } else {
                    redirectToNext(locationService, 'summary');
                }
            }
            else if (currentView === 'relevant-rent') {
                redirectToNext(locationService, 'summary');
            }
            else {
                redirectToNext(locationService, 'result');
            }
    	};

      function redirectBasedOnFTBExclusionCriteria(model, locationService) {
        if(model.propertyType === 'Residential' &&
          validator.effectiveDateWithinFTBRange(model.effectiveDate) &&
          model.individual === 'Yes' &&
          model.twoOrMoreProperties === 'No' &&
          (validator.effectiveDateIsBeforeJuly2020(model.effectiveDate) ||
           validator.effectiveDateIsAfterMarch2021(model.effectiveDate))
          ) {
            redirectToNext(locationService, 'owned-other-properties');
          } else {
            redirectBasedOnHoldingType(model, locationService);
          }
      }

      function redirectBasedOnHoldingType(model, locationService) {
          if (model.holdingType === 'Freehold') {
              redirectToNext(locationService, 'purchase-price');
          }
          else if (model.holdingType === 'Leasehold') {
              redirectToNext(locationService, 'lease-dates');
          }
          else {
              redirectToSummary(locationService);
          }
      }

	    return {
	    	logView : logView,
            startNow : startNow,
	    	next : gotoNextView,
            printView : gotoPrintView,
            viewDetails : gotoDetailsView 
	    };
	};

	app.service('navigationService', navigationService);

}());
