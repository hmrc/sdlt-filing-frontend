(function() {
    "use strict";

	var angular = require("angular-wrapper");
	require("./sdltc-module");

	window.name = "NG_DEFER_BOOTSTRAP!";

	angular.element().ready(function () {
	    angular.bootstrap(document, ['sdltc']);
	});
}());
