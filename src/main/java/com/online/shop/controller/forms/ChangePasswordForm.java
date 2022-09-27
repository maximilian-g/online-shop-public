package com.online.shop.controller.forms;

public class ChangePasswordForm {

    private final String currentPassword;
    private final String newPassword;
    private final String confirm;

    public ChangePasswordForm(String currentPassword, String newPassword, String confirm) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.confirm = confirm;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getNewPasswordRaw() {
        return newPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    // form considered as valid if passwords are not null and newPassword equals to confirm password
    public boolean isValid() {
        return currentPassword != null && newPassword != null && newPassword.equals(confirm);
    }

    public String getValidationMessage() {
        if(currentPassword == null || currentPassword.isEmpty()) {
            return "Current password must not be blank";
        }
        if(newPassword == null || newPassword.isEmpty()) {
            return "New password must not be blank";
        }
        if(!newPassword.equals(confirm)) {
            return "New password and confirmation password did not match.";
        }
        return "Password form is valid";
    }

}
