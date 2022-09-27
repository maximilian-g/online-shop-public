
const userAPIBaseURL = "/api/v1/users"

const userClient = {

    getCurrentUser: function(onSuccess, onFailure) {
        let request = $.ajax({
            method: "GET",
            url: userAPIBaseURL
        });
        request.done(onSuccess);
        request.fail(onFailure);
    },

    getCurrentUserAuthorities: function(onSuccess, onFailure) {
        let request = $.ajax({
            method: "GET",
            url: userAPIBaseURL + "/authorities"
        });
        request.done(onSuccess);
        request.fail(onFailure);
    },

    changeEmailForUser: function(userId, newEmail, onSuccess, onFailure) {
        let request = $.ajax({
            method: "POST",
            url: userAPIBaseURL + "/" + userId + "/email",
            contentType: "application/json",
            data: JSON.stringify({email: newEmail})
        });
        request.done(onSuccess);
        request.fail(onFailure);
    }

}