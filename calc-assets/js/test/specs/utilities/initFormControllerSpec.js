(function() {
	'use strict';

    var mocks = require("angular-mocks-wrapper");			

    describe('Initial Form Controller', function () {
    	var InitFormController = require("../../../src/utilities/initFormController.js");
    	var controller, 
            mockScope, 
            mockDataService, 
            mockNavigationService,
            mockValidationService,
            mockLocation,
            calledServiceGetModel = false;

    	beforeEach(mocks.inject(function ($rootScope,$controller) {
  			jasmine.addMatchers({
    			toHaveFocus: function() {
      				return {
        				compare: function(actual) {
          					return {
            					pass: document.activeElement === actual[0]
          					};
        				}
      				};
    			}
  			});

  			mockScope = $rootScope.$new();

            mockDataService = { 
                getModel : function() { 
                    return {
                        holdingType : "Freehold",
                        propertyType : "Non-residential"
                    }; 
                },
                updateModel : function() { }
            };

            mockNavigationService = { 
                logView : function() {}
            };

            mockValidationService = {
                validate : function() {
                    return { isModelValid : true };
                }
            };

            mockLocation = {
            	hash : function (id) {
            		return id;
            	}
            };

            spyOn(mockDataService, 'getModel').and.callThrough();
            spyOn(mockNavigationService, 'logView');
            spyOn(mockDataService, 'updateModel');

            controller = $controller(InitFormController,{
                scope : mockScope,
                location : mockLocation,
                scrollToHash : function() {},
                page : 'doc.html',
                dataService : mockDataService,
                validationService : mockValidationService,
                navigationService : mockNavigationService
            });
		}));

    	it('should add a jumpTo method to the scope which will jump to the correct element', function () {
    		var element = $('<input id="hello"/>');
        	element.appendTo(document.body);
    		mockScope.jumpTo('hello');
    		expect(element).toHaveFocus();
    	});


   	});
}());