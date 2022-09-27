let msgCount = 0;
const MSG_DIV_CLASS = 'messageBox'

function onRequestFailure(jqXHR, textStatus) {
    //{"readyState":4,
    // "responseText":
    // "{\"status\":404,
    // \"error_message\":\"Category with specified id was not found.\",
    // \"timestamp\":\"2022-05-25T17:14:21.943+00:00\"}",
    // "responseJSON":{"status":404,"responseMessage":"Category with specified id was not found.","timestamp":"2022-05-25T17:14:21.943+00:00"},
    // "status":404,
    // "statusText":"error"}

    showErrorMessage(jqXHR.responseJSON.status +
        ", " + jqXHR.responseJSON.responseMessage);
}

function showSuccessMessage(msg) {
    showMessage(msg, "badge-success", MSG_DIV_CLASS)
}

function showErrorMessage(msg) {
    showMessage(msg, "badge-danger", MSG_DIV_CLASS)
}

function showSuccessMessageInDiv(msg, msgDiv) {
    showMessage(msg, "badge-success", msgDiv)
}

function showErrorMessageInDiv(msg, msgDiv) {
    showMessage(msg, "badge-danger", msgDiv)
}

function showMessage(msg, badgeClass, msgDivClass) {
    let messageBox = $("#" + msgDivClass);
    let currentMsgCount = msgCount++;
    messageBox.append("<div id='msg" + currentMsgCount + "' class='badge " + badgeClass + "'>" + msg + "</div>" +
        "<br id='msg" + currentMsgCount + "br'>")
    setTimeout(function() {
        $("#msg" + currentMsgCount).remove();
        $("#msg" + currentMsgCount + "br").remove();
    }, 5000);
}

function getMonthDays() {
    return [
        31, // January
        28, // February
        31, // March
        30, // April
        31, // May
        30, // June
        31, // July
        31, // August
        30, // September
        31, // October
        30, // November
        31  // December
    ];
}

function isLeapYear(year) {
    return (year % 4 === 0 && year % 100 !== 0) || year % 400 === 0;
}

function getLeadingZero(a) {
    let intA = parseInt(a);
    if (intA < 10) {
        return '0' + intA;
    }
    return intA;
}

// yyyy-MM-dd HH:mm:ss
function getDateTimeString(date) {
    return date.getFullYear() + "-" +
        getLeadingZero(date.getMonth() + 1) + "-" +
        getLeadingZero(date.getDate()) + " " +
        getLeadingZero(date.getHours()) + ":" +
        getLeadingZero(date.getMinutes()) + ":" +
        getLeadingZero(date.getSeconds());
}