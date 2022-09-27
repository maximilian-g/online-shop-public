
function onUpdateClick() {
    let itemTypeId = $("#itemTypeId").val();
    itemTypeClient.updateItemTypeById(
        itemTypeId,
        {
            itemTypeId: Number(itemTypeId),
            name: $("#itemTypeName").val(),
            category: {
                id: Number($("#categorySelection").val())
            }
        },
        onUpdateSuccess,
        onRequestFailure);
}

function onUpdateSuccess(itemType) {
    let messageBox = $("#messageBox");
    messageBox.html("");
    messageBox.append("<div class='badge badge-success'>Item type updated successfully</div>")
    $("#selectCategoryLabel").text("Select category (current: " + itemType.category.name + "(" + itemType.category.id + "))");
    $("#itemTypeName").val(itemType.name);
    setTimeout(function() {
        messageBox.html("");
    }, 5000);
}

function onDeleteClick() {
    itemTypeClient.deleteItemTypeById($("#itemTypeId").val(),
        onDeleteSuccess,
        onRequestFailure);
}

function onDeleteSuccess(response) {
    $("#updateItemTypeForm").hide();
    let messageBox = $("#messageBox");
    messageBox.html("");
    messageBox.append("<div class='badge badge-success'>" + response.status +
        ", " + response.responseMessage + "</div>")
    setTimeout(function() {
        window.location = "/admin/item_type"
    }, 2000);
}

function onCreateClick() {
    itemTypeClient.createItemType(
        {
            name: $("#itemTypeNameCreation").val(),
            category: {
                id: Number($("#categoryOnCreationSelection").val())
            }
        },
        onCreateSuccess,
        onRequestFailure);
}

function onCreateSuccess(itemType) {
    let messageBox = $("#messageBox");
    messageBox.html("");
    messageBox.append("<div class='badge badge-success'>Item type created</div><br>");
    messageBox.append("<a class='badge badge-info' href='/admin/item_type/" + itemType.itemTypeId + "'>Link to item type</a>");
}

function onRequestFailure(jqXHR, textStatus) {
    //{"readyState":4,
    // "responseText":
    // "{\"status\":404,
    // \"error_message\":\"Category with specified id was not found.\",
    // \"timestamp\":\"2022-05-25T17:14:21.943+00:00\"}",
    // "responseJSON":{"status":404,"responseMessage":"Category with specified id was not found.","timestamp":"2022-05-25T17:14:21.943+00:00"},
    // "status":404,
    // "statusText":"error"}

    let messageBox = $("#messageBox");
    messageBox.html("");
    messageBox.append("<div class='badge badge-danger'>" + jqXHR.responseJSON.status +
        ", " + jqXHR.responseJSON.responseMessage + "</div>")
}

function fillCategorySelection(response) {
    let categorySelection = $("#categorySelection");
    let categoryOnCreateSelection = $("#categoryOnCreationSelection");
    for (let i = 0; i < response.length; i++) {
        let category = response[i];
        if (categorySelection.length) {
            categorySelection.append("<option value='" + category.id + "'>" + category.category + "(" + category.id + ")</option>");
        }
        if (categoryOnCreateSelection.length) {
            categoryOnCreateSelection.append("<option value='" + category.id + "'>" + category.category + "(" + category.id + ")</option>");
        }
    }
}

function onItemTypeIdSearchClick() {
    let id = $("#itemTypeSearchId").val();
    if(id != null && id.length > 0) {
        window.location = "/admin/item_type/" + id;
    }
}

$(document).ready(function () {

    $("#messageBox").html("");

    categoryClient.getAllCategories(fillCategorySelection, onRequestFailure)

})