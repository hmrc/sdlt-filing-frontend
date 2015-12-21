(function() {
    "use strict";

	var angular = require("angular-wrapper");
	require("./routes/index");
	require("./controllers/index");
	require("./services/index");
	require("./filters/index");

	module.exports = angular.module('sdltc', [
	    'sdltc.services',
	    'sdltc.routes',
	    'sdltc.filters',
	    'sdltc.controllers',
	    'sdltc-templates'
	]);
}());
