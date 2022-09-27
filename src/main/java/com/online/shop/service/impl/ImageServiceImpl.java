package com.online.shop.service.impl;

import com.online.shop.config.PaginationConfig;
import com.online.shop.dto.ImageDto;
import com.online.shop.entity.Image;
import com.online.shop.repository.ImageRepository;
import com.online.shop.service.abstraction.BaseService;
import com.online.shop.service.exception.EntityUpdateException;
import com.online.shop.service.exception.ImageUploadException;
import com.online.shop.service.exception.NotFoundException;
import com.online.shop.service.util.CustomPage;
import com.online.shop.service.util.Pagination;
import com.online.shop.service.util.PaginationInfo;
import com.online.shop.util.HashingUtil;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ImageServiceImpl extends BaseService {

    public static final String IMAGE_ATTRIBUTE_NAME = "image";
    public static final String IMAGES_ATTRIBUTE_NAME = "images";
    public static final String IMAGES_PAGE_ATTRIBUTE_NAME = "imagesPage";

    private final ImageRepository imageRepository;
    private final PaginationConfig paginationConfig;
    @Value("${app.images.folder}")
    private String imagesFolder;
    @Value("${app.images.size.max}")
    private Long maxImageSize;

    @Autowired
    public ImageServiceImpl(ImageRepository imageRepository, Validator validator, PaginationConfig paginationConfig) {
        super(validator, LoggerFactory.getLogger(ImageServiceImpl.class));
        this.imageRepository = imageRepository;
        this.paginationConfig = paginationConfig;
    }

    public Image saveImage(String fileName, byte[] content) {
        File initialDir = new File(imagesFolder);
        try {
            if(!imageRepository.existsByImagePath(fileName)) {
                if(content.length < maxImageSize) {
                    Image image = new Image();
                    image.setImagePath(fileName);
                    Set<ConstraintViolation<Image>> violations = getViolations(image);
                    if (violations.isEmpty()) {
                        if (!initialDir.exists()) {
                            Files.createDirectory(initialDir.toPath());
                        }
                        saveFile(fileName, content);
                        return imageRepository.save(image);
                    } else {
                        throw new ImageUploadException(getErrorMessagesTotal(violations));
                    }
                } else {
                    throw new ImageUploadException("Image with name '" + fileName + "' exceeded maximum file length");
                }
            } else {
                throw new ImageUploadException("Image with name '" + fileName + "' already exists");
            }
        } catch (IOException ex) {
            logger.error("Unable to upload image '" + fileName + "'. Exception msg = " + ex.getMessage());
            throw new ImageUploadException("Unable to upload image '" + fileName + "'");
        }
    }

    public ImageDto getImageDtoById(long id) {
        return ImageDto.getImageDto(getImageById(id));
    }

    public Image getImageById(long id) {
        return imageRepository.findById(id).orElseThrow(this::getImageNotFoundException);
    }

    public byte[] getImageByName(String fileName) {
        Image image = getImageEntityByName(fileName);
        try {
            byte[] file = getFile(image.getImagePath());
            logger.debug("Loaded " + file.length + " bytes for file " + fileName);
            return file;
        } catch (IOException ex) {
            logger.error("Unable to load image '" + fileName + "'. Exception msg = " + ex.getMessage());
            throw getImageNotFoundException();
        }
    }

    public Image updateImage(Image image) {
        Set<ConstraintViolation<Image>> violations = getViolations(image);
        if (violations.isEmpty()) {
            return imageRepository.save(image);
        } else {
            throw new ImageUploadException(getErrorMessagesTotal(violations));
        }
    }

    public Image updateImage(Image image, String newFileName) {
        Set<ConstraintViolation<Image>> violations = getViolations(image);
        if (violations.isEmpty()) {
            try {
                byte[] content = getFile(image.getImagePath());
                deleteFile(image.getImagePath());
                saveFile(newFileName, content);
                image.setImagePath(newFileName);
                return imageRepository.save(image);
            } catch (IOException ex) {
                logger.error("Unable to delete image '" + image.getImagePath() + "'. Exception msg = " + ex.getMessage());
                throw new EntityUpdateException("Unable to remove previous image");
            }
        } else {
            throw new EntityUpdateException(getErrorMessagesTotal(violations));
        }
    }

    protected Image getImageEntityByName(String fileName) {
        return imageRepository.getImageByName(fileName).orElseThrow(this::getImageNotFoundException);
    }

    protected NotFoundException getImageNotFoundException() {
        return new NotFoundException("Image was not found.");
    }

    protected String getSHA1FileName(String fileName) {
        return HashingUtil.getHexStrFromBytes(HashingUtil.getSHA1(fileName));
    }

    protected void saveFile(String fileName, byte[] content) throws IOException {
        String internalFileName = getSHA1FileName(fileName);
        File file = new File(imagesFolder + File.separatorChar + internalFileName);
        logger.info("Saving image to file " + file.getAbsolutePath());
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content);
        }
    }

    protected void deleteFile(String fileName) throws IOException {
        logger.info("Removing file '" + fileName + "'");
        Files.delete(Path.of(imagesFolder, getSHA1FileName(fileName)));
    }

    protected byte[] getFile(String fileName) throws IOException {
        return Files.readAllBytes(Path.of(imagesFolder, getSHA1FileName(fileName)));
    }

    public Pagination<ImageDto> findPaginated(int pageSize, int currentPage) {
        PaginationInfo paginationInfo =
                getPaginationInfo(imageRepository.count(), pageSize, currentPage, getPageSizeProp());
        Page<Image> imagePage = imageRepository.findAll(
                PageRequest.of(paginationInfo.currentPage - 1, paginationInfo.pageSize)
        );

        return new Pagination<>(
                new CustomPage<>(
                        imagePage.getContent().stream().map(ImageDto::getImageDto).collect(Collectors.toList()),
                        imagePage.getSize(),
                        imagePage.getNumber(),
                        imagePage.getTotalPages()
                ),
                getPageNumbers(imagePage.getTotalPages(), paginationInfo.currentPage)
        );
    }

    public int getPageSizeProp() {
        return paginationConfig.getPageSizeProp();
    }

    public List<ImageDto> findAll() {
        return imageRepository.findAll().stream().map(ImageDto::getImageDto).collect(Collectors.toList());
    }

    public void deleteById(Long id) {
        Image image = getImageById(id);
        try {
            deleteFile(image.getImagePath());
            logger.info("Removing Image entity with id " + id);
            imageRepository.deleteById(id);
        } catch (IOException ex) {
            logger.info("Exception during image removal: " + ex.getMessage());
            throw new EntityUpdateException("Unable to delete image");
        }
    }
}
