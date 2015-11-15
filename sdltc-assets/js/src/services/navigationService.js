(function() {
    "use strict";

	var app = require("./module");

	var navigationService = function() {

		var logView = function(pageName) {
    		ga('set', 'page', '/calculate-stamp-duty-land-tax/' + pageName);
    		ga('send', 'pageview', { 'anonymizeIp': true });
    		return pageName;
    	};

        var redirectToSummary = function(locationService) {
            locationService.path('summary');
        };

        var redirectToNext = function(locationService, nextView) {
            locationService.hash(null).path(nextView);
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
                if (model.propertyType === 'Non-residential' && model.premium < 150000) {
                    redirectToNext(locationService, 'relevant-rent');
                } 
                else {
                    redirectToNext(locationService, 'summary');
                } 
            }
             else if (currentView === 'relevant-rent') {
                redirectToNext(locationService, 'summary');
            }
    	};

	    return {
	    	logView : logView,
	    	next : gotoNextView
	    };
	};

	app.service('navigationService', navigationService);

}());
