package com.online.shop.controller.admin.forms;

import java.util.LinkedList;
import java.util.List;

public class ItemsParameters {
    private Long categoryId;
    private Long itemTypeId;
    private Integer page;
    private Integer size;
    private List<Long> properties = new LinkedList<>();
    private Boolean searchByAll = false;
    private String name = "";
    private Boolean searchByContains = false;
    private String errorMsg = "";
    private String info = "";

    public Long getCategoryId() {
        return categoryId;
    }

    public ItemsParameters setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    public Long getItemTypeId() {
        return itemTypeId;
    }

    public ItemsParameters setItemTypeId(Long itemTypeId) {
        this.itemTypeId = itemTypeId;
        return this;
    }

    public Integer getPage() {
        return page;
    }

    public ItemsParameters setPage(Integer page) {
        this.page = page;
        return this;
    }

    public Integer getSize() {
        return size;
    }

    public ItemsParameters setSize(Integer size) {
        this.size = size;
        return this;
    }

    public List<Long> getProperties() {
        return properties;
    }

    public ItemsParameters setProperties(List<Long> properties) {
        this.properties = properties;
        return this;
    }

    public Boolean getSearchByAll() {
        return searchByAll;
    }

    public ItemsParameters setSearchByAll(Boolean searchByAll) {
        this.searchByAll = searchByAll;
        return this;
    }

    public String getName() {
        return name;
    }

    public ItemsParameters setName(String name) {
        this.name = name;
        return this;
    }

    public Boolean getSearchByContains() {
        return searchByContains;
    }

    public ItemsParameters setSearchByContains(Boolean searchByContains) {
        this.searchByContains = searchByContains;
        return this;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public ItemsParameters setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
        return this;
    }

    public String getInfo() {
        return info;
    }

    public ItemsParameters setInfo(String info) {
        this.info = info;
        return this;
    }
}
