let isSelectionShown = false;
$(document).ready(function() {

    let uploadFile = $("#uploadFile");
    if(uploadFile.length) {

        /* Upload single File   */
        uploadFile.submit(function (event) {
            event.preventDefault();
            $.ajax({
                type: 'POST',
                enctype: 'multipart/form-data',
                url: '/api/v1/items/bulk_upload',
                data: new FormData(this),
                contentType: false,
                cache: false,
                processData: false,
                success: function (response) {
                    for(let i = 0; i < response.length; i++) {
                        $("#uploadResult").append("<li>" + response[i] + "</li>");
                    }
                },
                error: processError
            });
        });
    }

    let itemId = $("#itemId").val();
    if(itemId != null) {
        let request = $.ajax({
            method: "GET",
            url: "/api/v1/items/" + itemId +"/quantity"
        });
        request.done(function (response) {
            $("#quantity").val(response);
            $("#itemQuantity").show();
        });
    }

    $("#itemTypeSelectionDiv").hide();
    isSelectionShown = false;

    let propertiesBody = $("#associatedPropertiesTbody");
    if(propertiesBody.length) {
        itemClient.getItemById(itemId, onGetItemSuccess, onRequestFailure)
    }

});

function onGetItemSuccess(item) {
    let tableBody = $("#associatedPropertiesTbody");
    for(let i = 0; i < item.properties.length; i++) {
        let prop = item.properties[i];
        let elemId = "propVal_" + prop.propValueId;
        let rowId = "row_" + elemId;
        let html = "<tr id='" + rowId +"'>" +
            "<td><a href='/admin/property/" + prop.propNameId + "'>" + prop.propNameId + "</a></td>" +
            "<td>" + prop.name + "</td>" +
            "<td><a href='/admin/property/value/" + prop.propValueId + "'>" + prop.propValueId + "</a></td>" +
            "<td>" + prop.value + "</td>" +
            "<td class='btn btn-danger' id='" + elemId + "'>Unassign value</td>" +
            "</tr>"
        tableBody.append(html)
        $("#" + elemId).click(function() {
            propertyClient.removePropertyValItemLink(prop.propValueId,
                item.id,
                function(responseObj) {
                    $("#" + rowId).remove();
                    showSuccessMessage(responseObj.status + ", " + responseObj.responseMessage)
                },
                onRequestFailure)
        })
    }
}

function onChangeItemType() {
    let selection = $("#itemTypeSelectionDiv");
    if(!isSelectionShown) {
        selection.show();
        isSelectionShown = true;
    } else {
        selection.hide();
        isSelectionShown = false;
    }
}

function processError(error) {
    let errorObj = JSON.parse(error.responseText);
    let errorMessage = errorObj.responseMessage;
    $("#msg").append("<li><div class='badge badge-danger'>Unable to upload image: " + errorMessage + "</div></li>")
    console.log(error);
}