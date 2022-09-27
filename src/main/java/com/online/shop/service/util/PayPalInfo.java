package com.online.shop.service.util;

public class PayPalInfo {

    public final String approvalLink;
    public final String paymentId;

    public PayPalInfo(String approvalLink, String paymentId) {
        this.approvalLink = approvalLink;
        this.paymentId = paymentId;
    }

}
