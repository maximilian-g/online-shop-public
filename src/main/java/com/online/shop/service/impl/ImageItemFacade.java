package com.online.shop.service.impl;

import com.online.shop.dto.ImageDto;
import com.online.shop.entity.Image;
import com.online.shop.entity.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ImageItemFacade {

    private final ItemServiceImpl itemService;
    private final ImageServiceImpl imageService;
    private final Logger logger;

    public ImageItemFacade(ItemServiceImpl itemService, ImageServiceImpl imageService) {
        this.itemService = itemService;
        this.imageService = imageService;
        this.logger = LoggerFactory.getLogger(ImageItemFacade.class);
    }

    public byte[] getImage(String fileName) {
        logger.debug("Getting '" + fileName + "'");
        return imageService.getImageByName(fileName);
    }

    public Image saveImage(String fileName, byte[] content) {
        logger.info("Saving '" + fileName + "'");
        return imageService.saveImage(fileName, content);

    }

    public Image saveImage(String fileName, byte[] content, long itemId) {
        Item item = itemService.getItemById(itemId);
        logger.info("Saving '" + fileName + "' for item '" + item.getName() + "' (" + itemId + ")");
        Image image = imageService.saveImage(fileName, content);
        item.getImages().add(image);
        item = itemService.updateItem(item);
        image.setItem(item);
        return imageService.updateImage(image);
    }

    public Image updateImage(ImageDto image) {
        Image imageEntity = imageService.getImageById(image.getImageId());
        if(image.getItemId() != null &&
                (imageEntity.getItem() == null || !imageEntity.getItem().getId().equals(image.getItemId()))) {
            Item item = itemService.getItemById(image.getItemId());
            logger.info("Attaching image '" + imageEntity.getId() + "' to item '" + item.getName() + "'");
            imageEntity.setItem(item);
        }
        if(!image.getImagePath().equals(imageEntity.getImagePath())) {
            logger.info("Updating image path from '" + imageEntity.getImagePath() + "' to '" + image.getImagePath() + "'");
            return imageService.updateImage(imageEntity, image.getImagePath());
        }
        return imageService.updateImage(imageEntity);
    }

    public void deleteImage(Long id) {
        imageService.deleteById(id);
    }

}
