(function() {
    "use strict";

	var angular = require("angular");
	require("./calc-module");

	window.name = "NG_DEFER_BOOTSTRAP!";

	angular.element().ready(function () {
	    angular.bootstrap(document, ['calc']);
	});
}());
