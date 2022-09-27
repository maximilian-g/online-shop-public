package com.online.shop.service.impl;

import com.online.shop.config.PaginationConfig;
import com.online.shop.controller.admin.forms.PriceUpdateForm;
import com.online.shop.dto.PriceDto;
import com.online.shop.entity.Item;
import com.online.shop.entity.OrderItem;
import com.online.shop.entity.Price;
import com.online.shop.repository.PriceRepository;
import com.online.shop.service.abstraction.BaseService;
import com.online.shop.service.abstraction.PriceService;
import com.online.shop.service.exception.BusinessException;
import com.online.shop.service.exception.EntityUpdateException;
import com.online.shop.service.exception.NotFoundException;
import com.online.shop.service.exception.PriceCreationException;
import com.online.shop.service.util.CustomPage;
import com.online.shop.service.util.Pagination;
import com.online.shop.service.util.PaginationInfo;
import com.online.shop.util.DateUtil;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class PriceServiceImpl extends BaseService implements PriceService {

    private final PriceRepository priceRepository;
    private final PaginationConfig paginationConfig;

    public static final String PRICES_ATTRIBUTE_NAME = "prices";
    public static final String SINGLE_PRICE_ATTRIBUTE_NAME = "price";
    public static final String PRICES_PAGE_ATTRIBUTE_NAME = "pricePage";

    @Autowired
    protected PriceServiceImpl(Validator validator,
                               PriceRepository priceRepository,
                               PaginationConfig paginationConfig) {
        super(validator, LoggerFactory.getLogger(PriceServiceImpl.class));
        this.priceRepository = priceRepository;
        this.paginationConfig = paginationConfig;
    }

    @Override
    public Pagination<PriceDto> findPaginated(int pageSize, int currentPage) {
        PaginationInfo paginationInfo =
                getPaginationInfo(priceRepository.count(), pageSize, currentPage,
                        paginationConfig.getPageSizeProp());
        Page<Price> pricePage = priceRepository.findAll(
                PageRequest.of(paginationInfo.currentPage - 1, paginationInfo.pageSize)
        );
        return new Pagination<>(
                new CustomPage<>(
                        pricePage.getContent().stream().map(PriceDto::getPriceDto).collect(Collectors.toList()),
                        pricePage.getSize(),
                        pricePage.getNumber(),
                        pricePage.getTotalPages()
                ),
                getPageNumbers(pricePage.getTotalPages(),
                        paginationInfo.currentPage));
    }

    @Override
    public Collection<PriceDto> getAllPrices() {
        return priceRepository.findAll().stream().map(PriceDto::getPriceDto).collect(Collectors.toList());
    }

    @Override
    public PriceDto getById(Long id) {
        return PriceDto.getPriceDto(getPriceById(id));
    }

    @Override
    public PriceDto createPrice(Price price) {
        return PriceDto.getPriceDto(createPriceEntity(price));
    }

    @Override
    public PriceDto updatePrice(Price price) {
        return PriceDto.getPriceDto(updatePriceEntity(price));
    }

    @Override
    public PriceDto detachItemFromPrice(Long priceId) {
        return PriceDto.getPriceDto(detachItemFromPriceEntity(priceId));
    }

    @Override
    public int getPageSizeProp() {
        return paginationConfig.getPageSizeProp();
    }

    @Override
    public boolean existsById(Long id) {
        return priceRepository.existsById(id);
    }

    public Price getPriceById(Long id) {
        return priceRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Cannot find price with id '" + id + "'.")
        );
    }

    public Price getPriceByIdForUpdate(Long id) {
        return priceRepository.getPriceByIdForUpdate(id).orElseThrow(
                () -> new NotFoundException("Cannot find price with id '" + id + "'.")
        );
    }

    public Price createPriceEntity(Price price) {
        return savePrice(price, new PriceCreationException(""));
    }

    public Price updatePriceEntity(Price price) {
        return savePrice(price, new EntityUpdateException(""));
    }

    public Price updatePriceFieldsByForm(Price entity, PriceUpdateForm form) {
        try {
            boolean isUsedInOrder = false;
            // price cannot be updated if price is used in some order
            if (!isPriceAlreadyUsedInOrder(entity)) {
                entity.setPrice(form.price);
            } else {
                isUsedInOrder = true;
            }
            if (!form.startDate.isBlank()) {
                Date startDateObj = DateUtil.getDateFromString(form.startDate);
                if(isUsedInOrder) {
                    checkStartDateCompatibilityWithOrder(entity, startDateObj);
                }
                entity.setStartDate(startDateObj);
            }
            Date endDateObj = null;
            if (form.endDate != null && !form.endDate.isBlank()) {
                endDateObj = DateUtil.getDateFromString(form.endDate);
                if(isUsedInOrder) {
                    checkEndDateCompatibilityWithOrder(entity, endDateObj);
                }
            }
            if(isUsedInOrder) {
                checkAllPricesCompatibility(entity);
            }
            entity.setEndDate(endDateObj);
            return entity;
        } catch (ParseException ex) {
            throw new EntityUpdateException("Cannot parse dates '" + form.startDate + "' or '"
                    + (form.endDate != null ? form.endDate : "") + "'. " +
                    "Dates must be in format '" + DateUtil.CURRENT_DATE_FORMAT + "'.");
        }
    }

    public void deletePriceById(Long id) {
        Price priceEntity = getPriceById(id);
        if (priceEntity.getItem() != null && isPriceAlreadyUsedInOrder(priceEntity)) {
            throw new EntityUpdateException("Cannot delete price, which is already used in existent order.");
        }
        priceRepository.delete(priceEntity);
    }


    public Price detachItemFromPriceEntity(Long priceId) {
        Price price = getPriceById(priceId);
        if(price.getItem() != null && !isPriceAlreadyUsedInOrder(price)) {
            price.setItem(null);
            return priceRepository.save(price);
        } else if(price.getItem() == null) {
            return price;
        }
        throw new EntityUpdateException("Cannot detach item from price, item is already used in some order.");
    }

    public void removeAllPricesFromItem(Item itemToDelete) {
        for(Price price : itemToDelete.getPrices()) {
            price.setItem(null);
            updatePrice(price);
        }
    }

    public static boolean isPriceAlreadyUsedInOrder(Price price) {
        Item item = price.getItem();
        if (item != null) {
            return item.getOrders().stream()
                    .anyMatch(orderItem -> isInPriceRangeStrictly(orderItem.getOrder().getStartDate(), price));
        }
        return false;
    }

    // prices are considered compatible if their interval does not intersect
    public static boolean areCompatible(Price left, Price right) {
        return (left.getEndDate() == null && right.getEndDate() != null && left.getStartDate().after(right.getEndDate())) ||
                (right.getEndDate() == null && left.getEndDate() != null && right.getStartDate().after(left.getEndDate())) ||
                (left.getStartDate().before(right.getStartDate()) &&
                        left.getEndDate() != null &&
                        left.getEndDate().before(right.getStartDate())) ||
                (left.getStartDate().after(right.getStartDate()) &&
                        right.getEndDate() != null &&
                        right.getEndDate().before(left.getStartDate()));
    }

    public static boolean isInPriceRangeStrictly(Date dateToCheck, Price price) {
        return dateToCheck.after(price.getStartDate()) &&
                (price.getEndDate() == null || dateToCheck.before(price.getEndDate()));
    }

    protected void checkEndDateCompatibilityWithOrder(Price price, Date endDate) {
        Item item = price.getItem();
        for (OrderItem oi : item.getOrders()) {
            // end date of price cannot be earlier than max order date
            if (oi.getOrder().getStartDate().after(endDate)) {
                throw new EntityUpdateException("Cannot change end date to '" + endDate +
                        "', end date does not intersect with all orders made by this price.");
            }
        }
    }

    protected void checkStartDateCompatibilityWithOrder(Price price, Date startDate) {
        Item item = price.getItem();
        for (OrderItem oi : item.getOrders()) {
            // end date of price cannot be earlier than max order date
            if (oi.getOrder().getStartDate().before(startDate)) {
                throw new EntityUpdateException("Cannot change start date to '" + startDate +
                        "', start date does not intersect with all orders made by this price.");
            }
        }
    }

    protected void checkAllPricesCompatibility(Price entity) {
        Item item = entity.getItem();
        for (Price itemPrice : item.getPrices()) {
            // if it is not same price and it is not compatible with other prices on current item,
            // exception will be thrown
            if (!entity.getId().equals(itemPrice.getId()) &&
                    !PriceServiceImpl.areCompatible(entity, itemPrice)) {
                throw new EntityUpdateException("Current price and item prices are not compatible.");
            }
        }

    }

    protected Price savePrice(Price price, BusinessException exceptionToThrow) {
        Set<ConstraintViolation<Price>> violations = getViolations(price);
        if (violations.isEmpty()) {
            if (price.getEndDate() != null && price.getEndDate().before(price.getStartDate())) {
                exceptionToThrow.setMessage("Start date must be before end date.");
                throw exceptionToThrow;
            }
            price.setPrice(price.getPrice().setScale(2, RoundingMode.HALF_UP));
            return priceRepository.saveAndFlush(price);
        } else {
            exceptionToThrow.setMessage(getErrorMessagesTotal(violations));
            throw exceptionToThrow;
        }
    }

}
