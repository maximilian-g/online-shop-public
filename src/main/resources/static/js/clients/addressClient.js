
const addressAPIBaseURL = "/api/v1/addresses/"

const addressClient = {

    createAddress: function(ownerId, address, onSuccess, onFailure) {
        let request = $.ajax({
            method: "POST",
            url: addressAPIBaseURL,
            contentType: "application/json",
            data: JSON.stringify({
                ownerId: ownerId,
                address: address
            })
        });
        request.done(onSuccess);
        request.fail(onFailure);
    },

    getAddressById: function(addressId, onSuccess, onFailure) {
        let request = $.ajax({
            method: "GET",
            url: addressAPIBaseURL + addressId
        });
        request.done(onSuccess);
        request.fail(onFailure);
    },

    deleteAddressById: function(addressId, onSuccess, onFailure) {
        let request = $.ajax({
            method: "DELETE",
            url: addressAPIBaseURL + addressId
        });
        request.done(onSuccess);
        request.fail(onFailure);
    }

}