(function() {
    "use strict";

	var app = require("./module");

	var navigationService = function() {

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
            else if (currentView === 'purchase-price') {
                redirectToNext(locationService, 'summary');
            }
            else if (currentView === 'lease-dates') {
                redirectToNext(locationService, 'premium');
            }
            else if (currentView === 'premium') {
                redirectToNext(locationService, 'rent');
            }
            else if (currentView === 'rent') {
                var validator = require("../utilities/validator")();
                var checkRelevant = validator.relevantRentCheck([model.year1Rent, model.year2Rent, model.year3Rent, model.year4Rent, model.year5Rent]);
                //if (model.propertyType === 'Non-residential' && model.premium < 150000 && !(model.year1Rent >= 2000 || model.year2Rent >= 2000 || model.year3Rent >= 2000 || model.year4Rent >= 2000 || model.year5Rent >= 2000)) {
                if (model.propertyType === 'Non-residential' && model.premium < 150000 && checkRelevant) {
                    redirectToNext(locationService, 'relevant-rent');
                } 
                else {
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
