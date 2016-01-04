(function () {
    describe('Navigation Service', function () {

        var service;

        beforeEach(angular.mock.module("calc.services"));
        beforeEach(inject(function(_loggingService_) {
            service = _loggingService_;
        }));

        it('should be registered with angular', function () {
            expect(service).not.toBeUndefined();
        });

        describe("Calling logEvent()", function() {
            
            var testPage = 'start',
                gaCalls = [],
                currentView = '';

            beforeEach(function() {
                ga = function(arg1, arg2, arg3, arg4, arg5) {
                    var call = [arg1, arg2, arg3, arg4, arg5];
                    gaCalls.push(call);
                };

                log = service.logEvent('link','click','example.com');
            });

            it('should call google analytics (GA) twice', function() {
                expect(gaCalls.length).toEqual(1);
            });

            it('should first call GA with the command "send"', function() {
                var call = gaCalls[0];
                var command = call[0];
                expect(command).toEqual('send');
            });

            it('should call GA with the property "event"', function() {
                var call = gaCalls[0];
                var property = call[1];
                expect(property).toEqual('event');
            });            
            it('should call GA with the property "link"', function() {
                var call = gaCalls[0];
                var property = call[2];
                expect(property).toEqual('link');
            });    
            it('should call GA with the property "click"', function() {
                var call = gaCalls[0];
                var property = call[3];
                expect(property).toEqual('click');
            });    
            it('should call GA with the property "example.com"', function() {
                var call = gaCalls[0];
                var property = call[4];
                expect(property).toEqual('example.com');
            });    
        });
    });
}());