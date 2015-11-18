(function(){
    'use strict';

    var routesModule = require("./module");

    routesModule.config(['$routeProvider', function($routeProvider) {
        $routeProvider

            .when('/intro', {
                templateUrl : 'intro.html',
                controller  : 'introController',
                reloadOnSearch: false
            })

            .when('/holding', {
                templateUrl : 'holding.html',
                controller  : 'holdingController',
                reloadOnSearch: false
            })

            .when('/property', {
                templateUrl : 'property.html',
                controller  : 'propertyController',
                reloadOnSearch: false
            })

            .when('/date', {
                templateUrl : 'date.html',
                controller  : 'dateController',
                reloadOnSearch: false
            })

            .when('/purchase-price', {
                templateUrl : 'purchase-price.html',
                controller  : 'purchasePriceController',
                reloadOnSearch: false
            })

            .when('/lease-dates', {
                templateUrl : 'lease-dates.html',
                controller  : 'leaseDatesController',
                reloadOnSearch: false
            })

            .when('/premium', {
                templateUrl : 'premium.html',
                controller  : 'premiumController',
                reloadOnSearch: false
            })

            .when('/rent', {
                templateUrl : 'rent.html',
                controller  : 'rentController',
                reloadOnSearch: false
            })

            .when('/relevant-rent', {
                templateUrl : 'relevant-rent.html',
                controller  : 'relevantRentController',
                reloadOnSearch: false
            })

            .when('/summary', {
                templateUrl : 'summary.html',
                controller  : 'summaryController',
                reloadOnSearch: false
            })

            .when('/result', {
                templateUrl : 'result.html',
                controller  : 'resultController',
                reloadOnSearch: false
            })

            .when('/detail', {
                templateUrl : 'detail.html',
                controller  : 'detailController',
                reloadOnSearch: false
            })

            .when('/print', {
                templateUrl : 'print.html',
                controller  : 'printController',
                reloadOnSearch: false
            })


            // unsupported url, redirect to intro page
            .otherwise({redirectTo:'/intro'});

    }]);
}());
