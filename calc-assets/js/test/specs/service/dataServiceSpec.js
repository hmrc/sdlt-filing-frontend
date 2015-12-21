(function() {
    'use strict';

    describe('Data Service', function () {

        var service;
        var mockForm = {
            test : "data",
            data : "test"
        };

        beforeEach(angular.mock.module("sdltc.services"));

        beforeEach(inject(function (_dataService_) {
            service = _dataService_;
        }));

        it('should return an undefined object when no data has been provided', function() {

            expect(service.getModel()).toEqual({});
        });

        it('should update the model and return the data when data provided', function() {
            // start with empty model
            expect(service.getModel()).toEqual({});
            // update model
            service.updateModel(mockForm);
            //verify model has changed
            expect(service.getModel()).toEqual(mockForm);
        });

        it('should be able to overwrite existing data', function() {
            var overwriteMockForm = {
                test : "apple",
                data : "pear"
            };

            // start with empty model
            expect(service.getModel()).toEqual({});
            // update model
            service.updateModel(mockForm);
            // verify model has changed
            expect(service.getModel()).toEqual(mockForm);
            // overwrite existing field
            service.updateModel(overwriteMockForm);
            // verify model has changed
            expect(service.getModel()).toEqual(overwriteMockForm);
        });

        it('should be able to add new fields to existing data', function() {
            var newMockForm = {
                test : "data",
                data : "pear",
                newField : "new-data"
            };

            // start with empty model
            expect(service.getModel()).toEqual({});
            // update model
            service.updateModel(mockForm);
            //verify model has changed
            expect(service.getModel()).toEqual(mockForm);
            // add new field
            service.updateModel(newMockForm);
            // verify model has changed
            expect(service.getModel()).toEqual(newMockForm);
        });

        it('should be able to remove fields from existing data', function() {
            var newMockForm = {
                newField : "new-data"
            };
            // start with empty model
            expect(service.getModel()).toEqual({});
            // update model
            service.updateModel(mockForm);
            //verify model has changed
            expect(service.getModel()).toEqual(mockForm);
            // remove fields, add new field
            service.updateModel(newMockForm);
            // verify model has changed
            expect(service.getModel()).toEqual(newMockForm);
        });

        it('should be able to remove all fields from existing data', function() {
            var newMockForm = {};
            // start with empty model
            expect(service.getModel()).toEqual({});
            // update model
            service.updateModel(mockForm);
            //verify model has changed
            expect(service.getModel()).toEqual(mockForm);
            // remove all fields
            service.updateModel(newMockForm);
            // verify model has changed
            expect(service.getModel()).toEqual(newMockForm);
        });

    });
}());
