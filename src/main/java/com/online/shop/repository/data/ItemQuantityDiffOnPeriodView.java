package com.online.shop.repository.data;

public interface ItemQuantityDiffOnPeriodView {

    Long getItemId();

    String getItemName();

    Long getQuantityOnStartDate();

    Long getQuantityOnEndDate();

    Long getDifference();

}
