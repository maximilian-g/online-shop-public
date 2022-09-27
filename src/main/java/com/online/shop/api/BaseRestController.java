package com.online.shop.api;

import com.online.shop.api.exception.RestException;
import com.online.shop.api.util.ResponseObject;
import com.online.shop.entity.User;
import com.online.shop.security.config.SecurityConfig;
import com.online.shop.service.exception.BaseException;
import com.online.shop.service.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BaseRestController {

    public static final String ERROR_HEADER_NAME = "error";
    public static final String ERROR_MESSAGE_NAME = "error_message";

    protected String getCurrentUserUsername() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        // in this case user is not authenticated, but authentication object still will return user with username "anonymousUser"
        if(authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        if(authentication.getPrincipal() instanceof User) {
            return ((User) authentication.getPrincipal()).getUsername();
        } else if(authentication.getPrincipal() instanceof String) {
            return (String) authentication.getPrincipal();
        }
        return null;
    }

    protected boolean isAdmin(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream().anyMatch(role -> role.getAuthority().equals(SecurityConfig.getAdminAuthority().getAuthority()));
    }

    @ExceptionHandler({BusinessException.class,
            RestException.class,
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ResponseObject> handleException(Exception ex) {
        if(ex instanceof BaseException) {
            return handleBaseException((BaseException) ex);
        }
        if(ex instanceof MethodArgumentNotValidException) {
            return handleMethodArgNotValidEx((MethodArgumentNotValidException) ex);
        }
        if(ex instanceof MethodArgumentTypeMismatchException) {
            return handleMethodArgumentMismatch((MethodArgumentTypeMismatchException) ex);
        }
        if(ex instanceof MissingServletRequestParameterException) {
            return handleRequestParamException((MissingServletRequestParameterException) ex);
        }
        return handleConstraintViolationEx((ConstraintViolationException) ex);
    }

    protected ResponseEntity<ResponseObject> handleMethodArgNotValidEx(MethodArgumentNotValidException exception) {
        StringBuilder message = new StringBuilder();
        for (ObjectError error : exception.getBindingResult().getAllErrors()) {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            message.append("Field '").append(fieldName).append("', error: ").append(errorMessage).append(". ");
        }
        ResponseObject errorObject = new ResponseObject(false,
                HttpStatus.BAD_REQUEST.value(),
                message.toString());
        return new ResponseEntity<>(errorObject, HttpStatus.BAD_REQUEST);
    }

    protected ResponseEntity<ResponseObject> handleBaseException(BaseException exception) {
        ResponseObject errorObject = new ResponseObject(false,
                exception.getStatus().value(),
                exception.getMessage());
        return new ResponseEntity<>(errorObject, exception.getStatus());
    }

    protected ResponseEntity<ResponseObject> handleConstraintViolationEx(ConstraintViolationException exception) {
        List<String> errorMessages = new ArrayList<>(exception.getConstraintViolations().size());
        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            errorMessages.add(violation.getPropertyPath().toString() + " " + violation.getMessage());
        }
        String finalMsg = String.join(", ", errorMessages) + ".";
        ResponseObject errorObject = new ResponseObject(false,
                HttpStatus.BAD_REQUEST.value(),
                finalMsg);
        return new ResponseEntity<>(errorObject, HttpStatus.BAD_REQUEST);
    }

    protected ResponseEntity<ResponseObject> handleMethodArgumentMismatch(MethodArgumentTypeMismatchException ex) {
        return new ResponseEntity<>(new ResponseObject(
                false,
                "Invalid parameter '" + ex.getName() +
                        "', value '" + ObjectUtils.nullSafeToString(ex.getValue()) + "'."
        ),
                HttpStatus.BAD_REQUEST);
    }

    protected ResponseEntity<ResponseObject> handleRequestParamException(MissingServletRequestParameterException ex) {
        return new ResponseEntity<>(new ResponseObject(
                false,
                ex.getMessage()
        ),
                HttpStatus.BAD_REQUEST);
    }

    protected RestException getPermissionRestException() {
        return new RestException("You dont have permission to access this resource", HttpStatus.FORBIDDEN);
    }

}
