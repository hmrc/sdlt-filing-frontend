(function() {
    'use strict';

    require("calc-module");

    var mocks = require("angular-mocks-wrapper");

    describe('Main Controller', function () {
        
        var controller,
            mockScope,
            mockWindow,
            mockLoggingService,
            mockCookieService;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {

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
            mockLoggingService = { logEvent: function() {} };

            
            controller = $controller(
                'mainController', 
                {
                    $scope: mockScope,
                    loggingService: mockLoggingService,
                    cookieService: mockCookieService
                });
        }));

        it('should initialise scope with jumpTo function', function(){
            expect(mockScope.jumpTo).toBeDefined();
        });

        it('should initialise scope with toggleHelp function', function(){
            expect(mockScope.toggleHelp).toBeDefined();
        });

        it('should initialise scope with displayHelp function', function(){
            expect(mockScope.displayHelp).toBeDefined();
        });

        describe('Calling toggleHelp()', function () {
            
            beforeEach(mocks.inject(function ($controller, $rootScope) {
                
                mockScope = $rootScope.$new();
                mockLoggingService = { logEvent: function() {} };
                spyOn(mockLoggingService, 'logEvent');


                controller = $controller(
                    'mainController', 
                    {
                        $scope: mockScope,
                        loggingService: mockLoggingService,
                        cookieService: mockCookieService
                    });
            }));

            it('should call the navigation service once', function(){
                mockScope.toggleHelp('helpId', 'some text');
                expect(mockLoggingService.logEvent.calls.count()).toEqual(1);
            });

            it('should set a visibility toggle value to true when it is false', function(){
                mockScope.optionalHelp.helpId = false;
                mockScope.toggleHelp('helpId', 'some text');
                expect(mockScope.optionalHelp.helpId).toEqual(true);
            });
            
            it('should set a visibility toggle value to false when it is true', function(){
                mockScope.optionalHelp.helpId = true;
                mockScope.toggleHelp('helpId', 'some text');
                expect(mockScope.optionalHelp.helpId).toEqual(false);
            });
        });

        describe('Calling jumpTo()', function () {

            it('should set focus on the specified element', function(){
                var element = $('<input id="apple"/>');
                element.appendTo(document.body);
                mockScope.jumpTo('apple');
                expect(element).toHaveFocus();
            });

        });

        describe('Calling displayHelp()', function () {

            it('should call the optionalHelp function once', function(){
                mockScope.optionalHelp.helpId = false;
                var result = mockScope.displayHelp('helpId');
                expect(result).toEqual(false);
            });

        });

        describe('Calling getHelpGA()', function () {

            beforeEach(mocks.inject(function ($controller, $rootScope) {
                
                mockScope = $rootScope.$new();
                mockLoggingService = { logEvent: function() {} };
                spyOn(mockLoggingService, 'logEvent');


                controller = $controller(
                    'mainController', 
                    {
                        $scope: mockScope,
                        loggingService: mockLoggingService,
                        cookieService: mockCookieService
                    });
            }));

            it('should call the getHelpGA function when expanding the "get help" section', function () {
                mockScope.getHelpGA();
                expect(mockLoggingService.logEvent.calls.count()).toEqual(1);
            });

            it('should call the getHelpGA function when colapsing the "get help" section', function () {
                // User expanded "get help" then colapsed it.
                mockScope.getHelpGA();
                mockScope.getHelpGA();
                expect(mockLoggingService.logEvent.calls.count()).toEqual(2);
            });

        });

        describe('Checking focus and clicking a radio button', function () {
            beforeEach(mocks.inject(function ($controller, $rootScope, $location) {

                jasmine.addMatchers({
                    toHaveFocus: function() {
                        return {
                            compare: function(actual) {
                                return {
                                    pass: document.activeElement === actual[0]
                                };
                            }
                        };
                    },
                    toHaveClass: function() {
                        return {
                            compare: function(actual, className) {
                                return {
                                    pass: $(actual).hasClass(className)
                                }; 
                            }
                        };
                    },
                    toNotHaveClass: function() {
                        return {
                            compare: function(actual, className) {
                                return {
                                    pass: !($(actual).hasClass(className))
                                }; 
                            }
                        };
                    }
                });

                mockScope = $rootScope.$new();
                mockLoggingService = { logEvent: function() {} };
                spyOn(mockLoggingService, 'logEvent');

                controller = $controller(
                    'mainController', 
                    {
                        $scope: mockScope,
                        loggingService: mockLoggingService,
                        cookieService: mockCookieService
                    });
            }));

            it("should have class 'add-focus selected' when focused", function () {
                var element = $('<div id="main">' +
                    '<label class="block-label" for="partner-no">' +
                        '<input id="partner-no" ng-model="data.havePartner" name="partner" value="no" type="radio" required>No' +
                    '</label>' +
                    '<label class="block-label" for="partner-no">' +
                        '<input id="partner-yes" ng-model="data.havePartner" name="partner" value="no" type="checkbox" required>Yes' +
                    '</label>' +
                '</div>');
                element.appendTo(document.body);
                $('#main').on(
                    'focus click', 
                    'label.block-label input[type=radio], label.block-label input[type=checkbox]',
                    mockScope.addFocusToLabel
                ).on(
                    'change',
                    'label.block-label input[type=radio], label.block-label input[type=checkbox]',
                    mockScope.toggleFocus
                ).on(
                    'blur',
                    'label.block-label input[type=radio], label.block-label input[type=checkbox]',
                    mockScope.removeFocusFromLabel
                );
                $('#partner-no').focus();
                expect($('#partner-no').closest('label')[0]).toHaveClass('add-focus selected');
            });

            it("should remove class 'add-focus selected' when blurred", function () {
                var element = $('<div id="main">' +
                    '<label class="block-label" for="partner-no">' +
                        '<input id="partner-no" ng-model="data.havePartner" name="partner" value="no" type="radio" required>No' +
                    '</label>' +
                    '<label class="block-label" for="partner-no">' +
                        '<input id="partner-yes" ng-model="data.havePartner" name="partner" value="no" type="checkbox" required>Yes' +
                    '</label>' +
                '</div>');
                element.appendTo(document.body);
                $('#main').on(
                    'focus click', 
                    'label.block-label input[type=radio], label.block-label input[type=checkbox]',
                    mockScope.addFocusToLabel
                ).on(
                    'change',
                    'label.block-label input[type=radio], label.block-label input[type=checkbox]',
                    mockScope.toggleFocus
                ).on(
                    'blur',
                    'label.block-label input[type=radio], label.block-label input[type=checkbox]',
                    mockScope.removeFocusFromLabel
                );
                $('#partner-no').focus();
                expect($('#partner-no').closest('label')[0]).toHaveClass('add-focus selected');
                $('#partner-no').blur();
                expect($('#partner-no').closest('label')[0]).toNotHaveClass('add-focus selected');
                $('#partner-yes').focus();
                expect($('#partner-yes').closest('label')[0]).toHaveClass('add-focus selected');
                $('#partner-yes').click();
                $('#partner-yes').blur();
                expect($('#partner-yes').closest('label')[0]).toNotHaveClass('add-focus selected');
            });

            it("should toggle class 'add-focus selected' on change", function () {
                var element = $('<div id="main">' +
                    '<label class="block-label" for="partner-no">' +
                        '<input id="partner-no" ng-model="data.havePartner" name="partner" value="no" type="radio" required>No' +
                    '</label>' +
                    '<label class="block-label" for="partner-no">' +
                        '<input id="partner-yes" ng-model="data.havePartner" name="partner" value="no" type="checkbox" required>Yes' +
                    '</label>' +
                '</div>');
                element.appendTo(document.body);
                $('#main').on(
                    'focus click', 
                    'label.block-label input[type=radio], label.block-label input[type=checkbox]',
                    mockScope.addFocusToLabel
                ).on(
                    'change',
                    'label.block-label input[type=radio], label.block-label input[type=checkbox]',
                    mockScope.toggleFocus
                ).on(
                    'blur',
                    'label.block-label input[type=radio], label.block-label input[type=checkbox]',
                    mockScope.removeFocusFromLabel
                );
                $('#partner-no').focus();
                expect($('#partner-no').closest('label')[0]).toHaveClass('add-focus selected');
                $('#partner-no').change();
                expect($('#partner-no').closest('label')[0]).toNotHaveClass('add-focus selected');
                $('#partner-yes').focus();
                expect($('#partner-yes').closest('label')[0]).toHaveClass('add-focus selected');
                $('#partner-yes').change();
                expect($('#partner-no').closest('label')[0]).toNotHaveClass('add-focus selected');
            });

        });

       describe('Calling the UR Banner', function () {

            beforeEach(mocks.inject(function ($controller, $rootScope) {

                mockScope = $rootScope.$new();
                mockLoggingService = { logEvent: function() {} };
                mockCookieService = { getCookie: function() {  return "potato"; } };

                mockWindow = {
                        location: {  href : "result" }
                 };

            controller = $controller(
                'mainController',
                {
                    $scope: mockScope,
                    $window: mockWindow,
                    loggingService: mockLoggingService,
                    cookieService: mockCookieService
                });
            }));

            it('should get banner-panel', function () {
                expect(mockScope.getURBannerClass()).toEqual("banner-panel");
            });
        });

       describe('Calling the UR Banner', function () {
            beforeEach(mocks.inject(function ($controller, $rootScope) {

                mockScope = $rootScope.$new();
                mockLoggingService = { logEvent: function() {} };
                mockCookieService = { getCookie: function() {  return null; } };

                mockWindow = {
                        location: {  href : "result" }
                 };

            controller = $controller(
                'mainController',
                {
                    $scope: mockScope,
                    $window: mockWindow,
                    loggingService: mockLoggingService,
                    cookieService: mockCookieService
                });
            }));

            it('should get banner-panel banner-panel--show', function () {
                expect(mockScope.getURBannerClass()).toEqual("banner-panel banner-panel--show");
            });
        });

    });
}());
