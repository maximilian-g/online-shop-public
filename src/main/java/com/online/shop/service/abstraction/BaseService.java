package com.online.shop.service.abstraction;

import com.online.shop.service.exception.EntityUpdateException;
import com.online.shop.service.util.PaginationInfo;
import org.slf4j.Logger;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class BaseService extends LoggableService {

    private final Validator validator;

    public static final String ERROR_ATTRIBUTE_NAME = "error";
    public static final String INFO_MESSAGE_ATTRIBUTE_NAME = "msg";
    public static final String PAGE_NUMBERS_ATTRIBUTE_NAME = "pageNumbers";

    protected BaseService(Validator validator, Logger logger) {
        super(logger);
        this.validator = validator;
    }

    protected <T> Set<ConstraintViolation<T>> getViolations(T entity) {
        return validator.validate(entity);
    }

    protected <T> String getErrorMessagesTotal(Set<ConstraintViolation<T>> violations) {
        List<String> errorMessages = new ArrayList<>(violations.size());
        for (ConstraintViolation<T> violation : violations) {
            errorMessages.add(violation.getMessage());
        }
        return String.join(", ", errorMessages) + ".";
    }

    protected List<Integer> getPageNumbers(int totalPages, int currentPage) {
        List<Integer> pageNumbers = Collections.emptyList();
        if (totalPages > 0) {
            pageNumbers = IntStream
                    .rangeClosed(currentPage > 2 ? currentPage - 2 : currentPage,
                            currentPage + 2 > totalPages ? currentPage : currentPage + 2)
                    .boxed()
                    .collect(Collectors.toList());
            if(!pageNumbers.contains(totalPages)) {
                pageNumbers.add(totalPages);
            }
            if(!pageNumbers.contains(1)) {
                pageNumbers.add(0, 1);
            }
        }
        return pageNumbers;
    }

    protected PaginationInfo getPaginationInfo(long totalItems, int pageSize, int currentPage, int defaultPageSize) {
        if (pageSize * currentPage - pageSize > totalItems || pageSize * currentPage <= 0) {
            currentPage = 1;
        }
        if (pageSize * currentPage <= 0) {
            pageSize = defaultPageSize;
        }
        return new PaginationInfo(pageSize, currentPage);
    }

    protected <T> T saveIfValidOrThrow(T entity, JpaRepository<T, Long> repository) {
        Set<ConstraintViolation<T>> violations = getViolations(entity);
        if(violations.isEmpty()) {
            return repository.save(entity);
        }
        throw new EntityUpdateException(getErrorMessagesTotal(violations));
    }

}
