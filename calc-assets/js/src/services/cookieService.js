(function() {
   "use strict";

   var app = require("./module");
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