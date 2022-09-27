$(document).ready(function () {
        if (currentPage !== "login" || currentPage !== "register") {
            $("#" + currentPage).attr("class", $(this).attr("class") + " active");
        }
        $(".navbar-text").text(currentPageText);
        // we need to show login and register buttons only when user is not authenticated
        if ((isAuthenticated == null || !isAuthenticated) &&
            currentPage !== "login" &&
            currentPage !== "access_denied" &&
            currentPage !== "error") {
            $("#login").show();
            $("#register").show();
        } else if (currentPage !== "login" &&
            currentPage !== "access_denied" &&
            currentPage !== "error") {
            $("#logout").show();
        }
        if (currentPage === "login") {
            $("#register").show();
        } else if (currentPage === "register") {
            $("#login").show();
        }
        let main = $("#main");
        let classAttr = main.attr("class");
        main.attr("class", classAttr + " mb-5");

        if(isAuthenticated != null && isAuthenticated) {
            userClient.getCurrentUserAuthorities(onGetAuthoritiesSuccess, function (jqXHR, textStatus) {
                console.log("Response status: " + jqXHR.responseJSON.status);
            })
        }

    }
);

function onGetAuthoritiesSuccess(authorities) {
    console.log("Got " + JSON.stringify(authorities));
    let navbar = $("#navbar");
    for (let i = 0; i < authorities.length; i++) {
        let authority = authorities[i];
        if (authority.authority === 'OPERATOR') {
            navbar.append("<li id='operator' class='nav-item ml-1 mr-1'>" +
                "<a class='nav-link btn btn-success' href='/operator'>Operator panel " +
                "<span class='fas fa-edit'></span></a>" +
                "</li>")
        }
        if (authority.authority === 'ADMIN') {
            navbar.append("<li id='admin' class='nav-item'>" +
                "<a class='nav-link btn btn-danger' href='/admin'>Admin panel " +
                "<span class='fas fa-jedi'></span></a>" +
                "</li>")
        }
    }
}