(function() {
    "use strict";

    var app = require("./module");

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
