(function() {
    'use strict';

    require("sdltc-module");

    var mocks = require("angular-mocks-wrapper");

    describe('Main Controller', function () {
        
        var controller,
            mockScope;

        beforeEach(mocks.module('sdltc.controllers'));
        beforeEach(mocks.inject(function ($controller, $rootScope) {
            mockScope = $rootScope.$new();
            controller = $controller('mainController', {$scope : mockScope});
        }));

        it('initialise controller with populated scope', function(){
            expect(mockScope.jumpTo).toBeDefined();
        });
    });
}());
