(function() {
    "use strict";

    var app = require("../module");

    var demoController = function($scope, $location, $anchorScroll, $http) {

        $scope.response = null;
        $scope.responseReceived = false;

        $http.post("/calculate-stamp-duty-land-tax/test-response", {
          "returnError": false
        }).
           success(function(data, status, headers, config) {
             $scope.response = data;
             $scope.responseReceived = true;
           }).
           error(function(data, status, headers, config) {
             $scope.response = status+" response received. Message: "+data;
             $scope.responseReceived = true;
           });

    };

    app.controller('demoController', ['$scope', '$location', '$anchorScroll', '$http', demoController]);
}());
