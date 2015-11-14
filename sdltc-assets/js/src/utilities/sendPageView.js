(function() {
    "use strict";
    
	// sends virtual page view to Google Analytics
	//var sendPageView = function(pageName){
	module.exports = function(pageName){
	    ga('set', 'page', '/calculate-stamp-duty-land-tax/' + pageName);
	    ga('send', 'pageview', { 'anonymizeIp': true });
	};
}());	 		 
