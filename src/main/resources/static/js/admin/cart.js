
$(document).ready(function() {
    let searchButton = $("#searchButton");
    let userSelection = $("#userSelection");
    if(userSelection.length && searchButton.length) {
        searchButton.click(function() {
            window.location = "/admin/cart/" + userSelection.val();
        })
    }

});