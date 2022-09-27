
const CANCELLED_STATUS = 'CANCELLED';

function fillStatuses(statuses) {
    let statusSelection = $("#statusShow");
    let statusSelectionForFilter = $("#statusSelection");
    let currentStatus = statusSelection.children(".current-status").val();
    for(let i = 0; i < statuses.length; i++) {
        let option = "<option value='" + statuses[i] + "'>" + statuses[i] + "</option>";
        if(currentStatus !== statuses[i] && statusSelection.length) {
            statusSelection.append(option)
        }
        if(statusSelectionForFilter.length) {
            statusSelectionForFilter.append(option)
        }
    }
}

function onStatusUpdateSuccess(order) {
    $("#statusShowLabel").text("Status (current: " + order.status + ")")
    if(order.endDate != null) {
        $("#endDateShow").val(order.endDate);
    }
    showSuccessMessage("Status changed successfully")
}

$(document).ready(function () {

    $("#messageBox").html("");

    let changeStatusButton = $("#changeStatusButton");
    let statusSelectionForOrder = $("#statusShow");
    if(changeStatusButton.length) {
        statusSelectionForOrder.on('change', function() {
            if(this.value === CANCELLED_STATUS) {
                let statusSelectionDiv = $("#newStatusSelection");
                statusSelectionDiv.append("<div id='descriptionInputDiv' class='col-sm-10'>" +
                    "<label for='descriptionInput' class='control-label'>Enter description here</label>" +
                    "<input id='descriptionInput' required type='text' class='form-control' placeholder='Enter description here'>" +
                    "</div>");
            } else {
                $("#descriptionInputDiv").remove();
            }
        });
        changeStatusButton.click(function() {
            let status = statusSelectionForOrder.val();
            let orderId = $("#idShow").val();
            let descriptionInput = $("#descriptionInput")
            let description = descriptionInput.length ? descriptionInput.val() : '';
            if(status === CANCELLED_STATUS && description.length === 0) {
                showErrorMessageInDiv("Description field is required", 'descriptionInputDiv');

            } else {
                orderClient.updateOrderStatus(orderId,
                    JSON.stringify({status: status, description: description}),
                    onStatusUpdateSuccess,
                    onRequestFailure);
            }
        });
    }

    let orderSearchIdElem = $("#orderSearchId");
    let searchByIdButton = $("#searchById");
    if(orderSearchIdElem.length && searchByIdButton.length) {
        searchByIdButton.click(function() {
            let href;
            if(currentPage === "operatorOrders") {
                href = "/operator/order/";
            } else {
                href = "/admin/order/"
            }
            let value = orderSearchIdElem.val();
            if(!isNaN(Number(value))) {
                window.location = href + value;
            } else {
                showErrorMessage("Invalid order number")
            }
        })
    }

    if(statusSelectionForOrder.length || $("#statusSelection")) {
        orderClient.getAllStatuses(fillStatuses, onRequestFailure)
    }

    let filterByStatus = $("#filterByStatus");
    if(filterByStatus.length) {
        filterByStatus.click(function() {
            window.location = "/operator?status=" + $("#statusSelection").val();
        })
    }


})