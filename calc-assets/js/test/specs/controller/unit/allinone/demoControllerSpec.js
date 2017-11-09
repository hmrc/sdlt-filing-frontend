(function() {
    'use strict';

    require("calc-module");

    var mocks = require("angular-mocks-wrapper");


    describe('Demo Controller', function () {


        var controller,
            mockController,
            mockBackend,
            mockScope = false;

        var testResponse = "test server response";

        beforeEach(mocks.module('calc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope, $httpBackend) {

          mockScope = $rootScope.$new();
          mockScope.getHelpSetup = function() {return true;};

          mockController = $controller;
          mockBackend = $httpBackend;

        }));

        it('should process an OK response from the server', function(){
          mockBackend.whenPOST('/calculate-stamp-duty-land-tax/test-response').respond(200, testResponse);
          controller = mockController('demoController', {
            $scope : mockScope,
            $location : {}
          });
          mockBackend.flush();

          expect(mockScope.responseReceived).toEqual(true);
          expect(mockScope.response).toEqual(testResponse);
        });

        it('should process a BAD_REQUEST response from the server', function(){
          testResponse = "test error response";
          mockBackend.whenPOST('/calculate-stamp-duty-land-tax/test-response').respond(400, testResponse);
          controller = mockController('demoController', {
            $scope : mockScope,
            $location : {}
          });
          mockBackend.flush();

          expect(mockScope.responseReceived).toEqual(true);
          expect(mockScope.response).toEqual("400 response received. Message: test error response");
        });

    });
}());