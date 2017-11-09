(function(){
    'use strict';

    var routesModule = require("./module");

    routesModule.config(['$routeProvider', function($routeProvider) {
        $routeProvider

            .when('/intro', {
                title : 'Calculate Stamp Duty Land Tax (SDLT)',
                templateUrl : 'intro.html',
                controller  : 'introController',
                reloadOnSearch: false
            })

            .when('/holding', {
                title : 'Freehold or leasehold',
                templateUrl : 'holding.html',
                controller  : 'holdingController',
                reloadOnSearch: false
            })

            .when('/property', {
                title : 'Residential or non-residential',
                templateUrl : 'property.html',
                controller  : 'propertyController',
                reloadOnSearch: false
            })

            .when('/date', {
                title : 'Effective date of transaction',
                templateUrl : 'date.html',
                controller  : 'dateController',
                reloadOnSearch: false
            })

            .when('/purchase-price', {
                title : 'Purchase price',
                templateUrl : 'purchase-price.html',
                controller  : 'purchasePriceController',
                reloadOnSearch: false
            })

            .when('/purchaser', {
                title : 'Status of purchaser',
                templateUrl : 'purchaser.html',
                controller  : 'purchaserController',
                reloadOnSearch: false
            })

            .when('/additional-property', {
                title : 'Additional residential properties',
                templateUrl : 'additional-property.html',
                controller  : 'additionalPropertyController',
                reloadOnSearch: false
            })

            .when('/lease-dates', {
                title : 'Lease dates',
                templateUrl : 'lease-dates.html',
                controller  : 'leaseDatesController',
                reloadOnSearch: false
            })

            .when('/premium', {
                title : 'Lease premium',
                templateUrl : 'premium.html',
                controller  : 'premiumController',
                reloadOnSearch: false
            })

            .when('/rent', {
                title : 'Rent',
                templateUrl : 'rent.html',
                controller  : 'rentController',
                reloadOnSearch: false
            })

            .when('/exchange-contracts', {
                title : 'Exchange of contracts',
                templateUrl : 'exchange-contracts.html',
                controller : 'exchangeContractsController',
                reloadOnSearch : false
            })

            .when('/relevant-rent', {
                title : 'Relevant rental figure',
                templateUrl : 'relevant-rent.html',
                controller  : 'relevantRentController',
                reloadOnSearch: false
            })

            .when('/summary', {
                title : 'Check your answers',
                templateUrl : 'summary.html',
                controller  : 'summaryController',
                reloadOnSearch: false
            })

            .when('/result', {
                title : 'Result',
                templateUrl : 'result.html',
                controller  : 'resultController',
                reloadOnSearch: false
            })

            .when('/detail', {
                title : 'Detailed calculation',
                templateUrl : 'detail.html',
                controller  : 'detailController',
                reloadOnSearch: false
            })

            .when('/print', {
                title : 'Summary',
                templateUrl : 'print.html',
                controller  : 'printController',
                reloadOnSearch: false
            })
            .when('/demo-response', {
                title : 'demo',
                templateUrl : 'demo.html',
                controller  : 'demoController',
                reloadOnSearch: false
            })

            // unsupported url, redirect to intro page
            .otherwise({redirectTo:'/intro'});

    }]);

    routesModule.run(['$rootScope', function($rootScope) {
        $rootScope.$on('$routeChangeSuccess', function (event, current, previous) {
            if (current.$$route !== undefined) $rootScope.title = current.$$route.title;
        });
    }]);

}());
