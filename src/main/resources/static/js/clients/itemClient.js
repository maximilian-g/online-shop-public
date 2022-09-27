
const itemAPIBaseURL = "/api/v1/items/"

const itemClient = {

    getAllItemsMin: function(onSuccess, onFailure) {
        let request = $.ajax({
            method: "GET",
            url: itemAPIBaseURL + "min"
        });
        request.done(onSuccess);
        request.fail(onFailure);
    },

    getItemById: function(itemId, onSuccess, onFailure) {
        let request = $.ajax({
            method: "GET",
            url: itemAPIBaseURL + itemId
        });
        request.done(onSuccess);
        request.fail(onFailure);
    },

    getItemImagesById: function(itemId, onSuccess, onFailure) {
        let request = $.ajax({
            method: "GET",
            url: itemAPIBaseURL + itemId + "/images"
        });
        request.done(function(response) {
            onSuccess(response, itemId)
        });
        request.fail(onFailure);
    }

}