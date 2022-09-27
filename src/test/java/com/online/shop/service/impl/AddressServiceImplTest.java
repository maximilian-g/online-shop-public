package com.online.shop.service.impl;

import com.online.shop.api.exception.RestException;
import com.online.shop.config.PaginationConfig;
import com.online.shop.dto.AddressDto;
import com.online.shop.entity.Address;
import com.online.shop.entity.Cart;
import com.online.shop.entity.Order;
import com.online.shop.entity.Role;
import com.online.shop.entity.User;
import com.online.shop.repository.AddressRepository;
import com.online.shop.repository.CartRepository;
import com.online.shop.repository.PasswordRecoveryRepository;
import com.online.shop.repository.UserRepository;
import com.online.shop.repository.VerificationRepository;
import com.online.shop.security.config.SimplePasswordEncoder;
import com.online.shop.service.exception.AddressCreationException;
import com.online.shop.service.exception.EntityUpdateException;
import com.online.shop.service.exception.NotFoundException;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

// is needed to properly operate with mocks
@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;
    @Mock
    private VerificationRepository verificationRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordRecoveryRepository passwordRecoveryRepository;

    private AddressServiceImpl addressService;

    @BeforeEach
    void setUp() throws IllegalAccessException {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        PaginationConfig paginationConfig = new PaginationConfig();
        FieldUtils.writeField(paginationConfig, "pageSizeProp", 8, true);

        PasswordRecoveryService passwordRecoveryService = new PasswordRecoveryService(validator, passwordRecoveryRepository);
        VerificationService verificationService = new VerificationService(validator, verificationRepository);
        AsyncEmailSenderService asyncEmailSenderService = new AsyncEmailSenderService(validator, null);
        addressService = new AddressServiceImpl(addressRepository, validator, paginationConfig,
                new UserServiceImpl(cartRepository,
                        userRepository,
                        validator,
                        asyncEmailSenderService,
                        verificationService,
                        verificationRepository,
                        passwordRecoveryService,
                        paginationConfig,
                        new SimplePasswordEncoder()));
    }

    private User getEnabledUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("Bob");
        user.setIsEnabled(true);
        user.setIsCredentialsNonExpired(true);
        user.setIsAccountNonExpired(true);
        user.setIsAccountNonLocked(true);
        user.setRole(Role.USER);
        user.setCreationDate(LocalDateTime.now());
        user.setLastAccessDate(new Date());
        user.setPassword("$WES%YHWS$E5hgasgn3");

        Address address = new Address();
        address.setId(1L);
        address.setAddress("Rynok");
        address.setUser(user);
        address.setOrders(Collections.emptyList());
        user.setAddresses(List.of(address));

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        cart.setCartItems(Collections.emptyList());
        user.setCart(cart);

        user.setOrders(Collections.emptyList());
        user.setLastIp("127.0.0.1");
        user.setRegistrationIp(user.getLastIp());
        user.setEmail("test@gmail.com");

        return user;
    }

    @Test
    void createAddress() {
        when(addressRepository.save(any(Address.class))).then(AdditionalAnswers.returnsFirstArg());
        Assertions.assertDoesNotThrow(() -> addressService.createAddress("Rynok"));
        Assertions.assertThrows(AddressCreationException.class, () -> addressService.createAddress(null));

    }

    @Test
    void createAddressAssignedToUser() {
        User user = getEnabledUser();
        User anotherUser = getEnabledUser();
        anotherUser.setId(2L);
        BDDMockito.given(userRepository.findById(1L)).willReturn(Optional.of(user));
        BDDMockito.given(userRepository.findById(2L)).willReturn(Optional.of(anotherUser));
        BDDMockito.given(userRepository.findById(43L)).willReturn(Optional.empty());
        when(addressRepository.save(any(Address.class))).then(AdditionalAnswers.returnsFirstArg());

        Assertions.assertDoesNotThrow(() -> addressService.createAddressAssignedToUser("Rynok", 1L));
        Assertions.assertThrows(AddressCreationException.class, () -> addressService.createAddressAssignedToUser(null, 1L));
        Assertions.assertThrows(NotFoundException.class, () -> addressService.createAddressAssignedToUser("Rynok", 43L));

        Address address = new Address();
        address.setAddress("New address");
        address.setUser(user);
        AddressDto addressDto = AddressDto.getAddressDto(address);
        Assertions.assertDoesNotThrow(() -> addressService.createAddress(addressDto, 1L));
        Assertions.assertThrows(NotFoundException.class, () -> addressService.createAddress(addressDto, 43L));

        AddressDto addressThrows = new AddressDto(0L, 2L, "New address");
        Assertions.assertThrows(RestException.class, () -> addressService.createAddress(addressThrows, 1L));

        AddressDto anotherAddressThrows = new AddressDto(0L, null, "New address");
        Assertions.assertThrows(AddressCreationException.class, () -> addressService.createAddress(anotherAddressThrows, 1L));

    }

    @Test
    void updateAddress() {
        User user = getEnabledUser();
        User anotherUser = getEnabledUser();
        anotherUser.setId(2L);
        Address address = user.getAddresses().iterator().next();
        BDDMockito.given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        BDDMockito.given(userRepository.findById(anotherUser.getId())).willReturn(Optional.of(anotherUser));
        BDDMockito.given(addressRepository.findById(address.getId())).willReturn(Optional.of(address));
        when(addressRepository.save(any(Address.class))).then(AdditionalAnswers.returnsFirstArg());

        Assertions.assertDoesNotThrow(() -> addressService.updateAddress(1L, "New address"));
        Assertions.assertEquals("New address", address.getAddress());
        Assertions.assertThrows(EntityUpdateException.class, () -> addressService.updateAddress(1L, null));
        address.setAddress("New address");

        AddressDto addressDto = AddressDto.getAddressDto(address);
        Assertions.assertDoesNotThrow(() -> addressService.updateAddress(addressDto, 1L));
        Assertions.assertThrows(RestException.class, () -> addressService.updateAddress(addressDto, 2L));

        AddressDto addressThrows = new AddressDto(1L, null, "New address");
        Assertions.assertThrows(EntityUpdateException.class, () -> addressService.updateAddress(addressThrows, 2L));

        user.setRole(Role.ADMIN);
        AddressDto addressThrows1 = new AddressDto(1L, 2L, "New address");
        Assertions.assertDoesNotThrow(() -> addressService.updateAddress(addressThrows1, user.getId()));

        AddressDto addressThrows2 = new AddressDto(1L, null, "New address");
        Assertions.assertDoesNotThrow(() -> addressService.updateAddress(addressThrows2, user.getId()));

    }

    @Test
    void deleteById() {

        Address address = new Address();
        address.setId(1L);
        address.setAddress("Some address");
        address.setOrders(Collections.emptyList());
        BDDMockito.given(addressRepository.findById(address.getId())).willReturn(Optional.of(address));
        doNothing().when(addressRepository).deleteById(any(Long.class));

        Assertions.assertDoesNotThrow(() -> addressService.deleteById(address.getId()));

        Order order = new Order();
        address.setOrders(List.of(order));
        Assertions.assertThrows(EntityUpdateException.class, () -> addressService.deleteById(address.getId()));

    }
}