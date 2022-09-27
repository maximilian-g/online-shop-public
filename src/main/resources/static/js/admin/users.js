$(document).ready(function() {

    let emailChangeButton = $("#emailChangeButton");
    let emailChangeInput = $("#changeEmailInput");
    let emailShowInput = $("#emailShow");
    let idInput = $("#idShow");
    if(emailChangeButton.length && idInput.length) {
        emailChangeInput.val(emailShowInput.val());
        emailChangeButton.click(function () {
            let newEmail = $("#changeEmailInput").val();
            userClient.changeEmailForUser(idInput.val(), newEmail, onEmailChangeSuccess, onRequestFailure)
        })
    }

    let searchByIdButton = $("#searchById")
    if(searchByIdButton.length) {
        searchByIdButton.click(function() {
            let searchIdInput = $("#userSearchId");
            if(searchIdInput.length && searchIdInput.val() != null) {
                window.location = "/admin/user/" + searchIdInput.val();
            }
        });
    }

    let userSelectElement = $("#userSelect");
    let userSelectButton = $("#searchBySelect");
    if(userSelectElement.length && userSelectButton.length) {
        getUsersMin(onGetUsersSuccess, onRequestFailure);
    }

});


function getUsersMin(onSuccess, onFailure) {
    let request = $.ajax({
        method: "GET",
        url: userAPIBaseURL + "/min"
    });
    request.done(onSuccess);
    request.fail(onFailure);
}

function onGetUsersSuccess(response) {

    let userSelectElement = $("#userSelect");
    for(let i = 0; i < response.length; i++) {
        let user = response[i];
        userSelectElement.append("<option value='" + user.id + "'>" + user.username + "(" + user.id + ")</option>");
    }

    $("#searchBySelect").click(function() {
        if(userSelectElement.val() != null) {
            window.location = "/admin/user/" + userSelectElement.val();
        }
    })
}

function onEmailChangeSuccess(response) {
    $("#emailShow").val(response.email);
    showSuccessMessage("Email successfully changed.");
}