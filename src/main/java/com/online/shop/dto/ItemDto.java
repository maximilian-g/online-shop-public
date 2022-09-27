package com.online.shop.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.online.shop.dto.transfer.Exists;
import com.online.shop.dto.transfer.New;
import com.online.shop.entity.Item;
import com.online.shop.entity.Price;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ItemDto {

    @Null(groups = {New.class})
    @NotNull(groups = {Exists.class})
    private Long id;
    @NotNull(message = "Item name {app.validation.notnull.msg}")
    @Size(min = 3, max = 45, message = "Item name {app.validation.size.msg} but provided ${validatedValue}")
    private String name;
    @Size(min = 3, max = 255, message = "Description {app.validation.size.msg} but provided ${validatedValue}")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;
    private String article;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PriceDto price;
    @NotNull
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ItemTypeDto itemType;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ImageDto> images;
    private Boolean isInStock = false;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ItemPropertyDto> properties = new ArrayList<>();

    public static ItemDto getItemDto(Item item) {
        return getItemDto(item, new Date());
    }

    public static ItemDto getItemDto(Item item, Date priceDate) {
        return getItemDto(item, priceDate, new ItemDto());
    }

    public static ItemDto getItemDto(Item item, Date priceDate, ItemDto dtoToFill) {
        Price itemPrice = item.getPriceForDate(priceDate);
        PriceDto price = null;
        if(itemPrice != null) {
            price = PriceDto.getPriceDto(itemPrice);
        }
        dtoToFill.setId(item.getId());
        dtoToFill.setName(item.getName());
        dtoToFill.setDescription(item.getDescription());
        dtoToFill.setPrice(price);
        dtoToFill.setItemType(ItemTypeDto.getItemTypeDto(item.getType()));
        dtoToFill.setInStock(item.getQuantity() > 0);
        dtoToFill.setImages(ImageDto.getImageDtos(item));
        dtoToFill.setProperties(
                item.getPropertyValues()
                        .stream()
                        .map(pv -> new ItemPropertyDto(
                                pv.getPropertyValue().getProperty().getId(),
                                pv.getPropertyValue().getProperty().getName(),
                                pv.getPropertyValue().getId(),
                                pv.getPropertyValue().getValue()
                        ))
                        .collect(Collectors.toList())
        );
        dtoToFill.setArticle(dtoToFill.getItemType().getCategory().getId() + "" +
                dtoToFill.getItemType().getItemTypeId() +
                dtoToFill.getId());
        return dtoToFill;
    }

    public static Collection<ItemDto> getItemDtoCollection(Collection<Item> items) {
        Collection<ItemDto> result = new ArrayList<>(items.size());
        for (Item item : items) {
            result.add(getItemDto(item));
        }
        return result;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PriceDto getPrice() {
        return price;
    }

    public void setPrice(PriceDto price) {
        this.price = price;
    }

    public ItemTypeDto getItemType() {
        return itemType;
    }

    public void setItemType(ItemTypeDto itemType) {
        this.itemType = itemType;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean isInStock() {
        return isInStock;
    }

    public void setInStock(Boolean inStock) {
        isInStock = inStock;
    }

    public List<ImageDto> getImages() {
        return images;
    }

    public void setImages(List<ImageDto> images) {
        this.images = images;
    }

    public List<ItemPropertyDto> getProperties() {
        return properties;
    }

    public void setProperties(List<ItemPropertyDto> properties) {
        this.properties = properties;
    }

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }
}
