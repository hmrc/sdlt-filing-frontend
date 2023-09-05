(function() {
    "use strict";

	var angular = require("angular");
	require("./routes/index");
	require("./controllers/index");
	require("./services/index");
	require("./filters/index");

	module.exports = angular.module('calc', [
	    'calc.services',
	    'calc.routes',
	    'calc.filters',
	    'calc.controllers',
	    'calc-templates',
		'ngSanitize'
	]);
}());
