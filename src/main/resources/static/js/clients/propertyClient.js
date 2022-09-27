
const propertyAPIBaseURL = "/api/v1/properties/"
const propertyValueAPIBaseURL = "/api/v1/property_values/"

const propertyClient = {

    getAllProperties: function(onSuccess, onFailure) {
        let request = $.ajax({
            method: "GET",
            url: propertyAPIBaseURL
        });
        request.done(onSuccess);
        request.fail(onFailure);
    },

    getPropertyById: function(propertyId, onSuccess, onFailure) {
        let request = $.ajax({
            method: "GET",
            url: propertyAPIBaseURL + propertyId
        });
        request.done(onSuccess);
        request.fail(onFailure);
    },

    getPropertyValueById: function(propertyValId, onSuccess, onFailure) {
        let request = $.ajax({
            method: "GET",
            url: propertyValueAPIBaseURL + propertyValId
        });
        request.done(onSuccess);
        request.fail(onFailure);
    },

    getPropertyValueItemsById: function(propertyValId, onSuccess, onFailure) {
        let request = $.ajax({
            method: "GET",
            url: propertyValueAPIBaseURL + propertyValId + "/items"
        });
        request.done(onSuccess);
        request.fail(onFailure);
    },

    createProperty: function(property, onSuccess, onFailure) {
        let request = $.ajax({
            method: "POST",
            url: propertyAPIBaseURL,
            contentType: "application/json",
            data: JSON.stringify(property)
        });
        request.done(onSuccess);
        request.fail(onFailure);
    },

    createPropertyValue: function(propertyVal, onSuccess, onFailure) {
        let request = $.ajax({
            method: "POST",
            url: propertyValueAPIBaseURL,
            contentType: "application/json",
            data: JSON.stringify(propertyVal)
        });
        request.done(onSuccess);
        request.fail(onFailure);
    },

    updateProperty: function(propertyId, property, onSuccess, onFailure) {
        let request = $.ajax({
            method: "PUT",
            url: propertyAPIBaseURL + propertyId,
            contentType: "application/json",
            data: JSON.stringify(property)
        });
        request.done(onSuccess);
        request.fail(onFailure);
    },

    updatePropertyVal: function(propertyValId, propertyVal, onSuccess, onFailure) {
        let request = $.ajax({
            method: "PUT",
            url: propertyValueAPIBaseURL + propertyValId,
            contentType: "application/json",
            data: JSON.stringify(propertyVal)
        });
        request.done(onSuccess);
        request.fail(onFailure);
    },

    assignPropertyValToItem: function(propertyValId, itemId, onSuccess, onFailure) {
        let request = $.ajax({
            method: "PUT",
            url: propertyValueAPIBaseURL + propertyValId + "/item/" + itemId
        });
        request.done(onSuccess);
        request.fail(onFailure);
    },

    deleteProperty: function(propertyId, onSuccess, onFailure) {
        let request = $.ajax({
            method: "DELETE",
            url: propertyAPIBaseURL + propertyId
        });
        request.done(onSuccess);
        request.fail(onFailure);
    },

    deletePropertyVal: function(propertyValId, onSuccess, onFailure) {
        let request = $.ajax({
            method: "DELETE",
            url: propertyValueAPIBaseURL + propertyValId
        });
        request.done(onSuccess);
        request.fail(onFailure);
    },

    removePropertyValItemLink: function(propertyValId, itemId, onSuccess, onFailure) {
        let request = $.ajax({
            method: "DELETE",
            url: propertyValueAPIBaseURL + propertyValId + "/item/" + itemId
        });
        request.done(onSuccess);
        request.fail(onFailure);
    }

}