package com.online.shop.controller.forms;

public class RecoverPasswordForm {

    public final String newPassword;
    public final String confirm;
    public final String recoveryId;

    public RecoverPasswordForm(String newPassword, String confirm, String recoveryId) {
        this.newPassword = newPassword;
        this.confirm = confirm;
        this.recoveryId = recoveryId;
    }
}
