
const categoryAPIBaseURL = "/api/v1/categories/"

const categoryClient = {

    getAllCategories: function(onSuccess, onFailure) {
        let request = $.ajax({
            method: "GET",
            url: categoryAPIBaseURL
        });
        request.done(onSuccess);
        request.fail(onFailure);
    },

    getCategoryById: function(categoryId, onSuccess, onFailure) {
        let request = $.ajax({
            method: "GET",
            url: categoryAPIBaseURL + categoryId
        });
        request.done(onSuccess);
        request.fail(onFailure);
    }

}