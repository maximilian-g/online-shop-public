package com.online.shop.api;

import com.online.shop.api.util.ResponseObject;
import com.online.shop.dto.ImageDto;
import com.online.shop.entity.Image;
import com.online.shop.service.exception.ImageUploadException;
import com.online.shop.service.impl.ImageItemFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/images")
public class ImageRestController extends BaseRestController {

    private final ImageItemFacade imageItemFacade;
    private final Logger logger;

    @Autowired
    public ImageRestController(ImageItemFacade imageItemFacade) {
        this.imageItemFacade = imageItemFacade;
        logger = LoggerFactory.getLogger(ImageRestController.class);
    }

    @GetMapping(value = "/{fileName}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getImage(@PathVariable String fileName) {
        return ResponseEntity.ok(imageItemFacade.getImage(fileName));
    }

    @PostMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ImageDto> uploadImage(@RequestParam(name = "file") MultipartFile file,
                                                @RequestParam(name = "itemId", defaultValue = "-1L") long itemId) {
        try {
            Image image;
            if(itemId == -1L) {
                image = imageItemFacade.saveImage(file.getOriginalFilename(), file.getInputStream().readAllBytes());
            } else {
                image = imageItemFacade.saveImage(file.getOriginalFilename(), file.getInputStream().readAllBytes(), itemId);
            }
            ImageDto imageDto = new ImageDto();
            imageDto.setImageId(image.getId());
            imageDto.setImagePath(image.getImagePath());
            imageDto.setItemId(image.getItem().getId());
            return new ResponseEntity<>(imageDto, HttpStatus.CREATED);
        } catch (IOException ex) {
            logger.error("Error during file upload. " + ex.getMessage());
            throw new ImageUploadException("Could not upload image");
        }
    }

    @PutMapping(value = "/edit", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ImageDto> uploadImage(@RequestBody @Validated ImageDto image) {
        ImageDto response = ImageDto.getImageDto(imageItemFacade.updateImage(image));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseObject> uploadImage(@PathVariable Long id) {
        imageItemFacade.deleteImage(id);
        return new ResponseEntity<>(new ResponseObject(true, "Image with id '" + id + "' successfully deleted"), HttpStatus.OK);
    }

}
