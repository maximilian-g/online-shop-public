let associatedItems = 0;

function onUpdateClick() {
    let propertyId = $("#propertyId").val();
    propertyClient.updateProperty(propertyId,
        {
            itemTypeId: $("#itemTypeSelection").val(),
            name: $("#propertyName").val()
        },
        onPropertyUpdateSuccess,
        onRequestFailure);
}

function onPropertyUpdateSuccess(property) {
    let messageBox = $("#messageBox");
    messageBox.html("");
    messageBox.append("<div class='badge badge-success'>Property updated successfully</div>")
    itemTypeClient.getItemTypeById(property.itemTypeId, setItemTypeLabel, onRequestFailure);
    $("#propertyName").val(property.name);
    setTimeout(function () {
        messageBox.html("");
    }, 5000);
}

function onDeleteClick() {
    propertyClient.deleteProperty($("#propertyId").val(),
        onDeleteSuccess,
        onRequestFailure);
}

function onDeleteSuccess(response) {
    window.location = "/admin/property"
}

function onPropValCreate() {
    let propVal = $("#newPropertyValue").val();
    if(propVal != null && propVal.length > 0) {
        propertyClient.createPropertyValue({
            propId: Number($("#propertyId").val()),
            value: propVal
        }, onPropValueCreateSuccess, onRequestFailure);
    }
}

function onPropValueCreateSuccess(propVal) {
    window.location.href = window.location.href;
}

function onUpdateValClick() {
    let propertyValId = $("#propertyValId").val();
    propertyClient.updatePropertyVal(propertyValId,
        {
            propId: $("#propertySelection").val(),
            propValueId: propertyValId,
            value: $("#propertyValue").val()
        },
        onPropertyValUpdateSuccess,
        onRequestFailure);
}

function onPropertyValUpdateSuccess(propertyVal) {
    let messageBox = $("#messageBox");
    messageBox.html("");
    messageBox.append("<div class='badge badge-success'>Property value updated successfully</div>")
    $("#propertyValue").val(propertyVal.value);
    setTimeout(function () {
        messageBox.html("");
    }, 5000);
}

function onDeleteValClick() {
    let propertyValId = $("#propertyValId").val();
    propertyClient.deletePropertyVal(propertyValId,
        onDeleteSuccess,
        onRequestFailure);
}

function onSearchPropertyClick() {
    let searchId = $("#propertySearchId").val();
    if (searchId != null && searchId.length > 0) {
        window.location = "/admin/property/" + searchId;
    }
}

function onCreateClick() {
    propertyClient.createProperty(
        {
            name: $("#propertyNameCreation").val(),
            itemTypeId: $("#itemTypeSelectionOnCreation").val()
        },
        onCreatePropSuccess,
        onRequestFailure
    );
}

function onCreatePropSuccess(property) {
    let messageBox = $("#messageBox");
    messageBox.html("");
    messageBox.append("<div class='badge badge-success'>Property created</div><br>");
    messageBox.append("<a class='badge badge-info' href='/admin/property/" + property.id + "'>Link to property</a>");
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

function fillItemTypeSelection(response) {
    let currentItemTypeId = $("#currentItemTypeId").val();
    let itemTypeSelection = $("#itemTypeSelection");
    let itemTypeSelectionOnCreation = $("#itemTypeSelectionOnCreation");
    let itemTypeSelectionOptions = [];
    for (let i = 0; i < response.length; i++) {
        let itemType = response[i];
        let option = "<option value='" + itemType.id + "'>" + itemType.name + "(" + itemType.id + ")</option>";
        if(itemType.id === Number(currentItemTypeId)) {
            itemTypeSelectionOptions.splice(0, 0, option);
        } else {
            itemTypeSelectionOptions.push(option);
        }
        if(itemTypeSelectionOnCreation.length) {
            itemTypeSelectionOnCreation.append("<option value='" + itemType.id + "'>" + itemType.name + "(" + itemType.id + ")</option>");
        }
    }

    for(let i = 0; i < itemTypeSelectionOptions.length; i++) {
        if(itemTypeSelection.length) {
            itemTypeSelection.append(itemTypeSelectionOptions[i]);
        }
    }

}

function fillAllProperties(properties) {
    let propertySelection = $("#propertySelection");
    for(let i = 0; i < properties.length; i++) {
        let property = properties[i];
        propertySelection.append("<option value='" + property.id + "'>" + property.name + "(" + property.id + ")</option>");
    }
}

function setPropLabel(property) {
    console.log("Got property " + JSON.stringify(property));
    $("#selectPropertyLabel").text("Select property: (current is " + property.name + "(" + property.id + "))")
    $("#propName").val(property.name);
    $("#propNameShow").show()
}

function setItemTypeLabel(itemType) {
    console.log("Got item type " + JSON.stringify(itemType));
    $("#selectItemTypeLabel").text("Select item type: (current is " + itemType.name + "(" + itemType.itemTypeId + "))")
}

function fillAssociatedItems(itemCollection) {
    let tableBody = $("#associatedItemsTbody");
    tableBody.html("");
    associatedItems = itemCollection.length;
    for(let i = 0; i < itemCollection.length; i++) {
        let itemDto = itemCollection[i];
        tableBody.append("<tr id='itemTbody_" + itemDto.id + "'>" +
            "<td><a href='/admin/item/" + itemDto.id + "'>" + itemDto.id + "</a></td>" +
            "<td>" + itemDto.name + "</td>" +
            "<td><button type='button' class='btn btn-danger' onclick='onRemoveValClick(" + itemDto.id + ")'>Remove link</button></td></tr>")
    }
    if(associatedItems <= 0) {
        $("#associatedItemsDiv").hide();
    } else {
        $("#associatedItemsDiv").show();
    }
}

function fillAllItemsSelect(itemCollection) {
    let itemSelect = $("#assignValToItemSelect");
    for(let i = 0; i < itemCollection.length; i++) {
        let item = itemCollection[i];
        itemSelect.append("<option value='" + item.id + "'>" + item.name + "(" + item.id + ")</option>");

    }
}

function onAssignValClick() {
    let itemVal = $("#assignValToItemSelect").val();
    propertyClient.assignPropertyValToItem($("#propertyValId").val(), itemVal, onAssignValSuccess, onRequestFailure)
}

function onAssignValSuccess(response) {
    propertyClient.getPropertyValueItemsById($("#propertyValId").val(), fillAssociatedItems, onRequestFailure);
    onLinkChangeSuccess(response);
}

function onLinkChangeSuccess(responseObj) {
    let msgBox = $("#messageBox");
    msgBox.html("");
    msgBox.append("<div class='badge badge-success'>" + responseObj.status +
        ", " + responseObj.responseMessage + "</div>")
}

function onRemoveValClick(itemId) {
    console.log("Removing item link #" + itemId);
    propertyClient.removePropertyValItemLink($("#propertyValId").val(),
        itemId,
        function(responseObj) {
            propertyClient.getPropertyValueItemsById($("#propertyValId").val(), fillAssociatedItems, onRequestFailure);
            onLinkChangeSuccess(responseObj);
        },
        onRequestFailure)
}

$(document).ready(function () {

    $("#messageBox").html("");
    let propId = $("#propId");

    if($("#itemTypeSelection").length || $("#itemTypeSelectionOnCreation").length) {
        itemTypeClient.getAllItemTypesMin(fillItemTypeSelection, onRequestFailure)
    }

    if($("#propertySelection").length) {
        propertyClient.getAllProperties(fillAllProperties, onRequestFailure)
    }

    if($("#selectPropertyLabel").length) {
        propertyClient.getPropertyById(propId.val(), setPropLabel, onRequestFailure);
    }

    let currentItemTypeId = $("#currentItemTypeId");
    if(currentItemTypeId.length) {
        itemTypeClient.getItemTypeById(currentItemTypeId.val(), setItemTypeLabel, onRequestFailure);
    }

    if($("#assignValToItemSelect").length) {
        itemClient.getAllItemsMin(fillAllItemsSelect, onRequestFailure);
    }

    if($("#associatedItemsTbody").length) {
        propertyClient.getPropertyValueItemsById($("#propertyValId").val(), fillAssociatedItems, onRequestFailure);
    }

})