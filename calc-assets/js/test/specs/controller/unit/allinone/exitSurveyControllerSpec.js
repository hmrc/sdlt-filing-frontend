(function() {
    'use strict';

    require("calc-module");

        var mocks = require("angular").mock;

        describe('exitSurveyController with invalid data and an error response', function () {

            var controller,
                mockScope,
                mockDataService,
                mockNavigationService,
                mockLoggingService,
                mockMessagesService,
                mockBackend,
                calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope, $httpBackend) {

                mockScope = $rootScope.$new();

                mockDataService = {
                    getModel : function() {},
                    updateModel : function() {}
                };

                mockNavigationService = {
                    logView : function() {}
                };

                 mockLoggingService = {
                    logEvent : function() {}
                };

                mockMessagesService = {};

                mockBackend = $httpBackend;
                mockBackend.whenPOST('/calculate-stamp-duty-land-tax/submitExitSurvey').respond(400, {result: "Bad things happened"}, {"server" : "boom boom"});

                spyOn(mockLoggingService, 'logEvent');

                controller = $controller('exitSurveyController', {
                    $scope : mockScope,
                    dataService : mockDataService,
                    navigationService : mockNavigationService,
                    loggingService : mockLoggingService,
                    messagesService : mockMessagesService
                });

                mockScope.data = {};
                mockScope.data.survey = {"temp" : "data"};

                mockScope.submitExitSurvey({});
                mockBackend.flush();
             }));

            it('should make 1 call to loggingService.logEvent', function () {
                expect(mockLoggingService.logEvent.calls.count()).toEqual(1);
                expect(mockLoggingService.logEvent).toHaveBeenCalledWith("error", "survey", "status: 400, server: boom boom");
            });
        });

        describe('exitSurveyController with valid data and an OK response', function () {

            var controller,
                mockScope,
                mockDataService,
                mockNavigationService,
                mockLoggingService,
                mockMessagesService,
                mockBackend,
                calledServiceGetModel = false;

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope, $httpBackend) {

                mockScope = $rootScope.$new();

                mockDataService = {
                    getModel : function() {},
                    updateModel : function() {}
                };

                mockNavigationService = {
                    logView : function() {}
                };

                 mockLoggingService = {
                    logEvent : function() {}
                };

                mockMessagesService = {};

                mockBackend = $httpBackend;
                mockBackend.whenPOST('/calculate-stamp-duty-land-tax/submitExitSurvey').respond(200, {result: "Good things happened"}, {"server" : "no boom"});

                spyOn(mockLoggingService, 'logEvent');

                controller = $controller('exitSurveyController', {
                    $scope : mockScope,
                    dataService : mockDataService,
                    navigationService : mockNavigationService,
                    loggingService : mockLoggingService,
                    messagesService : mockMessagesService
                });

                mockScope.data = {};
                mockScope.data.survey = {"temp" : "data"};

                mockScope.submitExitSurvey({});
                mockBackend.flush();
             }));

            it('should make no calls to loggingService.logEvent', function () {
                expect(mockLoggingService.logEvent.calls.count()).toEqual(0);
            });
        });
}());