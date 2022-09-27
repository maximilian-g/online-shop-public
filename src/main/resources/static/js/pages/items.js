
const defaultItemsPath = "/items";
let searchByFiltersObj
let searchByCategory
let searchByItemType

$(document).ready(function () {

    searchByFiltersObj = {
        button: $("#searchByFilters")
    };
    searchByCategory = $("#searchByCategory");
    searchByItemType = $("#searchByItemType");

    if(searchByCategory.length && searchByItemType.length) {
        onCategoryChanged();
    }

    $(".nextmarker").click(onNextImgClick);
    $(".prevmarker").click(onPrevImgClick);

    let searchByAny = $("#searchByAny");
    let searchByAll = $("#searchByAll");
    if(searchByAny.length && searchByAll.length) {
        searchByAny.click(function() {
            searchByFiltersObj.searchByAll = false;
            onAnyFilterChanged()
        })
        searchByAll.click(function() {
            searchByFiltersObj.searchByAll = true;
            onAnyFilterChanged()
        })
    }

    let findByNameButton = $("#findByNameButton");
    if(findByNameButton.length) {
        findByNameButton.click(onSearchByItemNameClick);
    }

    let carouselInner = $("#carouselInner");
    if(carouselInner.length) {
        let innerElem = carouselInner.children(":first");
        if(innerElem != null && innerElem.length) {
            innerElem.toggleClass("active");
        } else {
            $("#carousel").hide();
            showMessage("There are no photos of this product.")
        }
    }

})

function checkNeededProps(properties) {
    $('*[id*=propValCheckBox_]').each(function() {
        if(properties.includes($(this).val())) {
            let currentPropId = $(this).attr("id");
            document.getElementById(currentPropId).checked = true;
            onPropertySelected($(this).val());
        }
    });
}

function onNextImgClick() {
    let imageId = getStrAfterChar($(this).attr("id"), 'n')
    findAndSetNext(imageId)
}

function onPrevImgClick() {
    let itemId = getStrAfterChar($(this).attr("id"), 'p')
    findAndSetPrev(itemId)
}

function findAndSetNext(itemId) {
    itemClient.getItemImagesById(itemId, onNextImageSuccess, onRequestFailure);
}

function onNextImageSuccess(images, itemId) {
    let imageElement = $("#img_" + itemId);
    let imagePath = getImagePath(imageElement);
    if(images.length === 0) {
        showMessage("Product does not have a photo");
        return;
    }
    if(images.length === 1) {
        showMessage("Product has only one photo");
        return;
    }
    for (let i = 0; i < images.length; i++) {
        let image = images[i];
        if (image.imagePath === imagePath) {
            if (i + 1 !== images.length) {
                // found next image
                imageElement.attr("src", "/api/v1/images/" + images[i + 1].imagePath);
                return;
            }
            // current image is last image
            imageElement.attr("src", "/api/v1/images/" + images[0].imagePath);
        }
    }
}

function getImagePath(imageElement) {
    let src = imageElement.attr("src");
    let lastIndexOfSlash = src.lastIndexOf("/");
    if(lastIndexOfSlash > 0 && lastIndexOfSlash !== src.length) {
        return src.substring(lastIndexOfSlash + 1);
    }
    return src;
}

function findAndSetPrev(itemId) {
    itemClient.getItemImagesById(itemId, onPrevImageSuccess, onRequestFailure);
}

function onPrevImageSuccess(images, itemId) {
    let imageElement = $("#img_" + itemId);
    let imagePath = getImagePath(imageElement);
    if(images.length === 0) {
        showMessage("Product does not have a photo");
        return;
    }
    if(images.length === 1) {
        showMessage("Product has only one photo");
        return;
    }
    for (let i = 0; i < images.length; i++) {
        let image = images[i];
        if (image.imagePath === imagePath) {
            if (i - 1 >= 0) {
                // found prev image
                imageElement.attr("src", "/api/v1/images/" + images[i - 1].imagePath);
                return;
            }
            // current image is first image
            imageElement.attr("src", "/api/v1/images/" + images[images.length - 1].imagePath);
            return;
        }
    }
}

function showMessage(msg) {
    let messageBox = $("#messageBox");
    messageBox.html("");
    messageBox.append("<div class='badge badge-danger'>" + msg + "</div>")
    setTimeout(function() {
        messageBox.html("");
    }, 2500)
}

function getStrAfterChar(str, char) {
    let index = str.lastIndexOf(char);
    if(index > 0 && index !== str.length - 1) {
        return str.substring(index + 1);
    }
    return str;
}

function onCategoryChanged() {

    let categorySelection = $("#categorySelection");
    let categoryId = categorySelection.val();
    if(categoryId != null) {
        searchByFiltersObj.categoryId = categoryId;
        searchByCategory.attr("href", defaultItemsPath + "?categoryId=" + categoryId);
        $("#searchByFilters").hide();

        categoryClient.getCategoryById(categoryId, onGetCategorySuccess, onRequestFailure)
    }

}

function onGetCategorySuccess(response) {
    let typeSelection = $("#typeSelection");
    typeSelection.html("");
    for (let i = 0; i < response.itemTypes.length; i++) {
        let itemType = response.itemTypes[i];
        typeSelection.append("<option value='" + itemType.id + "'>" + itemType.name + "</option>");
    }
    if(itemTypeIdGlobal != null && itemTypeIdGlobal !== -1) {
        typeSelection.val(itemTypeIdGlobal).change();
        // we need to choose item type only once on page load
        itemTypeIdGlobal = null;
    }
    $("#searchByFilters").show();
    searchByFiltersObj.checkedProperties = [];
    onItemTypeChanged();
}

function onItemTypeChanged() {
    let itemTypeSelection = $("#typeSelection");
    let itemTypeId = itemTypeSelection.val();
    if(itemTypeId != null) {
        searchByFiltersObj.itemTypeId = itemTypeId;
        searchByItemType.attr("href", defaultItemsPath + "?itemTypeId=" + itemTypeId);
        onAnyFilterChanged();
        updateProperties(itemTypeId);
    }
}

function onAnyFilterChanged() {
    let path = defaultItemsPath + "?";
    if(searchByFiltersObj.itemTypeId != null) {
        path += "itemTypeId=" + searchByFiltersObj.itemTypeId + "&";
    }
    if(searchByFiltersObj.checkedProperties != null) {
        path += "properties=" + encodeURIComponent(searchByFiltersObj.checkedProperties.join(",")) + "&";
    }
    if(searchByFiltersObj.categoryId != null) {
        path += "categoryId=" + searchByFiltersObj.categoryId + "&";
    }
    if(searchByFiltersObj.searchByAll != null) {
        path += "searchByAll=" + searchByFiltersObj.searchByAll;
    }
    searchByFiltersObj.button.attr("href", path);
}

function updateProperties(itemTypeId) {
    itemTypeClient.getItemTypeById(itemTypeId, onItemTypeSuccess, onRequestFailure);
}

function onItemTypeSuccess(response) {

    //"itemType": {
    //         "itemTypeId": 1,
    //         "name": "Processors",
    //         "category": {
    //             "id": 1,
    //             "name": "Computer devices"
    //         },
    //         "properties": [
    //             {
    //                 "propNameId": 2,
    //                 "name": "Frequency",
    //                 "propertyValues": [
    //                     {
    //                         "propValueId": 2,
    //                         "value": "4.5 GHz"
    //                     }
    //                 ]
    //             }
    //         ]
    //     },

    let propertySelection = $("#propertySelection");
    propertySelection.html("");
    if(response.properties.length > 0) {
        $("#propertySelectionLabel").show();
        $("#searchType").show();
    } else {
        $("#propertySelectionLabel").hide();
    }
    let propertyValuesCount = 0;
    for(let i = 0; i < response.properties.length; i++) {
        let property = response.properties[i];
        propertyValuesCount += property.propertyValues.length;
        if(property.propertyValues.length > 0) {
            propertySelection.append("<h5>" + property.name + "</h5>")
            for (let j = 0; j < property.propertyValues.length; j++) {
                let propertyVal = property.propertyValues[j];
                let propertyInputId = "propValCheckBox_" + propertyVal.propValueId;
                propertySelection.append("<input type='checkbox' id='" + propertyInputId + "' value='" + propertyVal.propValueId +
                    "' onchange='onPropertySelected(" + propertyVal.propValueId + ")'>" +
                    propertyVal.value + "</input><br>")
            }
        }
    }
    console.log("propertyValuesCount = " + propertyValuesCount)
    if(propertyValuesCount === 0) {
        $("#propertySelectionLabel").hide();
        $("#searchType").hide();
    }
    if(propertiesStr && propertySelection) {
        console.log("propertiesStr = " + propertiesStr)
        checkNeededProps(decodeURI(propertiesStr).split(","));
    }
}

function onPropertySelected(elementId) {
    let propElement = $("#propValCheckBox_" + elementId);
    // let propElement = elementId;
    if(propElement.is(":checked")) {
        if (searchByFiltersObj.checkedProperties != null &&
            !searchByFiltersObj.checkedProperties.includes(propElement.val())) {
            searchByFiltersObj.checkedProperties.push(propElement.val());
        } else {
            searchByFiltersObj.checkedProperties = [propElement.val()];
        }
    } else {
        if(searchByFiltersObj.checkedProperties != null) {
            searchByFiltersObj.checkedProperties = searchByFiltersObj.checkedProperties.filter(val => val !== propElement.val());
        }
    }
    onAnyFilterChanged();
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
    $("#searchByFilters").show();
}

function onSearchByItemNameClick() {
    let nameInputVal = $("#itemNameInput").val();
    let searchByContains = $("#searchByContains").is(":checked");
    let finalLocation = defaultItemsPath + "?name=" + nameInputVal;
    if(searchByContains) {
        finalLocation += "&searchByContains=" + searchByContains +
            "&info=" + encodeURIComponent("Searching for all products containing '" + nameInputVal + "'");
    } else {
        finalLocation += "&info=" +
            encodeURIComponent("Searching for item with name '" + nameInputVal + "'")
    }
    window.location = finalLocation
}