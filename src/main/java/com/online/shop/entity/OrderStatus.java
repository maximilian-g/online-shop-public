package com.online.shop.entity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public enum OrderStatus {
    CREATED("CREATED", "Order is created", 0),
    WAITING_FOR_PAYMENT("WAITING FOR PAYMENT", "Waiting for payment", 1),
    CANCELLED("CANCELLED", "Order is cancelled", 2),
    PAID("PAID", "Order is paid successfully", 3),
    SENT("SENT", "Items are sent", 4),
    DELIVERED("DELIVERED", "Items are delivered", 5),
    COMPLETED("COMPLETED", "Order is completed, items are delivered", 6);

    private final String status;
    private final String description;
    // represents natural order of order statuses
    private final int sequenceNumber;
    private static final HashSet<String> availableStatuses = new HashSet<>();
    private static final HashMap<String, OrderStatus> availableStatusesMap = new HashMap<>();

    static {
        for(OrderStatus status : OrderStatus.values()) {
            availableStatuses.add(status.getStatus());
            availableStatusesMap.put(status.getStatus(), status);
        }
    }

    public static OrderStatus getStatusObject(String status) {
        return availableStatusesMap.get(status);
    }

    public static boolean isValidStatus(String status) {
        return status != null &&
                availableStatuses.contains(status);
    }

    public static List<String> getStatuses() {
        return Arrays.stream(OrderStatus.values()).map(OrderStatus::getStatus).collect(Collectors.toList());
    }

    public String getStatus() {
        return status;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public String getDescription() {
        return description;
    }

    OrderStatus(String status, String description, int sequenceNumber) {
        this.status = status;
        this.description = description;
        this.sequenceNumber = sequenceNumber;
    }

}
