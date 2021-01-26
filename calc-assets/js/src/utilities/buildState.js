(function() {
    "use strict";
    
	// returns an object with methods to check validity of model
	module.exports = function(state){
        var hasError = function(field) {
            if (state[field]) {
                return 'form-field--error';
            } else {
                return '';
            }
        };

        var validationMessage = function(field) {
            return state[field] || '';
        };

        function isEmpty() {
            for(var prop in state) {
                return !state.hasOwnProperty(prop);
            }

            return true;
        }

        var isValid = isEmpty();

        return {
            isValid: isValid,
            hasError: hasError,
            validationMessage: validationMessage
        };
	};
}());	 		 
