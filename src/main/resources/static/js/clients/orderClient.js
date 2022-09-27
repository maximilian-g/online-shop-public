
const orderAPIBaseURL = "/api/v1/orders/"

const orderClient = {

    getAllStatuses: function(onSuccess, onFailure) {
        let request = $.ajax({
            method: "GET",
            url: orderAPIBaseURL + "statuses"
        });
        request.done(onSuccess);
        request.fail(onFailure);
    },

    updateOrderStatus: function(orderId, orderStatus, onSuccess, onFailure) {
        let request = $.ajax({
            method: "PUT",
            url: orderAPIBaseURL + orderId + "/status",
            contentType: "application/json",
            data: orderStatus
            // data: JSON.stringify({status: status})
        });
        request.done(onSuccess);
        request.fail(onFailure);
    }

}