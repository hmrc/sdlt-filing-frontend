(function() {
    "use strict";

	var angular = require("angular-wrapper");
	require("./calc-module");

	window.name = "NG_DEFER_BOOTSTRAP!";

	angular.element().ready(function () {
	    angular.bootstrap(document.documentElement, ['calc']);
	});
}());
