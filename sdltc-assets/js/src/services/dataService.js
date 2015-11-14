(function() {
    "use strict";

    var app = require("./module");

    app.service('dataService', function(){
        var model = {};
     		 
        var updateModel = function(data){
            model = angular.copy(data);
        };
     		 
        var getModel = function(){
            return angular.copy(model);
        };
     		 
        return {
            updateModel : updateModel,
            getModel : getModel
        };
    });
}());
