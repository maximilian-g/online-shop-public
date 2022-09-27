$(document).ready(function () {
    userClient.getCurrentUser(onGetUserSuccess, onRequestFailure)
});

function onGetUserSuccess(response) {
    $("#registeredAt").val(response.registeredAt);
    $("#email").val(response.email);
    $("#welcomeHeader").text("Hello, " + response.username + "! This is your account page.");
    $("#addAddressButton").click(function() {
        let newAddress = $("#newAddress").val();
        if(newAddress != null && newAddress.length > 0) {
            addressClient.createAddress(response.id,
                newAddress,
                onAddressCreateResponse,
                onRequestFailure)
        }
    });
    for(let i = 0; i < response.addresses.length; i++) {
        addressClient.getAddressById(response.addresses[i].id,
            onAddressGetSuccess,
            onRequestFailure)
    }
    let changeEmailInput = $("#changeEmailInput");
    changeEmailInput.val(response.email);
    $("#emailChangeButton").click(function() {
        let newEmail = changeEmailInput.val();
        console.log(newEmail);
        userClient.changeEmailForUser(response.id, newEmail, onEmailChangeSuccess,onRequestFailure)
    })

}

function onEmailChangeSuccess(response) {
    $("#email").val(response.email);
    showSuccessMessage("Email successfully changed.");
}

function onAddressGetSuccess(addressResponse) {
    let addressDeleteButtonId = "addressDelete" + addressResponse.id;
    let addressRowId = "addressRow" + addressResponse.id;
    $("#addressTableBody").append("<tr id='" + addressRowId + "'> <td>" + addressResponse.address + "</td>" +
        "<td>" +
        "<input id='" + addressDeleteButtonId + "' type='button' class='btn btn-warning' value='Delete address'>" +
        "</td>");
    $("#" + addressDeleteButtonId).click(function() {
        addressClient.deleteAddressById(addressResponse.id,
            function(response) {
                onAddressDeleteSuccess(response, addressRowId)
            }, onRequestFailure)
    });
}

function onAddressCreateResponse(createAddressResponse) {
    if(createAddressResponse.success != null && !createAddressResponse.success) {
        showErrorMessage(createAddressResponse.responseMessage);
    } else {
        onAddressGetSuccess(createAddressResponse);
        showSuccessMessage("Created address '" + createAddressResponse.address + "'");
    }
}

function onAddressDeleteSuccess(deleteAddressResponse, addressRowId) {
    if (deleteAddressResponse.success != null && !deleteAddressResponse.success) {
        showErrorMessage(deleteAddressResponse.responseMessage);
    } else {
        $("#" + addressRowId).remove();
        showSuccessMessage(deleteAddressResponse.responseMessage)
    }
}