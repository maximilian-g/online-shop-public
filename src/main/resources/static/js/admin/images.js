$(document).ready(function () {
    /* Upload single File   */
    $("#uploadFile").submit(function (event) {
        event.preventDefault();
        $.ajax({
            type: 'POST',
            enctype: 'multipart/form-data',
            url: '/api/v1/images/upload',
            data: new FormData(this),
            contentType: false,
            cache: false,
            processData: false,
            success: function (response) {
                $("#uploadResult").html("<li>Uploaded: <a href='/api/v1/images/" + response.imagePath + "'>" + response.imagePath + "</a></li>");
            },
            error: processError
        });
    });

    $("#changeImage").submit(function(event) {
        event.preventDefault();
        console.log(JSON.stringify({
            imageId: $("#imageId").val(),
            imagePath: $("#imageName").val(),
            itemId: $("#itemSelectionUpdate").find(":selected").val()
        }));
        $.ajax( {
            type: 'PUT',
            url: '/api/v1/images/edit',
            data: JSON.stringify({
                imageId: $("#imageId").val(),
                imagePath: $("#imageName").val(),
                itemId: $("#itemSelectionUpdate").find(":selected").val()
            }),
            contentType: "application/json",
            cache: false,
            processData: false,
            success: function(response) {
                $("#changeResult").html("<li>Changed: <a href='/api/v1/images/" + response.imagePath + "'>" + response.imagePath + "</a></li>");
            },
            error: processError
        });
    });

    $("#deleteImage").submit(function(event) {
        event.preventDefault();
        let imageId = $("#imageToDeleteId").val();
        $.ajax( {
            type: 'DELETE',
            url: '/api/v1/images/' + imageId,
            contentType: false,
            cache: false,
            processData: false,
            success: function(response) {
                $("#changeResult").html("<li class='badge badge-success'>" + response.responseMessage + "</li>");
                setTimeout(function() {
                    window.location = "/admin/image";
                }, 2000);
            },
            error: processError
        });
    });
});

function processError(error) {
    let errorObj = JSON.parse(error.responseText);
    let errorMessage = errorObj.responseMessage;
    $("#uploadResult").append("<li><div class='badge badge-danger'>Unable to upload photo: " + errorMessage + "</div></li>")
    console.log(error);
}