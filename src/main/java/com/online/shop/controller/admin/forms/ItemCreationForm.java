package com.online.shop.controller.admin.forms;

public class ItemCreationForm {
    private String itemName;
    private String description;
    private Long itemTypeId;

    public ItemCreationForm() {
    }

    public ItemCreationForm(String itemName, String description, Long itemTypeId) {
        this.itemName = itemName;
        this.description = description;
        this.itemTypeId = itemTypeId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getItemTypeId() {
        return itemTypeId;
    }

    public void setItemTypeId(Long itemTypeId) {
        this.itemTypeId = itemTypeId;
    }

}
