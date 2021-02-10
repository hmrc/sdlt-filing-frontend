/******/ (function(modules) { // webpackBootstrap
/******/ 	// The module cache
/******/ 	var installedModules = {};

/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {

/******/ 		// Check if module is in cache
/******/ 		if(installedModules[moduleId])
/******/ 			return installedModules[moduleId].exports;

/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = installedModules[moduleId] = {
/******/ 			exports: {},
/******/ 			id: moduleId,
/******/ 			loaded: false
/******/ 		};

/******/ 		// Execute the module function
/******/ 		modules[moduleId].call(module.exports, module, module.exports, __webpack_require__);

/******/ 		// Flag the module as loaded
/******/ 		module.loaded = true;

/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}


/******/ 	// expose the modules object (__webpack_modules__)
/******/ 	__webpack_require__.m = modules;

/******/ 	// expose the module cache
/******/ 	__webpack_require__.c = installedModules;

/******/ 	// __webpack_public_path__
/******/ 	__webpack_require__.p = "";

/******/ 	// Load entry module and return exports
/******/ 	return __webpack_require__(0);
/******/ })
/************************************************************************/
/******/ ([
/* 0 */
/***/ (function(module, exports, __webpack_require__) {

	module.exports = __webpack_require__(1);


/***/ }),
/* 1 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

		var angular = __webpack_require__(2);
		__webpack_require__(4);

		window.name = "NG_DEFER_BOOTSTRAP!";

		angular.element().ready(function () {
		    angular.bootstrap(document, ['calc']);
		});
	}());


/***/ }),
/* 2 */
/***/ (function(module, exports, __webpack_require__) {

	(function ()
	{
	    if (!this.__angular_wrapper_loaded__)
	    {
	        this.__angular_wrapper_loaded__ = true;
	        __webpack_require__(3);
	    }

	    module.exports = angular;
	})();

/***/ }),
/* 3 */
/***/ (function(module, exports) {

	module.exports = angular;

/***/ }),
/* 4 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

		var angular = __webpack_require__(2);
		__webpack_require__(5);
		__webpack_require__(8);
		__webpack_require__(38);
		__webpack_require__(63);

		module.exports = angular.module('calc', [
		    'calc.services',
		    'calc.routes',
		    'calc.filters',
		    'calc.controllers',
		    'calc-templates'
		]);
	}());


/***/ }),
/* 5 */
/***/ (function(module, exports, __webpack_require__) {

	(function(){
	    'use strict';

		__webpack_require__(6);
		
	}());


/***/ }),
/* 6 */
/***/ (function(module, exports, __webpack_require__) {

	(function(){
	    'use strict';

	    var routesModule = __webpack_require__(7);

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

	            .when('/owned-other-properties', {
	                title : 'Other property',
	                templateUrl : 'owned-other-properties.html',
	                controller  : 'ownedOtherPropertiesController',
	                reloadOnSearch: false
	            })

	            .when('/main-residence', {
	                title : 'Main residence',
	                templateUrl : 'main-residence.html',
	                controller  : 'mainResidenceController',
	                reloadOnSearch: false
	            })

	            .when('/shared-ownership', {
	                title : 'Shared ownership scheme',
	                templateUrl : 'shared-ownership.html',
	                controller  : 'sharedOwnershipController',
	                reloadOnSearch: false
	            })

	            .when('/current-value', {
	                title : 'Shared ownership scheme',
	                templateUrl : 'current-value.html',
	                controller  : 'currentValueController',
	                reloadOnSearch: false
	            })

	            .when('/market-value', {
	                title : 'Shared ownership scheme',
	                templateUrl : 'market-value.html',
	                controller  : 'marketValueController',
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
	                title : 'SDLT calculator',
	                templateUrl : 'print.html',
	                controller  : 'printController',
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


/***/ }),
/* 7 */
/***/ (function(module, exports, __webpack_require__) {

	(function(){
	    'use strict';

		var angular = __webpack_require__(2);

		module.exports = angular.module("calc.routes", ['ngRoute']);
		
	}());


/***/ }),
/* 8 */
/***/ (function(module, exports, __webpack_require__) {

	(function(){
	  'use strict';

	  __webpack_require__(9);
	  __webpack_require__(11);
	  __webpack_require__(13);
	  __webpack_require__(14);
	  __webpack_require__(16);
	  __webpack_require__(17);
	  __webpack_require__(18);
	  __webpack_require__(19);
	  __webpack_require__(20);
	  __webpack_require__(21);
	  __webpack_require__(22);
	  __webpack_require__(24);
	  __webpack_require__(26);
	  __webpack_require__(27);
	  __webpack_require__(28);
	  __webpack_require__(29);
	  __webpack_require__(30);
	  __webpack_require__(31);
	  __webpack_require__(32);
	  __webpack_require__(34);
	  __webpack_require__(36);
	  __webpack_require__(37);

	}());


/***/ }),
/* 9 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";
	    var app = __webpack_require__(10);
	    // define the main controller
	    var mainController = function($scope, $window, loggingService, cookieService) {

	        var expanded = false;

	        $scope.jumpTo = function(id) {
	            var selector = '#' + id;
	            $(selector).focus();
	        };

	        $scope.getFeedbackSurveyClass = function(){
	                  if(onResultPage()){
	                    return "feedback-survey--show";
	                  }else{
	                    return "visually-hidden";
	                  }
	                };

	        function onResultPage() {
	            var page = $window.location.href.split("/").slice(-1)[0];
	            return page == "result";
	        }


	        $scope.optionalHelp = {};
	        
	        $scope.toggleHelp = function(helpId, summary) {
	            var currentValue = $scope.optionalHelp[helpId] || false,
	                action = currentValue ? 'hide' : 'show';
	            
	            $scope.optionalHelp[helpId] = !currentValue;
	            loggingService.logEvent('help', action, summary);
	        };

	        $scope.displayHelp = function(helpId) {
	            return $scope.optionalHelp[helpId];
	        };

	        $scope.getHelpGA = function() {
	            var action = expanded ? "hide": "show";
	            loggingService.logEvent('getHelp', action, "/calculate-stamp-duty-land-tax/"+document.location.href.split('/').pop());
	            expanded = !expanded;
	        };

	        $scope.addFocusToLabel = function() {
	            var label = $(this).closest('label')[0];
	            $(label).addClass('add-focus selected');
	        };

	        $scope.removeFocusFromLabel = function() {
	            $(this).closest('label').removeClass('add-focus');

	            if (!$(this).is(':checked')) {
	                $(this).closest('label').removeClass('selected');
	            }
	        };

	        $scope.toggleFocus = function() {
	            if ($(this).attr('type') === 'radio') {
	                $(this).closest('label').siblings().removeClass('add-focus selected');
	            }

	            $(this).closest('label').toggleClass('add-focus selected', $(this).prop('checked'));
	        };

	        // re-apply radio/checkbox styling
	        $('#main').on(
	            'focus click',
	            'label.block-label input[type=radio], label.block-label input[type=checkbox]',
	            $scope.addFocusToLabel
	        ).on(
	            'change',
	            'label.block-label input[type=radio], label.block-label input[type=checkbox]',
	            $scope.toggleFocus
	        ).on(
	            'blur',
	            'label.block-label input[type=radio], label.block-label input[type=checkbox]',
	            $scope.removeFocusFromLabel
	        );

	    };

	    // register the main controller
	    app.controller('mainController', ['$scope', '$window', 'loggingService', 'cookieService', mainController]);
	}());


/***/ }),
/* 10 */
/***/ (function(module, exports, __webpack_require__) {

	(function(){
	    'use strict';

		var angular = __webpack_require__(2);

		module.exports = angular.module("calc.controllers",[]);
	}());


/***/ }),
/* 11 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(10);

	    var additionalPropertyController = function($scope, $location, $anchorScroll, dataService, additionalPropertyValidationService, navigationService, loggingService) {
	        
	        var init = __webpack_require__(12);
	        init($scope, $location, $anchorScroll, 'additional-property', dataService, additionalPropertyValidationService, navigationService);

	        $scope.beforeUpdateModel = function() {
	            if($scope.data.twoOrMoreProperties === "No") {
	                loggingService.logEvent('decision', 'submit', "AdditonalProperty.SingleProperty");
	            } else {
	                if($scope.data.replaceMainResidence === "Yes") {
	                    loggingService.logEvent('decision', 'submit', "AdditonalProperty.MultiplePoperties.MainResidence");
	                } else {
	                    loggingService.logEvent('decision', 'submit', "AdditonalProperty.MultiplePoperties.NotMainResidence");
	                }
	            }
	        };

	    };

	    app.controller('additionalPropertyController', ['$scope', '$location', '$anchorScroll', 'dataService', 'additionalPropertyValidationService', 'navigationService', 'loggingService', additionalPropertyController ]);
	}());


/***/ }),
/* 12 */
/***/ (function(module, exports) {

	(function() {
	    "use strict";

	    // performs standard controller setup
	    module.exports = function(scope, location, scrollToHash, page, dataService, validationService, navigationService){

	        // log Google Analytics hit
	        navigationService.logView(page);

	        // copy dataService data model into scope variable 'data'
	        scope.data = dataService.getModel();

	        // set scope variable 'state' to have an object with a hasError function that returns false
	        scope.state = {
	            isValid: true,
	            hasError: function() {
	                // empty so that on page load no fieldset class is set
	                return "";
	            }
	        };

	        scope.jumpTo = function(id) {
	            if (location.hash() !== id) {
	                location.hash(id);
	            }
	            scrollToHash();
	            $('#' + id).focus();
	        };
	        
	        // hide error summary
			$('#pageErrors').hide();

	        // add submit method to scope
	        scope.submit = function() {
	            scope.state = validationService.validate(scope.data);

	            if (scope.state.isValid) {
	                if (angular.isDefined(scope.beforeUpdateModel)) {       
	                    scope.beforeUpdateModel();
	                }
	                dataService.updateModel(scope.data);
	                navigationService.next(page, scope.data, location);
	            } else {
	                $('#pageErrors').show().focus();
	            }
	        };
	    };
	}());


/***/ }),
/* 13 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(10);

	    var exchangeContractsController = function($scope, $location, $anchorScroll, dataService, exchangeContractsValidationService, navigationService, loggingService) {
	        
	        var init = __webpack_require__(12);
	        init($scope, $location, $anchorScroll, 'exchange-contracts', dataService, exchangeContractsValidationService, navigationService);

	        $scope.beforeUpdateModel = function() {
	            if($scope.data.contractPre201603 === "No") {
	                loggingService.logEvent('decision', 'submit', "ExchangeContracts.Post20160316");
	            } else {
	                if($scope.data.contractVariedPost201603 === "Yes") {
	                    loggingService.logEvent('decision', 'submit', "ExchangeContracts.Pre20160317.VariedAfter");
	                } else {
	                    loggingService.logEvent('decision', 'submit', "ExchangeContracts.Pre20160317.NotVariedAfter");
	                }
	            }
	        };

	    };

	    app.controller('exchangeContractsController', ['$scope', '$location', '$anchorScroll', 'dataService', 'exchangeContractsValidationService', 'navigationService', 'loggingService', exchangeContractsController ]);
	}());


/***/ }),
/* 14 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";
	    
	    var app = __webpack_require__(10);

	    var dateController = function($scope, $location, $anchorScroll, dataService, dateValidationService, navigationService) {
	        
	        var init = __webpack_require__(12);
	        init($scope, $location, $anchorScroll, 'date', dataService, dateValidationService, navigationService);

	        var dateHelper = __webpack_require__(15);
	        $scope.updateEffectiveDate = function() {
	            $scope.data.effectiveDate = dateHelper.parseUIDate($scope.data.effectiveDateYear, $scope.data.effectiveDateMonth, $scope.data.effectiveDateDay);
	        };
	    };

	    app.controller('dateController', ['$scope', '$location', '$anchorScroll', 'dataService', 'dateValidationService', 'navigationService', dateController ]);
	}());


/***/ }),
/* 15 */
/***/ (function(module, exports) {

	(function() {
	    "use strict";

	    function isValidDate(date, year, month, day) {

	        var failedNumberCheck,
	            failedDateCheck,
	            yearPattern = /^\d{4}$/;

	        failedNumberCheck = isNaN(date);
	        failedDateCheck = date == 'Invalid Date';

	        if (failedNumberCheck || failedDateCheck) {
	            return false;
	        }

	        if (!yearPattern.test(year)) {
	            return false;
	        }

	        if (date.getFullYear() !== parseInt(year) || 
	            date.getMonth() !== (parseInt(month) - 1) || 
	            date.getDate() !== parseInt(day)) {
	            return false;
	        }

	        return true;
	    }

	    var parseUIDate = function parseUIDate(year, month, day) {

	        var dateString,
	            date;

	        year = year || 'empty';
	        month = month || 'empty';
	        day = day || 'empty';

	        dateString = year + month + day;

	        // mandatory check
	        if (dateString === 'emptyemptyempty') {
	            return '';
	        }

	        date = new Date(year, month - 1, day);

	        return isValidDate(date, year, month, day) ? date : 'bad date';
	    };

	    var calculateTermOfLease = function calculateTermOfLease(effectiveDate, leaseStartDate, leaseEndDate) {

	        var startDate = leaseStartDate;
	        if (effectiveDate > leaseStartDate) {
	            startDate = effectiveDate;
	        }
	        var endDate = leaseEndDate;

	        var numYears = 0;
	        var numDays = 0;
	        var numDaysInPartialYear = 0;

	        function getEndOfFutureYear(startDate, numYears) {
	            var futureDate = new Date(startDate.getFullYear() + numYears, startDate.getMonth(), startDate.getDate());
	            futureDate.setDate(futureDate.getDate() -1);
	            return futureDate;
	        }

	        function is29thFeb(date) {
	          return date.getDate() == 29 && date.getMonth() == 1;
	        }

	        numYears = 1;
	        var comparisonDate = getEndOfFutureYear(startDate, numYears);
	        while (comparisonDate <= endDate) {
	            numYears++;
	            comparisonDate = getEndOfFutureYear(startDate, numYears);
	        }
	        // we went past the end date so need to go back 1 year
	        numYears--;

	        // count the number of partial days, i.e. keep adding 1 day till we get past the end date
	        numDays = 1;

	       comparisonDate = getEndOfFutureYear(startDate, numYears);

	       if (is29thFeb(startDate) && numYears % 4 != 0) {
	        comparisonDate.setDate(comparisonDate.getDate() -1);
	        }

	        comparisonDate.setDate(comparisonDate.getDate() + 1);
	        while (comparisonDate <= endDate) {
	            numDays += 1;
	            comparisonDate.setDate(comparisonDate.getDate() + 1);
	        }
	        // we went past the end date so need to go back 1 day
	        numDays--;

	        // need to calculate number of days in partial year (is it 365 or 366)
	        if (numDays > 0) {
	            var partialYearEndDate = getEndOfFutureYear(startDate, numYears + 1);
	            // set comparison date to end date of last full year in term
	            comparisonDate = getEndOfFutureYear(startDate, numYears);
	            numDaysInPartialYear = 1;
	            comparisonDate.setDate(comparisonDate.getDate() + 1);
	            while (comparisonDate <= partialYearEndDate) {
	                numDaysInPartialYear += 1;
	                comparisonDate.setDate(comparisonDate.getDate() + 1);
	            }
	            numDaysInPartialYear--;
	        }

	        var termOfLease = {
	            years : numYears,
	            days : numDays,
	            daysInPartialYear : numDaysInPartialYear
	        };
	        return termOfLease;
	    };

	    module.exports = {
	        parseUIDate : parseUIDate,
	        calculateTermOfLease : calculateTermOfLease
	    };
	}());


/***/ }),
/* 16 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(10);

	    var ownedOtherPropertiesController = function($scope, $location, $anchorScroll, dataService, ownedOtherPropertiesValidationService, navigationService, loggingService) {

	        var init = __webpack_require__(12);
	        init($scope, $location, $anchorScroll, 'owned-other-properties', dataService, ownedOtherPropertiesValidationService, navigationService);

	        $scope.beforeUpdateModel = function() {
	            loggingService.logEvent('decision', 'submit', $scope.data.ownedOtherProperties);
	        };

	    };

	    app.controller('ownedOtherPropertiesController', ['$scope', '$location', '$anchorScroll', 'dataService', 'ownedOtherPropertiesValidationService', 'navigationService', 'loggingService', ownedOtherPropertiesController ]);
	}());


/***/ }),
/* 17 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(10);

	    var mainResidenceController = function($scope, $location, $anchorScroll, dataService, mainResidenceValidationService, navigationService, loggingService) {

	        var init = __webpack_require__(12);
	        init($scope, $location, $anchorScroll, 'main-residence', dataService, mainResidenceValidationService, navigationService);

	        $scope.beforeUpdateModel = function() {
	            loggingService.logEvent('decision', 'submit', $scope.data.mainResidence);
	        };

	    };

	    app.controller('mainResidenceController', ['$scope', '$location', '$anchorScroll', 'dataService', 'mainResidenceValidationService', 'navigationService', 'loggingService', mainResidenceController ]);
	}());


/***/ }),
/* 18 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(10);

	    var sharedOwnershipController = function($scope, $location, $anchorScroll, dataService, sharedOwnershipValidationService, navigationService, loggingService) {

	        var init = __webpack_require__(12);
	        init($scope, $location, $anchorScroll, 'shared-ownership', dataService, sharedOwnershipValidationService, navigationService);

	        $scope.beforeUpdateModel = function() {
	            loggingService.logEvent('decision', 'submit', $scope.data.sharedOwnership);
	        };

	    };

	    app.controller('sharedOwnershipController', ['$scope', '$location', '$anchorScroll', 'dataService', 'sharedOwnershipValidationService', 'navigationService', 'loggingService', sharedOwnershipController ]);
	}());


/***/ }),
/* 19 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(10);

	    var currentValueController = function($scope, $location, $anchorScroll, dataService, currentValueValidationService, navigationService, loggingService) {

	        var init = __webpack_require__(12);
	        init($scope, $location, $anchorScroll, 'current-value', dataService, currentValueValidationService, navigationService);

	        $scope.beforeUpdateModel = function() {
	            loggingService.logEvent('decision', 'submit', $scope.data.currentValue);
	        };

	    };

	    app.controller('currentValueController', ['$scope', '$location', '$anchorScroll', 'dataService', 'currentValueValidationService', 'navigationService', 'loggingService', currentValueController ]);
	}());


/***/ }),
/* 20 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(10);

	    var marketValueController = function($scope, $location, $anchorScroll, dataService, marketValueValidationService, navigationService, loggingService) {

	        var init = __webpack_require__(12);
	        init($scope, $location, $anchorScroll, 'market-value', dataService, marketValueValidationService, navigationService);

	        $scope.beforeUpdateModel = function() {
	                if($scope.data.paySDLT === "Using market value election" && $scope.data.marketPropValue) {
	                    loggingService.logEvent('decision', 'submit', "MarketValue.UpfrontMarketValue");
	                }

	                if($scope.data.paySDLT === "Stages" && $scope.data.stagePropValue) {
	                    loggingService.logEvent('decision', 'submit', "MarketValue.StagesShareValue");
	                }

	            };
	        };

	    app.controller('marketValueController', ['$scope', '$location', '$anchorScroll', 'dataService', 'marketValueValidationService', 'navigationService', 'loggingService', marketValueController ]);
	}());


/***/ }),
/* 21 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";
	    
	    var app = __webpack_require__(10);

	    var holdingController = function($scope, $location, $anchorScroll, dataService, holdingValidationService, navigationService, loggingService) {
	        
	        var init = __webpack_require__(12);
	        init($scope, $location, $anchorScroll, 'holding', dataService, holdingValidationService, navigationService);

	        $scope.beforeUpdateModel = function() {
	        	loggingService.logEvent('decision', 'submit', $scope.data.holdingType);
	        };

	    };

	    app.controller('holdingController', ['$scope', '$location', '$anchorScroll', 'dataService', 'holdingValidationService', 'navigationService', 'loggingService', holdingController ]);
	}());


/***/ }),
/* 22 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(10);

	    var introController = function($scope, $location, $anchorScroll, dataService, navigationService) {

	        var init = __webpack_require__(23);
	        init($scope, $location, $anchorScroll, 'intro', dataService, navigationService);

	        $scope.startNow = function() {
	            dataService.updateModel({});
	            navigationService.startNow($location);
	        };

	    };

	    app.controller('introController', ['$scope', '$location', '$anchorScroll', 'dataService' , 'navigationService', introController ]);
	}());


/***/ }),
/* 23 */
/***/ (function(module, exports) {

	(function() {
	    "use strict";

	    // performs standard controller setup
	    module.exports = function(scope, location, scrollToHash, page, dataService, navigationService){

	        // log Google Analytics hit
	        navigationService.logView(page);

	        // copy dataService data model into scope variable 'data'
	        scope.data = dataService.getModel();

	        scope.jumpTo = function(id) {
	            if (location.hash() !== id) {
	                location.hash(id);
	            }
	            scrollToHash();
	            $('#' + id).focus();
	        };

	        // add submit method to scope
	        scope.submit = function() {
	            navigationService.next(page, scope.data, location);
	        };
	    };
	}());


/***/ }),
/* 24 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";
	    
	    var app = __webpack_require__(10);

	    var leaseDatesController = function($scope, $location, $anchorScroll, dataService, leaseDatesValidationService, navigationService) {

	        var init = __webpack_require__(12);
	        init($scope, $location, $anchorScroll, 'lease-dates', dataService, leaseDatesValidationService, navigationService);

	        var dateHelper = __webpack_require__(15);
	        $scope.updateStartDate = function() {
	            $scope.data.startDate = dateHelper.parseUIDate($scope.data.startDateYear, $scope.data.startDateMonth, $scope.data.startDateDay);
	        };
	        $scope.updateEndDate = function() {
	            $scope.data.endDate = dateHelper.parseUIDate($scope.data.endDateYear, $scope.data.endDateMonth, $scope.data.endDateDay);
	        };
	        var rentFields = __webpack_require__(25);
	        $scope.beforeUpdateModel = function() {
	            $scope.data.leaseTerm = dateHelper.calculateTermOfLease($scope.data.effectiveDate, $scope.data.startDate, $scope.data.endDate);
	            var yearsToDisplay = rentFields().getFunctions($scope.data);
	            setDisplayYears(yearsToDisplay);

	        };

	        function setDisplayYears(yearsToDisplay) {

	            if(!yearsToDisplay.displayYearOneRent){
	                $scope.data.year1Rent = undefined;
	            }
	            if(!yearsToDisplay.displayYearTwoRent){
	                $scope.data.year2Rent = undefined;
	            }
	            if(!yearsToDisplay.displayYearThreeRent){
	                $scope.data.year3Rent = undefined;
	            }
	            if(!yearsToDisplay.displayYearFourRent){
	                $scope.data.year4Rent = undefined;
	            }
	            if(!yearsToDisplay.displayYearFiveRent){
	                $scope.data.year5Rent = undefined;
	            }
	        }
	        
	    };

	    app.controller('leaseDatesController', ['$scope', '$location', '$anchorScroll', 'dataService', 'leaseDatesValidationService', 'navigationService', leaseDatesController ]);
	}());


/***/ }),
/* 25 */
/***/ (function(module, exports) {

	(function() {
	    "use strict";

	    // decides whether rent fields should be displayed
	    module.exports = function(){
	        
	        var reqFieldsCompleted = function(data) {
	            return data.holdingType !== undefined && data.holdingType === 'Leasehold' && data.leaseTerm !== undefined;
	        };


	        var getFunctions = function(data) {
	            var displayYearOneRent = reqFieldsCompleted(data);

	            var displayYearTwoRent = reqFieldsCompleted(data) && (data.leaseTerm.years > 1 || (data.leaseTerm.years == 1 && data.leaseTerm.days > 0));

	            var displayYearThreeRent = reqFieldsCompleted(data) && (data.leaseTerm.years > 2 || (data.leaseTerm.years == 2 && data.leaseTerm.days > 0));

	            var displayYearFourRent = reqFieldsCompleted(data) && (data.leaseTerm.years > 3 || (data.leaseTerm.years == 3 && data.leaseTerm.days > 0));

	            var displayYearFiveRent = reqFieldsCompleted(data) && (data.leaseTerm.years > 4 || (data.leaseTerm.years == 4 && data.leaseTerm.days > 0));

	            return {
	                displayYearOneRent : displayYearOneRent,
	                displayYearTwoRent : displayYearTwoRent,
	                displayYearThreeRent : displayYearThreeRent,
	                displayYearFourRent : displayYearFourRent,
	                displayYearFiveRent : displayYearFiveRent
	            };
	        };

	        var addFunctionsToScope = function(scope) {
	            var functions = getFunctions(scope.data);

	            scope.displayYearOneRent = functions.displayYearOneRent;
	            scope.displayYearTwoRent = functions.displayYearTwoRent;
	            scope.displayYearThreeRent = functions.displayYearThreeRent;
	            scope.displayYearFourRent = functions.displayYearFourRent;
	            scope.displayYearFiveRent = functions.displayYearFiveRent;
	        };

	        return {
	            getFunctions : getFunctions,
	            addFunctionsToScope : addFunctionsToScope
	        };        

	    };

	}());


/***/ }),
/* 26 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";
	    
	    var app = __webpack_require__(10);

	    var premiumController = function($scope, $location, $anchorScroll, dataService, premiumValidationService, navigationService) {
	        
	        var init = __webpack_require__(12);
	        init($scope, $location, $anchorScroll, 'premium', dataService, premiumValidationService, navigationService);

	    	if($scope.data.propertyType === 'Residential') {
	    		$scope.showPremiumHelp = true;
	    	} else if ($scope.data.propertyType === 'Non-residential' && $scope.data.effectiveDate >= new Date(2016,2,17)) {
	    		$scope.showPremiumHelp = true;
	    	} else {
	    		$scope.showPremiumHelp = false;
	    	}

	    };


	    app.controller('premiumController', ['$scope', '$location', '$anchorScroll', 'dataService', 'premiumValidationService', 'navigationService', premiumController ]);
	}());


/***/ }),
/* 27 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(10);

	    var propertyController = function($scope, $location, $anchorScroll, dataService, propertyValidationService, navigationService, loggingService) {
	        
	        var init = __webpack_require__(12);
	        init($scope, $location, $anchorScroll, 'property', dataService, propertyValidationService, navigationService);

	        $scope.beforeUpdateModel = function() {
	            loggingService.logEvent('decision', 'submit', $scope.data.holdingType + '.' + $scope.data.propertyType);
	        };

	    };

	    app.controller('propertyController', ['$scope', '$location', '$anchorScroll', 'dataService', 'propertyValidationService', 'navigationService', 'loggingService', propertyController ]);
	}());


/***/ }),
/* 28 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";
	    
	    var app = __webpack_require__(10);

	    var purchasePriceController = function($scope, $location, $anchorScroll, dataService, purchasePriceValidationService, navigationService) {
	        
	        var init = __webpack_require__(12);
	        init($scope, $location, $anchorScroll, 'purchase-price', dataService, purchasePriceValidationService, navigationService);

	    };

	    app.controller('purchasePriceController', ['$scope', '$location', '$anchorScroll', 'dataService', 'purchasePriceValidationService', 'navigationService', purchasePriceController ]);
	}());


/***/ }),
/* 29 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";
	    
	    var app = __webpack_require__(10);

	    var purchaserController = function($scope, $location, $anchorScroll, dataService, purchaserValidationService, navigationService, loggingService) {
	        
	        var init = __webpack_require__(12);
	        init($scope, $location, $anchorScroll, 'purchaser', dataService, purchaserValidationService, navigationService);

	        $scope.beforeUpdateModel = function() {
	        	loggingService.logEvent('decision', 'submit', $scope.data.purchaserType);
	        };

	    };

	    app.controller('purchaserController', ['$scope', '$location', '$anchorScroll', 'dataService', 'purchaserValidationService', 'navigationService', 'loggingService', purchaserController ]);
	}());

/***/ }),
/* 30 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";
	    
	    var app = __webpack_require__(10);

	    var relevantRentController = function($scope, $location, $anchorScroll, dataService, relevantRentValidationService, navigationService) {
	        
	        var init = __webpack_require__(12);
	        init($scope, $location, $anchorScroll, 'relevant-rent', dataService, relevantRentValidationService, navigationService);

	    };

	    app.controller('relevantRentController', ['$scope', '$location', '$anchorScroll', 'dataService', 'relevantRentValidationService', 'navigationService', relevantRentController ]);
	}());


/***/ }),
/* 31 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";
	    
	    var app = __webpack_require__(10);

	    var rentController = function($scope, $location, $anchorScroll, dataService, rentValidationService, navigationService) {
	        
	        var init = __webpack_require__(12);
	        init($scope, $location, $anchorScroll, 'rent', dataService, rentValidationService, navigationService);

	        var rent = __webpack_require__(25);
	        rent = rent();
	        rent.addFunctionsToScope($scope);

	        // if they have missed required questions redirect to summary
	        if(!$scope.displayYearOneRent) {
	            $location.path('summary');
	        }

	    };

	    app.controller('rentController', ['$scope', '$location', '$anchorScroll', 'dataService', 'rentValidationService', 'navigationService', rentController ]);
	}());


/***/ }),
/* 32 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(10);

	    var resultController = function($scope, $location, $anchorScroll, dataService, modelValidationService, navigationService, dataMarshallingService, loggingService, $http) {

	        var init = __webpack_require__(23);
	        init($scope, $location, $anchorScroll, 'result', dataService, navigationService);

	        $scope.viewDetails = function(resultIndex, taxCalcIndex) {
	            $scope.data.resultIndex = resultIndex;
	            $scope.data.taxCalcIndex = taxCalcIndex;
	            dataService.updateModel($scope.data);
	            navigationService.viewDetails($scope.data, $location);
	        };

	        $scope.printView = function() {
	            navigationService.printView($scope.data, $location);
	        };

	        var validator = __webpack_require__(33)();
	        $scope.data.result = null;
	        $scope.responseReceived = false;
	        $scope.errorResponse = false;

	        var rent = __webpack_require__(25);
	        rent = rent().getFunctions($scope.data);
	        if (modelValidationService.validate($scope.data).isModelValid) {
	          var submission = dataMarshallingService.constructCalculationRequest($scope.data);
	          $http.post("/calculate-stamp-duty-land-tax/calculate", submission).
	             success(function(data, status, headers, config) {
	               $scope.data.result = data.result;
	               dataService.updateModel($scope.data);
	               $scope.responseReceived = true;
	             }).
	             error(function(data, status, headers, config) {
	               $scope.errorResponse = true;
	               $scope.responseReceived = true;
	               loggingService.logEvent("error", "calculation", "status: "+status+", server: "+headers('Server'));
	             });
	        }
	        else {
	            $location.path('summary');
	        }
	    };

	    app.controller('resultController', ['$scope', '$location', '$anchorScroll', 'dataService', 'modelValidationService', 'navigationService', 'dataMarshallingService', 'loggingService', '$http', resultController ]);
	}());


/***/ }),
/* 33 */
/***/ (function(module, exports) {

	(function() {
	    "use strict";

	    module.exports = function(){

	        var integerRegex = /^[0-9]+$/;
	        var floatRegex = /^(\d+)?([.]?\d{0,2})?$/;
	        var posOrNegFloatRegex = /^-?[0-9]+(\.[0-9]{0,2})?$/;

	        // Populated
	        var isPopulated = function(value) {
	            return !isNotPopulated(value);
	        };

	        var isNotPopulated = function(value) {
	            return (value === '' || value === undefined || value.length < 1);
	        };

	        // Format
	        var isNotANumber = function(value) { return isNaN(value); };

	        var isInvalidInteger = function(value) {
	            return isNaN(value) || !value.match(integerRegex);
	        };

	        var isInvalidFloat = function(value) {
	            return isNaN(value) || !value.match(floatRegex);
	        };

	        var isInvalidPosOrNegFloat = function(value) {
	            return isNaN(value) || !value.match(posOrNegFloatRegex);
	        };

	        var isInvalidFloatOneDecimal = function(value) {
	            return (parseFloat(value).toFixed(1) != parseFloat(value));
	        };

	        var isInvalidFloatTwoDecimal = function(value) {
	            return (parseFloat(value).toFixed(2) != parseFloat(value));
	        };

	        var isInvalidParsedDate = function(value) {
	            return value === 'bad date';
	        };

	        // Range
	        var isOutsideIntegerRange = function(value, min, max) {
	            return parseInt(value) < parseInt(min) || parseInt(value) > parseInt(max);
	        };

	        var isOutsideFloatRange = function(value, min, max) {
	            return parseFloat(value) < parseFloat(min) || parseFloat(value) > parseFloat(max);
	        };

	        // Less/Greater than
	        var isLessThanInteger = function(value, integer) {
	            return parseInt(value) < parseInt(integer);
	        };

	        var isLessThanFloat = function(value, float) {
	            return parseFloat(value) < parseFloat(float);
	        };

	        var isLessThanDate = function(value, date) {
	            return new Date(value) < new Date(date);
	        };

	        var isGreaterThanOrEqualToDate = function(value, date) {
	            return new Date(value) >= new Date(date);
	        };

	        var isGreaterThanInteger = function(value, integer) {
	            return parseInt(value) > parseInt(integer);
	        };

	        var isGreaterThanFloat = function(value, float) {
	            return parseFloat(value) > parseFloat(float);
	        };

	        var checkAllRentsBelow2000 = function(rentData) {
	            // for(var i = 0; i < rentArray.length; i++){
	            //     if(rentArray[i] >= 2000){
	            //         return false;
	            //     }
	            // }
	            if (rentData.year1Rent  >= 2000) return false;
	            if (rentData.year2Rent  >= 2000) return false;
	            if (rentData.year3Rent  >= 2000) return false;
	            if (rentData.year4Rent  >= 2000) return false;
	            if (rentData.year5Rent  >= 2000) return false;
	            return true;
	        };

	        var effectiveDateWithinFTBRange = function(effectiveDate) {
	          return effectiveDate >= new Date('November 22, 2017');
	        };

	        var effectiveDateJuly2020 = function(effectiveDate) {
	            return effectiveDate < new Date('July 08, 2020');
	        };

	        var effectiveDateMarch2021 = function(effectiveDate) {
	            return effectiveDate > new Date('March 31, 2021');
	        };

	        return {
	            isPopulated : isPopulated,
	            isNotPopulated : isNotPopulated,
	            isNotANumber : isNotANumber,
	            isInvalidInteger : isInvalidInteger,
	            isInvalidFloat : isInvalidFloat,
	            isInvalidPosOrNegFloat : isInvalidPosOrNegFloat,
	            isInvalidFloatOneDecimal : isInvalidFloatOneDecimal,
	            isInvalidFloatTwoDecimal : isInvalidFloatTwoDecimal,
	            isInvalidParsedDate : isInvalidParsedDate,
	            isOutsideIntegerRange : isOutsideIntegerRange,
	            isOutsideFloatRange : isOutsideFloatRange,
	            isLessThanInteger : isLessThanInteger,
	            isLessThanFloat : isLessThanFloat,
	            isLessThanDate : isLessThanDate,
	            isGreaterThanOrEqualToDate : isGreaterThanOrEqualToDate,
	            isGreaterThanInteger : isGreaterThanInteger,
	            isGreaterThanFloat : isGreaterThanFloat,
	            checkAllRentsBelow2000 : checkAllRentsBelow2000,
	            effectiveDateWithinFTBRange: effectiveDateWithinFTBRange,
	            effectiveDateIsAfterJuly2020: effectiveDateJuly2020,
	            effectiveDateIsAfterMarch2021: effectiveDateMarch2021
	        };

	    };
	}());


/***/ }),
/* 34 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(10);

	    var summaryController = function($scope, $location, $anchorScroll, dataService, modelValidationService, navigationService, loggingService) {

	        var validator = __webpack_require__(33)();

	        var init = __webpack_require__(23);
	        init($scope, $location, $anchorScroll, 'summary', dataService, navigationService);

	        var rent = __webpack_require__(25);
	        rent = rent();
	        rent.addFunctionsToScope($scope);

	        // calculate highest rent
	        var highest = parseFloat("0");
	        if ($scope.displayYearOneRent && highest < parseFloat($scope.data.year1Rent)) highest = $scope.data.year1Rent;
	        if ($scope.displayYearTwoRent && highest < parseFloat($scope.data.year2Rent)) highest = $scope.data.year2Rent;
	        if ($scope.displayYearThreeRent && highest < parseFloat($scope.data.year3Rent)) highest = $scope.data.year3Rent;
	        if ($scope.displayYearFourRent && highest < parseFloat($scope.data.year4Rent)) highest = $scope.data.year4Rent;
	        if ($scope.displayYearFiveRent && highest < parseFloat($scope.data.year5Rent)) highest = $scope.data.year5Rent;
	        $scope.data.highestRent = highest;

	        $scope.validatedModel = modelValidationService.validate($scope.data);

	        var summaryHelper = __webpack_require__(35);
	        $scope.data.summary = summaryHelper.summaryHelper($scope, $scope.validatedModel);
	        dataService.updateModel($scope.data);

	        $scope.submit = function() {
	            navigationService.next('result', $scope.data, $location);
	        };

	        $scope.logEvent = loggingService.logEvent;

	    };

	    app.controller('summaryController', ['$scope', '$location', '$anchorScroll', 'dataService', 'modelValidationService', 'navigationService', 'loggingService', summaryController]);
	}());


/***/ }),
/* 35 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var Validator = __webpack_require__(33);

	    var validator = new Validator();

	    var displayFreehold = function(data) {
	        if(data === undefined) return false;
	        return data.holdingType === "Freehold";
	    };

	    var displayLeasehold = function(data) {
	        if(data === undefined) return false;
	        return data.holdingType === "Leasehold";
	    };

	    var displayTermOfLease = function(data) {
	        if(data === undefined) return false;
	        return displayLeasehold(data) && data.leaseTerm !== undefined;
	    };

	    var displayExchangeContracts = function(data) {
	        if(data === undefined) return false;
	        var allRentsBelow2000 = validator.checkAllRentsBelow2000(data);
	        return (data.holdingType === 'Leasehold' && 
	            data.propertyType === 'Non-residential' && 
	            data.premium < 150000 && 
	            allRentsBelow2000 && 
	            validator.isGreaterThanOrEqualToDate(data.effectiveDate, new Date(2016, 2, 17)));
	    };

	    var displayContractVaried = function(data) {
	        if(data === undefined) return false;
	        return (displayExchangeContracts(data) && data.contractPre201603 === 'Yes');
	    };

	    var displayRelevantRent = function(data) {
	        if(data === undefined) return false;
	        var allRentsBelow2000 = validator.checkAllRentsBelow2000(data);
	        var commonChecks = (data.holdingType === 'Leasehold' && data.propertyType === 'Non-residential' && data.premium < 150000 && allRentsBelow2000);

	        if (commonChecks && validator.isLessThanDate(data.effectiveDate, new Date(2016, 2, 17))) {
	            return true;
	        } else if (commonChecks && validator.isGreaterThanOrEqualToDate(data.effectiveDate, new Date(2016, 2, 17)) && data.contractPre201603 === 'Yes' && data.contractVariedPost201603 === 'No') {
	            return true;
	        }
	        return false;
	    };

	    var displayIndividual = function(data) {
	        if(data === undefined) return false;
	        return data.propertyType === "Residential" && validator.isGreaterThanOrEqualToDate(data.effectiveDate, new Date(2016, 3, 1));
	    };

	    var displayAdditionalProperty = function(data) {
	        if(data === undefined) return false;
	        return (displayIndividual(data) && data.individual === "Yes");
	    };

	    var displayOwnedOtherProperties = function(data) {
	        if(data === undefined) return false;
	        return (data.propertyType === 'Residential' && data.individual === 'Yes' && data.twoOrMoreProperties == 'No' && validator.effectiveDateWithinFTBRange(data.effectiveDate) && (validator.effectiveDateIsBeforeJuly2020(data.effectiveDate) ||  validator.effectiveDateIsAfterMarch2021(data.effectiveDate)));
	    };

	    var displayMainResidence = function(data) {
	        if(displayOwnedOtherProperties(data)) {
	            return data.ownedOtherProperties === 'No';
	        } else {
	            return false;
	        }
	    };

	    var displaySharedOwnership = function(data) {
	        if(displayMainResidence(data) && data.holdingType === 'Leasehold') {
	            return data.mainResidence === 'Yes';
	        } else {
	            return false;
	        }
	    };

	    var displayCurrentValue = function(data) {
	        if(displaySharedOwnership(data)) {
	            return data.sharedOwnership === 'Yes';
	        } else {
	            return false;
	        }
	    };

	    var displayPaySDLT = function(data) {
	        if(displayCurrentValue(data)) {
	            return data.currentValue === '£500,000 or less';
	        } else {
	            return false;
	        }
	    };

	    var displayReplaceMainResidence = function(data) {
	        if(data === undefined) return false;
	        return (displayAdditionalProperty(data) && data.twoOrMoreProperties === 'Yes');
	    };

	    var getDisplayValue = function(value) {
	        if(value === undefined || value === 'undefined' || value === '') {
	            return '-';
	        }
	        else {
	            return value;
	        }
	    };

	    var summaryHelper = function(scope, validatedModel) {
	        var template = [
	            {
	                question   : "Freehold or leasehold",
	                answer     : (scope.data !== undefined) ? getDisplayValue(scope.data.holdingType) : undefined,
	                link       : "#holding",
	                id         : "holdingType",
	                isValid    : validatedModel.isHoldingValid,
	                hiddenText : "Is property freehold or leasehold?"
	            },
	            {
	                question   : "Residential or non-residential",
	                answer     : (scope.data !== undefined) ? getDisplayValue(scope.data.propertyType) : undefined,
	                link       : "#property",
	                id         : "propertyType",
	                isValid    : validatedModel.isPropertyValid,
	                hiddenText : "Is property residential or non-residential?"
	            },
	            {
	                question   : "Effective date of transaction",
	                answer     : (scope.data !== undefined) ? getDisplayValue(scope.data.effectiveDate) : undefined,
	                link       : "#date",
	                id         : "effectiveDate",
	                isValid    : validatedModel.isEffectiveDateValid,
	                hiddenText : "Effective date of your transaction?",
	                type       : "Date"
	            },
	            {
	                question   : displayIndividual(scope.data) ? "Individual" : undefined,
	                answer     : (scope.data !== undefined) ? getDisplayValue(scope.data.individual) : undefined,
	                link       : "#purchaser",
	                id         : "individual",
	                isValid    : validatedModel.isIndividualValid,
	                hiddenText : "Are you purchasing the property as an individual?"
	            },
	            {
	                question   : displayAdditionalProperty(scope.data) ? "Additional residential property" : undefined,
	                answer     : (scope.data !== undefined) ? scope.data.twoOrMoreProperties : undefined,
	                link       : "#additional-property",
	                id         : "twoOrMoreProperties",
	                isValid    : validatedModel.isTwoOrMorePropertiesValid,
	                hiddenText : "Will you own two or more properties?"
	            },
	            {
	                question   : displayReplaceMainResidence(scope.data) ? "Replacing main residence" : undefined,
	                answer     : (scope.data !== undefined) ? scope.data.replaceMainResidence : undefined,
	                link       : "#additional-property",
	                id         : "replaceMainResidence",
	                isValid    : validatedModel.isReplaceMainResidenceValid,
	                hiddenText : "Are you replacing a main residence?"
	            },
	            {
	                question   : displayOwnedOtherProperties(scope.data) ? "Owned other property" : undefined,
	                answer     : (scope.data !== undefined) ? scope.data.ownedOtherProperties : undefined,
	                link       : "#owned-other-properties",
	                id         : "ownedOtherProperties",
	                isValid    : validatedModel.isOwnedOtherPropertiesValid,
	                hiddenText : "Have you ever owned any other property?"
	            },
	            {
	                question   : displayMainResidence(scope.data) ? "Main residence" : undefined,
	                answer     : (scope.data !== undefined) ? scope.data.mainResidence : undefined,
	                link       : "#main-residence",
	                id         : "mainResidence",
	                isValid    : validatedModel.isMainResidenceValid,
	                hiddenText : "Will this property be your main residence?"
	            },
	            {
	                question   : displaySharedOwnership(scope.data) ? "Shared ownership" : undefined,
	                answer     : (scope.data !== undefined) ? scope.data.sharedOwnership : undefined,
	                link       : "#shared-ownership",
	                id         : "sharedOwnership",
	                isValid    : validatedModel.isSharedOwnershipValid,
	                hiddenText : "Are you buying the property through a shared ownership scheme?"
	            },
	            {
	                question   : displayCurrentValue(scope.data) ? "Market value" : undefined,
	                answer     : (scope.data !== undefined) ? scope.data.currentValue : undefined,
	                link       : "#current-value",
	                id         : "currentValue",
	                isValid    : validatedModel.isCurrentValueValid,
	                hiddenText : "Is the current market value of the property £500,000 or less?"
	            },

	            {
	                question   : displayPaySDLT(scope.data) ? "Pay SDLT" : undefined,
	                answer     : displayPaySDLT(scope.data) ? scope.data.paySDLT : undefined,
	                link       : "#market-value",
	                id         : "market-value",
	                isValid    : validatedModel.isMarketValueValid,
	                hiddenText : "Pay SDLT"
	            },
	            {
	                question   : displayFreehold(scope.data) ? "Purchase price" : undefined,
	                answer     : displayFreehold(scope.data) ? scope.data.premium : undefined,
	                link       : "#purchase-price",
	                id         : "purchasePrice",
	                isValid    : validatedModel.isPurchasePriceValid,
	                hiddenText : "Purchase price?",
	                type       : "Currency"
	            },
	            {
	                question   : displayLeasehold(scope.data) ? "Start date as specified in lease" : undefined,
	                answer     : displayLeasehold(scope.data) ? scope.data.startDate : undefined,
	                link       : "#lease-dates",
	                id         : "leaseStartDate",
	                isValid    : validatedModel.isStartDateValid,
	                hiddenText : "Start date as specified in lease?",
	                type       : "Date"
	            },
	            {
	                question   : displayLeasehold(scope.data) ? "End date as specified in lease" : undefined,
	                answer     : displayLeasehold(scope.data) ? scope.data.endDate : undefined,
	                link       : "#lease-dates",
	                id         : "leaseEndDate",
	                isValid    : validatedModel.isEndDateValid,
	                hiddenText : "End date as specified in lease?",
	                type       : "Date"
	            },
	            {
	                question   : displayTermOfLease(scope.data) ? "Term of lease" : undefined,
	                answer     : displayTermOfLease(scope.data) ? getDisplayValue(scope.data.leaseTerm.years) + " years " + getDisplayValue(scope.data.leaseTerm.days) + " days" : undefined,
	                link       : undefined,
	                id         : "leaseTerm",
	                isValid    : "",
	                hiddenText : undefined
	            },
	            {
	                question   : displayLeasehold(scope.data) ? "Premium" : undefined,
	                answer     : displayLeasehold(scope.data) ? scope.data.premium : undefined,
	                link       : "#premium",
	                id         : "premium",
	                isValid    : validatedModel.isPremiumValid,
	                hiddenText : "Premium payable?",
	                type       : "Currency"
	            },
	            {
	                question   : (scope.displayYearOneRent) ? "Year 1 rent" : undefined,
	                answer     : (scope.displayYearOneRent) ? scope.data.year1Rent : undefined,
	                link       : "#rent",
	                id         : "year1Rent",
	                isValid    : validatedModel.isYear1RentValid,
	                hiddenText : "Year 1 rent?",
	                type       : "Currency"
	            },
	            {
	                question   : (scope.displayYearTwoRent) ? "Year 2 rent" : undefined,
	                answer     : (scope.displayYearTwoRent) ? scope.data.year2Rent : undefined,
	                link       : "#rent",
	                id         : "year2Rent",
	                isValid    : validatedModel.isYear2RentValid,
	                hiddenText : "Year 2 rent?",
	                type       : "Currency"
	            },
	            {
	                question   : (scope.displayYearThreeRent) ? "Year 3 rent" : undefined,
	                answer     : (scope.displayYearThreeRent) ? scope.data.year3Rent : undefined,
	                link       : "#rent",
	                id         : "year3Rent",
	                isValid    : validatedModel.isYear3RentValid,
	                hiddenText : "Year 3 rent?",
	                type       : "Currency"
	            },
	            {
	                question   : (scope.displayYearFourRent) ? "Year 4 rent" : undefined,
	                answer     : (scope.displayYearFourRent) ? scope.data.year4Rent : undefined,
	                link       : "#rent",
	                id         : "year4Rent",
	                isValid    : validatedModel.isYear4RentValid,
	                hiddenText : "Year 4 rent?",
	                type       : "Currency"
	            },
	            {
	                question   : (scope.displayYearFiveRent) ? "Year 5 rent" : undefined,
	                answer     : (scope.displayYearFiveRent) ? scope.data.year5Rent : undefined,
	                link       : "#rent",
	                id         : "year5Rent",
	                isValid    : validatedModel.isYear5RentValid,
	                hiddenText : "Year 5 rent?",
	                type       : "Currency"
	            },
	            {
	                question   : displayLeasehold(scope.data) ? "Highest 12 monthly rent" : undefined,
	                answer     : displayLeasehold(scope.data) ? scope.data.highestRent : undefined,
	                link       : undefined,
	                id         : "highestRent",
	                isValid    : "",
	                hiddenText : undefined,
	                type       : "Currency"
	            },
	            {
	                question   : (displayExchangeContracts(scope.data)) ? "Exchange of contracts before 17 March 2016" : undefined,
	                answer     : (displayExchangeContracts(scope.data)) ? scope.data.contractPre201603 : undefined,
	                link       : "#exchange-contracts",
	                id         : "contractPre201603",
	                isValid    : validatedModel.isContractPre201603Valid,
	                hiddenText : "Exchange of contracts before 17 March 2016?"
	            },
	            {
	                question   : (displayContractVaried(scope.data)) ? "Contract changed on or after 17 March 2016" : undefined,
	                answer     : (displayContractVaried(scope.data)) ? scope.data.contractVariedPost201603 : undefined,
	                link       : "#exchange-contracts",
	                id         : "contractVariedPost201603",
	                isValid    : validatedModel.isContractVariedPost201603Valid,
	                hiddenText : "Contract changed on or after 17 March 2016?"
	            },
	            {
	                question   : (displayRelevantRent(scope.data)) ? "Relevant rental figure" : undefined,
	                answer     : (displayRelevantRent(scope.data)) ? scope.data.relevantRent : undefined,
	                link       : "#relevant-rent",
	                id         : "relevantRent",
	                isValid    : validatedModel.isRelevantRentValid,
	                hiddenText : "Relevant rental figure?",
	                type       : "Currency"
	            }
	        ];
	        var result = [];
	        for(var i = 0; i < template.length; i++) {
	            if(template[i].question !== undefined) {
	                result.push(template[i]);
	            }
	        }
	        
	        return result;
	    };
	    
	    module.exports = {
	        summaryHelper : summaryHelper
	    };
	}());


/***/ }),
/* 36 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(10);

	    var detailController = function($scope, $location, $anchorScroll, dataService, modelValidationService, navigationService) {

	        var validator = __webpack_require__(33)();

	        var init = __webpack_require__(23);
	        init($scope, $location, $anchorScroll, 'detail', dataService, navigationService);

	        if (!modelValidationService.validate($scope.data).isModelValid) {
	            $location.path('summary');
	        }

	        $scope.printView = function() {
	            navigationService.printView($scope.data, $location);
	        };

	        $scope.isAdditionalProperty = function() {
	            return $scope.data.propertyType === "Residential" &&
	                validator.isGreaterThanOrEqualToDate($scope.data.effectiveDate, new Date(2016, 3, 1)) &&
	                $scope.data.twoOrMoreProperties == "Yes" &&
	                $scope.data.replaceMainResidence == "No";
	        };
	    };

	    app.controller('detailController', ['$scope', '$location', '$anchorScroll', 'dataService', 'modelValidationService', 'navigationService', detailController ]);
	}());


/***/ }),
/* 37 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(10);

	    var printController = function($scope, $location, $anchorScroll, dataService, modelValidationService, navigationService) {

	        var validator = __webpack_require__(33)();

	        var init = __webpack_require__(23);
	        init($scope, $location, $anchorScroll, 'print', dataService, navigationService);

	        if (modelValidationService.validate($scope.data).isModelValid) {
	            var rent = __webpack_require__(25);
	            rent = rent();
	            rent.addFunctionsToScope($scope);            
	        }   
	        else {
	            $location.path('summary');
	        } 

	        $scope.displayExchangeContracts = function() {
	            var allRentsBelow2000 = validator.checkAllRentsBelow2000($scope.data);
	            return ($scope.data.holdingType === 'Leasehold' && 
	                $scope.data.propertyType === 'Non-residential' && 
	                $scope.data.premium < 150000 && 
	                allRentsBelow2000 && 
	                validator.isGreaterThanOrEqualToDate($scope.data.effectiveDate, new Date(2016, 2, 17)));
	        };

	        $scope.displayContractVaried = function() {
	            return ($scope.displayExchangeContracts() && $scope.data.contractPre201603 === 'Yes');
	        };

	        $scope.displayRelevantRent = function() {
	            var allRentsBelow2000 = validator.checkAllRentsBelow2000($scope.data);
	            var commonChecks = ($scope.data.holdingType === 'Leasehold' && $scope.data.propertyType === 'Non-residential' && $scope.data.premium < 150000 && allRentsBelow2000);

	            if (commonChecks && validator.isLessThanDate($scope.data.effectiveDate, new Date(2016, 2, 17))) {
	                return true;
	            } else if (commonChecks && validator.isGreaterThanOrEqualToDate($scope.data.effectiveDate, new Date(2016, 2, 17)) && $scope.data.contractPre201603 === 'Yes' && $scope.data.contractVariedPost201603 === 'No') {
	                return true;
	            }
	            return false;
	        };

	        $scope.displayAdditionalProperty = function() {
	            return $scope.data.propertyType === "Residential" && validator.isGreaterThanOrEqualToDate($scope.data.effectiveDate, new Date(2016, 3, 1));
	        };

	        $scope.displayReplaceMainResidence = function() {
	            return ($scope.displayAdditionalProperty() && $scope.data.twoOrMoreProperties === 'Yes');
	        };

	        // $scope.getHeading = function() {
	        //     if($scope.effDateAfterCutOff()) {
	        //         return "Results based on SDLT rules before 4 December 2014";
	        //     } else {
	        //         return "Result";
	        //     }
	        // };
	          
	    };

	    app.controller('printController', ['$scope', '$location', '$anchorScroll', 'dataService', 'modelValidationService', 'navigationService', printController ]);
	}());


/***/ }),
/* 38 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	  "use strict";

	  __webpack_require__(39);
	  __webpack_require__(41);
	  __webpack_require__(42);
	  __webpack_require__(43);
	  __webpack_require__(44);
	  __webpack_require__(45);
	  __webpack_require__(47);
	  __webpack_require__(48);
	  __webpack_require__(49);
	  __webpack_require__(50);
	  __webpack_require__(51);
	  __webpack_require__(52);
	  __webpack_require__(53);
	  __webpack_require__(54);
	  __webpack_require__(55);
	  __webpack_require__(56);
	  __webpack_require__(57);
	  __webpack_require__(58);
	  __webpack_require__(59);
	  __webpack_require__(60);
	  __webpack_require__(61);
	  __webpack_require__(62);

	}());


/***/ }),
/* 39 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(40);

	    app.service('dataService', function(){
	        var model = {};
	     		 
	        var updateModel = function(data){
	            model = angular.copy(data);
	        };
	     		 
	        var getModel = function(){
	            return angular.copy(model);
	        };
	     		 
	        return {
	            updateModel : updateModel,
	            getModel : getModel
	        };
	    });
	}());


/***/ }),
/* 40 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

		var angular = __webpack_require__(2);

		module.exports = angular.module("calc.services",[]);

	}());


/***/ }),
/* 41 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	  "use strict";

	  var app = __webpack_require__(40);

	  app.service('dataMarshallingService', function(){

	    var validator = __webpack_require__(33)();

	    var constructCalculationRequest = function(data){
	      var model = {};

	      model.holdingType = data.holdingType;
	      model.propertyType = data.propertyType;
	      model.effectiveDateDay = parseInt(data.effectiveDateDay);
	      model.effectiveDateMonth = parseInt(data.effectiveDateMonth);
	      model.effectiveDateYear = parseInt(data.effectiveDateYear);
	      model.highestRent = data.highestRent;
	      model.premium = data.premium;

	      if(data.propertyType === 'Residential' && data.effectiveDate >= new Date('April 1, 2016')) {
	        model.propertyDetails = constructPropertyDetails(data);
	      }

	      if(data.holdingType === "Leasehold") {
	        model.leaseDetails = constructLeaseDetails(data);
	      }

	      if(data.holdingType === "Leasehold" && data.propertyType === 'Non-residential' && data.premium < 150000 && validator.checkAllRentsBelow2000(data)) {
	        if(data.effectiveDate >= new Date('March 16, 2016')) {
	          model.relevantRentDetails = constructRelevantRentDetails(data, true);
	        } else {
	          model.relevantRentDetails = constructRelevantRentDetails(data, false);
	        }
	      }

	      if(data.propertyType === 'Residential' && data.individual === 'Yes' && data.twoOrMoreProperties === 'No' && validator.effectiveDateWithinFTBRange(data.effectiveDate) && (validator.effectiveDateIsBeforeJuly2020(data.effectiveDate) || validator.effectiveDateIsAfterMarch2021(data.effectiveDate))) {
	        model.firstTimeBuyer = constructFirstTimeBuyerDetails(data);
	      }

	      return model;
	    };

	    function constructPropertyDetails(data) {
	      var propertyDetails = {};
	        propertyDetails.individual = data.individual;
	        if (data.individual === 'Yes') {
	          propertyDetails.twoOrMoreProperties = data.twoOrMoreProperties;
	          if (data.twoOrMoreProperties === 'Yes') {
	            propertyDetails.replaceMainResidence = data.replaceMainResidence;
	          } else {
	            if(data.mainResidence === 'Yes'){
	              propertyDetails.sharedOwnership = data.sharedOwnership;
	                if(data.sharedOwnership === 'Yes'){
	                  if(data.currentValue === '£500,000 or less'){
	                      propertyDetails.currentValue = 'Yes';
	                  } else {
	                      propertyDetails.currentValue = 'No';
	                  }
	                }
	            }
	          }
	        }
	        return propertyDetails;
	    }

	    function constructLeaseDetails(data) {
	      var leaseDetails = {};
	      leaseDetails.startDateDay = parseInt(data.startDateDay);
	      leaseDetails.startDateMonth = parseInt(data.startDateMonth);
	      leaseDetails.startDateYear = parseInt(data.startDateYear);
	      leaseDetails.endDateDay = parseInt(data.endDateDay);
	      leaseDetails.endDateMonth = parseInt(data.endDateMonth);
	      leaseDetails.endDateYear = parseInt(data.endDateYear);
	      leaseDetails.leaseTerm = data.leaseTerm;

	      var rentsToInclude = yearsOfRentToInclude(data.leaseTerm);
	      leaseDetails.year1Rent = data.year1Rent;
	      if(rentsToInclude >= 2) {leaseDetails.year2Rent = data.year2Rent;}
	      if(rentsToInclude >= 3) {leaseDetails.year3Rent = data.year3Rent;}
	      if(rentsToInclude >= 4) {leaseDetails.year4Rent = data.year4Rent;}
	      if(rentsToInclude >= 5) {leaseDetails.year5Rent = data.year5Rent;}
	      return leaseDetails;
	    }

	    function yearsOfRentToInclude(leaseTerm) {
	      if(leaseTerm.years >= 5) {
	        return 5;
	      } else if (leaseTerm.days > 0) {
	        return leaseTerm.years + 1;
	      } else {
	        return leaseTerm.years;
	      }
	    }

	    function constructRelevantRentDetails(data, includeFilters) {
	      var relRentDetails = {};
	      if(!includeFilters) {
	        relRentDetails.relevantRent = data.relevantRent;
	      } else {
	        relRentDetails.contractPre201603 = data.contractPre201603;
	        if(data.contractPre201603 === "Yes") {
	          relRentDetails.contractVariedPost201603 = data.contractVariedPost201603;
	          if(data.contractVariedPost201603 === "No") {
	            relRentDetails.relevantRent = data.relevantRent;
	          }
	        }
	      }
	      return relRentDetails;
	    }

	    function constructFirstTimeBuyerDetails(data) {
	      if(data.ownedOtherProperties === "No" && data.mainResidence === "Yes") {
	        if(data.sharedOwnership === 'Yes' && data.currentValue === '£500,000 or less'){
	          return "Yes";
	        }else if (data.currentValue === 'More than £500,000') {
	            return "No";
	        }else{
	           return "Yes";
	        }
	      } else {
	        return "No";
	      }
	    }

	    return {
	      constructCalculationRequest : constructCalculationRequest
	    };
	  });
	}());


/***/ }),
/* 42 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

		var app = __webpack_require__(40);

		var navigationService = function() {

	        var validator = __webpack_require__(33)();
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
	                if(model.propertyType === "Residential") {
	                    var dateHelper = __webpack_require__(15);
	                    var effectiveDate = dateHelper.parseUIDate(model.effectiveDateYear, model.effectiveDateMonth, model.effectiveDateDay);

	                    if(validator.isLessThanDate(effectiveDate, new Date(2016, 3, 1))) {
	                        redirectBasedOnHoldingType(model, locationService);
	                    } else {
	                        redirectToNext(locationService, 'purchaser');
	                    }
	                } else {
	                    redirectBasedOnHoldingType(model, locationService);
	                }
	            }
	            else if (currentView === 'purchase-price') {
	                redirectToNext(locationService, 'summary');
	            }
	            else if (currentView === 'purchaser') {
	                if (model.individual === "Yes") {
	                    redirectToNext(locationService, 'additional-property');
	                } else {
	                    redirectBasedOnHoldingType(model, locationService);
	                }
	            }
	            else if (currentView === "additional-property") {
	                redirectBasedOnFTBExclusionCriteria(model, locationService);
	            }
	            else if (currentView === "owned-other-properties") {
	                if(model.ownedOtherProperties === "No") {
	                    redirectToNext(locationService, 'main-residence');
	                } else {
	                    redirectBasedOnHoldingType(model, locationService);
	                }
	            }
	            else if (currentView === "main-residence") {
	                if(model.mainResidence === "Yes" && model.holdingType === "Leasehold") {
	                    redirectToNext(locationService, 'shared-ownership');
	                } else {
	                redirectBasedOnHoldingType(model, locationService);
	                }
	            }
	            else if (currentView === "shared-ownership") {
	                if(model.sharedOwnership === "Yes") {
	                    redirectToNext(locationService, 'current-value');
	                } else {
	                    redirectBasedOnHoldingType(model, locationService);
	                }
	            }
	            else if (currentView === "current-value") {
	                if(model.currentValue === "£500,000 or less") {
	                    redirectToNext(locationService, 'market-value');
	                } else {
	                    redirectBasedOnHoldingType(model, locationService);
	                }
	            }
	            else if (currentView === "market-value") {
	                if(model.paySDLT === "Using market value election" || model.paySDLT === "Stages") {
	                    redirectToNext(locationService, 'lease-dates');
	                }
	            }
	            else if (currentView === 'lease-dates') {
	                if(model.premium !== undefined && model.currentValue === '£500,000 or less' && model.sharedOwnership === 'Yes') {
	                    redirectToNext(locationService, 'rent');
	                } else {
	                redirectToNext(locationService, 'premium');
	                }
	            }
	            else if (currentView === 'premium') {
	                redirectToNext(locationService, 'rent');
	            }
	            else if (currentView === 'rent') {
	                var allRentsBelow2000 = validator.checkAllRentsBelow2000(model);
	                if (model.propertyType === 'Non-residential' && model.premium < 150000 && allRentsBelow2000) {
	                    if(model.effectiveDate > new Date(2016, 2, 16)) {
	                        redirectToNext(locationService, 'exchange-contracts');
	                    } else {
	                        redirectToNext(locationService, 'relevant-rent');
	                    }
	                } 
	                else {
	                    redirectToNext(locationService, 'summary');
	                }
	            }
	            else if(currentView === 'exchange-contracts') {
	                if(model.contractPre201603 === 'Yes' && model.contractVariedPost201603 === 'No') {
	                    redirectToNext(locationService, 'relevant-rent');
	                } else {
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

	      function redirectBasedOnFTBExclusionCriteria(model, locationService) {
	        if(model.propertyType === 'Residential' &&
	          validator.effectiveDateWithinFTBRange(model.effectiveDate) &&
	          model.individual === 'Yes' &&
	          model.twoOrMoreProperties === 'No' &&
	          (validator.effectiveDateIsBeforeJuly2020(model.effectiveDate) ||
	           validator.effectiveDateIsAfterMarch2021(model.effectiveDate))
	          ) {
	            redirectToNext(locationService, 'owned-other-properties');
	          } else {
	            redirectBasedOnHoldingType(model, locationService);
	          }
	      }

	      function redirectBasedOnHoldingType(model, locationService) {
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


/***/ }),
/* 43 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	   "use strict";

	   var app = __webpack_require__(40);
	/* istanbul ignore next */
	   app.service('cookieService', function(){

	       var getCookie = function() {
	           return GOVUK.getCookie("mdtpurr");
	       };

	       return {
	           getCookie : getCookie
	       };
	   });
	}());

/***/ }),
/* 44 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(40);

	    var loggingService = function() {

	        var logEvent = function(category, action, label) {
	            ga('send', 'event', category, action, label);
	        };

	        return {
	            logEvent : logEvent
	        };
	    };

	    app.service('loggingService', loggingService);

	}());


/***/ }),
/* 45 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(40);

	    app.service('additionalPropertyValidationService', function() {

	        var validate = function(data) {
	            var state = {};
	            var buildState = __webpack_require__(46);
	            var validator = __webpack_require__(33)();

	            if (validator.isNotPopulated(data.twoOrMoreProperties)) {
	                state.twoOrMoreProperties = "Select 'Yes' or 'No'";
	                ga('send', 'event', "userError", "twoOrMorePropertiesError", "notPopulated");
	            }

	            if(data.twoOrMoreProperties === "Yes") {
	                if (validator.isNotPopulated(data.replaceMainResidence)) {
	                    state.replaceMainResidence = "Select 'Yes' or 'No'";
	                    ga('send', 'event', "userError", "replaceMainResidenceError", "notPopulated");
	                }
	            }

	            return buildState(state);
	        };
	      
	        return {
	            validate: validate
	        };
	    });

	}());


/***/ }),
/* 46 */
/***/ (function(module, exports) {

	(function() {
	    "use strict";
	    
		// returns an object with methods to check validitity of model
		module.exports = function(state){
	        var hasError = function(field) {
	            if (state[field]) {
	                return 'form-field--error';
	            } else {
	                return '';
	            }
	        };

	        var validationMessage = function(field) {
	            return state[field] || '';
	        };

	        function isEmpty() {
	            for(var prop in state) {
	                return !state.hasOwnProperty(prop);
	            }

	            return true;
	        }

	        var isValid = isEmpty();

	        return {
	            isValid: isValid,
	            hasError: hasError,
	            validationMessage: validationMessage
	        };
		};
	}());	 		 


/***/ }),
/* 47 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(40);

	    app.service('exchangeContractsValidationService', function() {

	        var validate = function(data) {
	            var state = {};
	            var buildState = __webpack_require__(46);
	            var validator = __webpack_require__(33)();

	            if (validator.isNotPopulated(data.contractPre201603)) {
	                state.contractPre201603 = "Select 'Yes' or 'No'";
	                ga('send', 'event', "userError", "contractPre201603Error", "notPopulated");
	            }

	            if(data.contractPre201603 === "Yes") {
	                if (validator.isNotPopulated(data.contractVariedPost201603)) {
	                    state.contractVariedPost201603 = "Select 'Yes' or 'No'";
	                    ga('send', 'event', "userError", "contractVariedPost201603Error", "notPopulated");
	                }
	            }

	            return buildState(state);
	        };
	      
	        return {
	            validate: validate
	        };
	    });

	}());


/***/ }),
/* 48 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(40);

	    app.service('dateValidationService', function() {

	        var validate = function(data) {
	            var state = {};
	            var buildState = __webpack_require__(46);
	            var validator = __webpack_require__(33)();

	            if (validator.isNotPopulated(data.effectiveDate)) {
	                state.effectiveDate = 'You must complete the effective date field';
	                ga('send', 'event', "userError", "effectiveDateError", "notPopulated");
	            } else if (validator.isInvalidParsedDate(data.effectiveDate)) {
	                state.effectiveDate = 'Enter a valid date';
	                ga('send', 'event', "userError", "effectiveDateError", "invalid");
	            } else if (data.propertyType === 'Residential' && validator.isLessThanDate(data.effectiveDate, new Date(2012, 2, 22))) {
	                state.effectiveDate = "Date can't be earlier than 22/3/2012";
	                ga('send', 'event', "userError", "effectiveDateError", "outOfRange");
	            }

	            return buildState(state);
	        };
	      
	        return {
	            validate: validate
	        };
	    });

	}());


/***/ }),
/* 49 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(40);

	    app.service('ownedOtherPropertiesValidationService', function() {

	        var validate = function(data) {
	            var state = {};
	            var buildState = __webpack_require__(46);
	            var validator = __webpack_require__(33)();

	            if (validator.isNotPopulated(data.ownedOtherProperties)) {
	                state.ownedOtherProperties = "Select 'Yes' or 'No'";
	                ga('send', 'event', "userError", "ownedOtherPropertiesError", "notPopulated");
	            }

	            return buildState(state);
	        };

	        return {
	            validate: validate
	        };
	    });

	}());


/***/ }),
/* 50 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(40);

	    app.service('mainResidenceValidationService', function() {

	        var validate = function(data) {
	            var state = {};
	            var buildState = __webpack_require__(46);
	            var validator = __webpack_require__(33)();

	            if (validator.isNotPopulated(data.mainResidence)) {
	                state.mainResidence = "Select 'Yes' or 'No'";
	                ga('send', 'event', "userError", "mainResidenceError", "notPopulated");
	            }

	            return buildState(state);
	        };

	        return {
	            validate: validate
	        };
	    });

	}());


/***/ }),
/* 51 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(40);

	    app.service('sharedOwnershipValidationService', function() {

	        var validate = function(data) {
	            var state = {};
	            var buildState = __webpack_require__(46);
	            var validator = __webpack_require__(33)();

	            if (validator.isNotPopulated(data.sharedOwnership)) {
	                state.sharedOwnership = "Select 'Yes' or 'No'";
	                ga('send', 'event', "userError", "sharedOwnershipError", "notPopulated");
	            }

	            return buildState(state);
	        };

	        return {
	            validate: validate
	        };
	    });
	}());


/***/ }),
/* 52 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(40);

	    app.service('currentValueValidationService', function() {

	        var validate = function(data) {
	            var state = {};
	            var buildState = __webpack_require__(46);
	            var validator = __webpack_require__(33)();

	            if (validator.isNotPopulated(data.currentValue)) {
	                state.currentValue = "Select 'Yes' or 'No'";
	                ga('send', 'event', "userError", "currentValueError", "notPopulated");
	            }

	            return buildState(state);
	        };

	        return {
	            validate: validate
	        };
	    });

	}());


/***/ }),
/* 53 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(40);

	    app.service('marketValueValidationService', function() {

	        var validate = function(data) {
	            var state = {};
	            var buildState = __webpack_require__(46);
	            var validator = __webpack_require__(33)();

	            if (validator.isNotPopulated(data.paySDLT)) {
	                state.paySDLT = "Provide an answer to continue.";
	                ga('send', 'event', "userError", "marketValueError", "notPopulated");
	            }

	            if(data.paySDLT === "Using market value election" || data.paySDLT === "Stages") {
	                if (validator.isNotPopulated(data.premium)) {
	                    state.marketValue = "Provide an answer to continue.";
	                    ga('send', 'event', "userError", "marketValueError", "notPopulated");
	                } else if (validator.isInvalidFloat(data.premium)) {
	                    state.marketValue = "Enter the amount again - don't use any letters or characters including £";
	                    ga('send', 'event', "userError", "marketValueError", "invalid");
	                }
	            }
	            if(validator.isLessThanInteger(500000, data.premium)) {
	                state.marketValue = "Enter a value that is £500000 or less.";
	                ga('send', 'event', "userError", "marketValueError", "notPopulated");
	            }
	            
	            return buildState(state);
	        };

	        return {
	            validate: validate
	        };
	    });

	}());


/***/ }),
/* 54 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(40);

	    app.service('holdingValidationService', function() {

	        var validate = function(data) {
	            var state = {};
	            var buildState = __webpack_require__(46);
	            var validator = __webpack_require__(33)();

	            if (validator.isNotPopulated(data.holdingType)) {
	                state.holdingType = "Select 'Freehold' or 'Leasehold'";
	                ga('send', 'event', "userError", "holdingTypeError", "notPopulated");
	            }

	            return buildState(state);
	        };
	      
	        return {
	            validate: validate
	        };
	    });

	}());


/***/ }),
/* 55 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(40);

	    app.service('leaseDatesValidationService', function() {

	        var validate = function(data) {
	            var state = {},
	                startDateValid = false,
	                endDateValid = false;

	            var buildState = __webpack_require__(46);
	            var validator = __webpack_require__(33)();

	            if (validator.isNotPopulated(data.startDate)) {
	                state.startDate = 'Enter a start date';
	                ga('send', 'event', "userError", "startDateError", "notPopulated");
	            } else if (validator.isInvalidParsedDate(data.startDate)) {
	                state.startDate = 'Enter a valid date';
	                ga('send', 'event', "userError", "startDateError", "invalid");
	            } else {
	                startDateValid = true;
	            }

	            if (validator.isNotPopulated(data.endDate)) {
	                state.endDate = 'Enter an end date';
	                ga('send', 'event', "userError", "endDateError", "notPopulated");
	            } else if (validator.isInvalidParsedDate(data.endDate)) {
	                state.endDate = 'Enter a valid date';
	                ga('send', 'event', "userError", "endDateError", "invalid");
	            } else if (startDateValid && validator.isLessThanDate(data.endDate, data.startDate)) {
	                state.endDate = "End date can't be before the start date";
	                ga('send', 'event', "userError", "endDateError", "outOfRange");
	            } else if (data.effectiveDate && validator.isLessThanDate(data.endDate, data.effectiveDate)) {
	                state.endDate = "End date can't be before the effective date";
	                ga('send', 'event', "userError", "endDateError", "outOfRange");
	            }

	            return buildState(state);
	        };
	      
	        return {
	            validate: validate
	        };
	    });

	}());

/***/ }),
/* 56 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(40);

	    app.service('premiumValidationService', function() {

	        var validate = function(data) {
	            var state = {};
	            var buildState = __webpack_require__(46);
	            var validator = __webpack_require__(33)();

	            if (validator.isNotPopulated(data.premium)) {
	                state.premium = "Enter your Premium";
	                ga('send', 'event', "userError", "premiumError", "notPopulated");
	            } else if (validator.isInvalidFloat(data.premium)) {
	                state.premium = "Enter the premium again - don't use any letters or characters including £";
	                ga('send', 'event', "userError", "premiumError", "invalid");
	            }

	            return buildState(state);
	        };
	      
	        return {
	            validate: validate
	        };
	    });

	}());


/***/ }),
/* 57 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(40);

	    app.service('propertyValidationService', function() {

	        var validate = function(data) {
	            var state = {};
	            var buildState = __webpack_require__(46);
	            var validator = __webpack_require__(33)();

	            if (validator.isNotPopulated(data.propertyType)) {
	                state.propertyType = "Select 'Residential' or 'Non-residential'";
	                ga('send', 'event', "userError", "propertyTypeError", "notPopulated");
	            }

	            return buildState(state);
	        };
	      
	        return {
	            validate: validate
	        };
	    });

	}());


/***/ }),
/* 58 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(40);

	    app.service('purchasePriceValidationService', function() {

	        var validate = function(data) {
	            var state = {};
	            var buildState = __webpack_require__(46);
	            var validator = __webpack_require__(33)();

	            if (validator.isNotPopulated(data.premium)) {
	                state.premium = "Enter your Purchase Price";
	                ga('send', 'event', "userError", "purchasePriceError", "notPopulated");
	            } else if (validator.isInvalidFloat(data.premium)) {
	                state.premium = "Enter the purchase price again - don't use any letters or characters including £";
	                ga('send', 'event', "userError", "purchasePriceError", "invalid");
	            }

	            return buildState(state);
	        };
	      
	        return {
	            validate: validate
	        };
	    });

	}());


/***/ }),
/* 59 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(40);

	    app.service('purchaserValidationService', function() {

	        var validate = function(data) {
	            var state = {};
	            var buildState = __webpack_require__(46);
	            var validator = __webpack_require__(33)();

	            if (validator.isNotPopulated(data.individual)) {
	                state.individual = "Select 'Yes' or 'No'";
	                ga('send', 'event', "userError", "individualError", "notPopulated");
	            }

	            return buildState(state);
	        };
	      
	        return {
	            validate: validate
	        };
	    });

	}());


/***/ }),
/* 60 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(40);

	    app.service('relevantRentValidationService', function() {

	        var validate = function(data) {
	            var state = {};
	            var buildState = __webpack_require__(46);
	            var validator = __webpack_require__(33)();

	            if (validator.isNotPopulated(data.relevantRent)) {
	                state.relevantRent = "Please enter the rental figure";
	                ga('send', 'event', "userError", "relevantRentError", "notPopulated");
	            } else if (validator.isInvalidFloat(data.relevantRent)) {
	                state.relevantRent = "Enter the relevant rent again - don't use any letters or characters including £";
	                ga('send', 'event', "userError", "relevantRentError", "invalid");
	            }

	            return buildState(state);
	        };
	      
	        return {
	            validate: validate
	        };
	    });

	}());


/***/ }),
/* 61 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(40);

	    app.service('rentValidationService', function() {

	        var validate = function(data) {
	            var state = {};
	            var buildState = __webpack_require__(46);
	            var validator = __webpack_require__(33)();
	            var rent = __webpack_require__(25);
	            rent = rent().getFunctions(data);

	            var validateRent = function(data, state, field) {
	                if (validator.isNotPopulated(data[field])) {
	                    state[field] = "Enter the annual rent for all the years";
	                    ga('send', 'event', "userError", field + "Error", "notPopulated");
	                } else if (validator.isInvalidFloat(data[field])) {
	                    state[field] = "Enter the rent again - don't use any letters or characters including £";
	                    ga('send', 'event', "userError", field + "Error", "invalid");
	                }
	            };

	            if (rent.displayYearOneRent) {
	                validateRent(data, state, 'year1Rent');
	            }

	            if (rent.displayYearTwoRent) {
	                validateRent(data, state, 'year2Rent');
	            }

	            if (rent.displayYearThreeRent) {
	                validateRent(data, state, 'year3Rent');
	            }

	            if (rent.displayYearFourRent) {
	                validateRent(data, state, 'year4Rent');
	            }

	            if (rent.displayYearFiveRent) {
	                validateRent(data, state, 'year5Rent');
	            }

	            return buildState(state);
	        };
	      
	        return {
	            validate: validate
	        };
	    });

	}());


/***/ }),
/* 62 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(40);

	    app.service('modelValidationService', function() {

	        var validate = function(data) {
	            var rent = __webpack_require__(25);
	            var validator = __webpack_require__(33)();
	            rent = rent();
	            rent = rent.getFunctions(data);

	            var result = {
	                isModelValid : true
	            };

	            var hasError = function(value) {
	                if (data[value]) {
	                    return '';
	                } else {
	                    result.isModelValid = false;
	                    return 'form-field--error';
	                }
	            };

	            // mandatory pages
	            result.isHoldingValid = hasError('holdingType');
	            result.isPropertyValid = hasError('propertyType');
	            result.isEffectiveDateValid = hasError('effectiveDate');
	            
	            // if Freehold then must have premium
	            if(data.holdingType === 'Freehold') {
	                result.isPurchasePriceValid = hasError('premium');
	            }

	            // if Residential & >= 01/04/2016 then individual question required
	            if(data.propertyType === 'Residential' && data.effectiveDate >= new Date(2016, 3, 1)) {
	                result.isIndividualValid = hasError('individual');
	                // individual = Yes then additional property question(s) required
	                if (data.individual === 'Yes') {
	                    result.isTwoOrMorePropertiesValid = hasError('twoOrMoreProperties');
	                     if (data.twoOrMoreProperties === 'Yes') {
	                         result.isReplaceMainResidenceValid = hasError('replaceMainResidence');
	                    }
	                }
	            }

	            // if Residential, between 22/11/2017 and 30/11/2019 and individual and not
	            // two or more properties then FTB information required
	            if(data.propertyType === 'Residential' &&
	               validator.effectiveDateWithinFTBRange(data.effectiveDate) &&
	                (validator.effectiveDateIsBeforeJuly2020(data.effectiveDate) ||
	                validator.effectiveDateIsAfterMarch2021(data.effectiveDate)) &&
	               data.individual === 'Yes' &&
	               data.twoOrMoreProperties === 'No'
	            ){
	                result.isOwnedOtherPropertiesValid = hasError('ownedOtherProperties');
	                if(data.ownedOtherProperties === 'No') {
	                    result.isMainResidenceValid = hasError('mainResidence');
	                }
	            }

	            // if Residential, between 22/11/2017 and 30/11/2019 and individual and not
	            // two or more properties then FTB information required
	            if( data.holdingType === 'Leasehold' &&
	                data.propertyType === 'Residential' &&
	                validator.effectiveDateWithinFTBRange(data.effectiveDate) &&
	                data.individual === 'Yes' &&
	                data.twoOrMoreProperties === 'No' &&
	                data.ownedOtherProperties === 'No' &&
	                data.mainResidence === 'Yes'
	            ){
	                result.isSharedOwnershipValid = hasError('sharedOwnership');
	                if(data.sharedOwnership === 'Yes'){
	                    result.isCurrentValueValid = hasError('currentValue');
	                    if(data.currentValue === 'Yes'){
	                        result.isMarketValueValid = hasError('paySDLT');
	                        if(data.paySDLT === ('Using market value election')|| data.paySDLT === 'Stages') {
	                            result.isPremiumValid = hasError('premium');
	                        }
	                    }
	                }
	            }

	            // if Leasehold must have lease dates and appropriate number of rents
	            if(data.holdingType === 'Leasehold') {
	                result.isStartDateValid = hasError('startDate');
	                result.isEndDateValid = hasError('endDate');
	                result.isPremiumValid = hasError('premium');

	                result.isYear1RentValid = hasError('year1Rent');
	                if (rent.displayYearTwoRent) result.isYear2RentValid = hasError('year2Rent');
	                if (rent.displayYearThreeRent) result.isYear3RentValid = hasError('year3Rent');
	                if (rent.displayYearFourRent) result.isYear4RentValid = hasError('year4Rent');
	                if (rent.displayYearFiveRent) result.isYear5RentValid = hasError('year5Rent');

	                var allRentsBelow2000 = validator.checkAllRentsBelow2000(data);
	                if(data.propertyType === 'Non-residential' && data.premium < 150000 && allRentsBelow2000){
	                    if (data.effectiveDate > new Date('March 16, 2016')) {
	                        result.isContractPre201603Valid = hasError('contractPre201603');
	                        if (data.contractPre201603 === 'Yes') {
	                            result.isContractVariedPost201603Valid = hasError('contractVariedPost201603');
	                        }
	                        if (data.contractPre201603 === 'Yes' && data.contractVariedPost201603 === 'No') {
	                            result.isRelevantRentValid = hasError('relevantRent');
	                        }
	                    } else {
	                        result.isRelevantRentValid = hasError('relevantRent');
	                    }
	               }
	            }
	            return result;
	        };
	      
	        return {
	            validate: validate
	        };
	    });

	}());


/***/ }),
/* 63 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

		__webpack_require__(64);
		
	}());


/***/ }),
/* 64 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

	    var app = __webpack_require__(65);

	    app.filter('calcCurrency', ['$filter', function($filter) {
	      return function(input, currency) {

	        if(input === undefined || input === 'undefined' || input === '') {
	          return '-';
	        }

	        input = parseFloat(input);

	        if(input % 1 === 0) {
	          input = input.toFixed(0);
	        }
	        else {
	          input = input.toFixed(2);
	        }

	        if(currency === undefined || currency === 'undefined' || currency === '') {
	          currency = '';
	        }

	        return currency + input.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
	      };
	    }]);
	}());


/***/ }),
/* 65 */
/***/ (function(module, exports, __webpack_require__) {

	(function() {
	    "use strict";

		var angular = __webpack_require__(2);

		module.exports = angular.module("calc.filters",[]);

	}());


/***/ })
/******/ ]);