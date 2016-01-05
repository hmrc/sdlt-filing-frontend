(function() {
	'use strict';

    var mocks = require("angular-mocks-wrapper");			

    describe('Initial Controller', function () {
    	var InitController = require("../../../src/utilities/initController.js");
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
                logView : function() {},
                next : function() {}
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
            spyOn(mockNavigationService, 'next');
            spyOn(mockDataService, 'updateModel');

            controller = $controller(InitController,{
                scope : mockScope,
                location : mockLocation,
                scrollToHash : function() {},
                page : 'doc.html',
                dataService : mockDataService,
                navigationService : mockNavigationService
            });
		}));

    	it('should add a jumpTo method to the scope which will jump to the correct element', function () {
    		var element = $('<input id="banana"/>');
        	element.appendTo(document.body);
    		mockScope.jumpTo('banana');
    		expect(element).toHaveFocus();
    	});

        it('should add a submit method to the scope which will call the navigation service', function () {
            mockScope.submit();
            expect(mockNavigationService.next.calls.count()).toEqual(1);
        });


   	});
}());