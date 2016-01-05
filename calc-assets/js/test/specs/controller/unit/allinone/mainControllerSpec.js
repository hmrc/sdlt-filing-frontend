(function() {
    'use strict';

    require("calc-module");

    var mocks = require("angular-mocks-wrapper");

    describe('Main Controller', function () {
        
        var controller,
            mockScope,
            mockLoggingService;

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
                    loggingService: mockLoggingService
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
                        loggingService: mockLoggingService
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
    });
}());
