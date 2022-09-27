package com.online.shop.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.online.shop.entity.Image;
import com.online.shop.entity.Item;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class ImageDto {

    @NotNull
    private long imageId;
    @NotNull
    private String imagePath;
    @NotNull
    private Long itemId;
    @JsonIgnore
    private String itemName;

    public static List<ImageDto> getImageDtos(Item item) {
        if(item.getImages() != null) {
            List<ImageDto> result = new ArrayList<>(item.getImages().size());
            for (Image image : item.getImages()) {
                ImageDto imageDto = new ImageDto();
                imageDto.setImageId(image.getId());
                imageDto.setImagePath(image.getImagePath());
                imageDto.setItemId(image.getItem().getId());
                imageDto.setItemName(item.getName());
                result.add(imageDto);
            }
            return result;
        }
        return new ArrayList<>();
    }

    public static ImageDto getImageDto(Image image) {
        ImageDto imageDto = new ImageDto();
        imageDto.setImageId(image.getId());
        imageDto.setImagePath(image.getImagePath());
        imageDto.setItemId(image.getItem() == null ? null : image.getItem().getId());
        imageDto.setItemName(image.getItem() == null ? null : image.getItem().getName());
        return imageDto;
    }

    public long getImageId() {
        return imageId;
    }

    public void setImageId(long imageId) {
        this.imageId = imageId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
}
