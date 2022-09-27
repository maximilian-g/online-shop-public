
const itemTypeAPIBaseURL = "/api/v1/item_types/"

const itemTypeClient = {

    createItemType: function(itemType, onSuccess, onFailure) {
        let request = $.ajax({
            method: "POST",
            url: itemTypeAPIBaseURL,
            contentType: "application/json",
            data: JSON.stringify(itemType)
        });
        request.done(onSuccess);
        request.fail(onFailure);
    },

    getAllItemTypesMin: function(onSuccess, onFailure) {
        let request = $.ajax({
            method: "GET",
            url: itemTypeAPIBaseURL + "min"
        });
        request.done(onSuccess);
        request.fail(onFailure);
    },

    getItemTypeById: function(itemTypeId, onSuccess, onFailure) {
        let request = $.ajax({
            method: "GET",
            url: itemTypeAPIBaseURL + itemTypeId
        });
        request.done(onSuccess);
        request.fail(onFailure);
    },

    updateItemTypeById: function(itemTypeId, itemType, onSuccess, onFailure) {
        console.log("Got item type " + JSON.stringify(itemType));
        let request = $.ajax({
            method: "PUT",
            url: itemTypeAPIBaseURL + itemType.itemTypeId,
            contentType: "application/json",
            data: JSON.stringify(itemType)
        });
        request.done(onSuccess);
        request.fail(onFailure);
    },

    deleteItemTypeById: function(itemTypeId, onSuccess, onFailure) {
        let request = $.ajax({
            method: "DELETE",
            url: itemTypeAPIBaseURL + itemTypeId
        });
        request.done(onSuccess);
        request.fail(onFailure);
    }

}