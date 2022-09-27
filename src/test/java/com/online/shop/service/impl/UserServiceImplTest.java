package com.online.shop.service.impl;

import com.online.shop.config.PaginationConfig;
import com.online.shop.controller.forms.ChangePasswordForm;
import com.online.shop.controller.forms.RecoverPasswordForm;
import com.online.shop.controller.forms.RegistrationForm;
import com.online.shop.dto.AddressDto;
import com.online.shop.dto.UserDto;
import com.online.shop.entity.Address;
import com.online.shop.entity.Cart;
import com.online.shop.entity.EmailVerification;
import com.online.shop.entity.Order;
import com.online.shop.entity.PasswordRecovery;
import com.online.shop.entity.Role;
import com.online.shop.entity.User;
import com.online.shop.repository.CartRepository;
import com.online.shop.repository.PasswordRecoveryRepository;
import com.online.shop.repository.UserRepository;
import com.online.shop.repository.VerificationRepository;
import com.online.shop.service.exception.ChangePasswordException;
import com.online.shop.service.exception.EntityUpdateException;
import com.online.shop.service.exception.PasswordRecoveryException;
import com.online.shop.service.exception.UserRegisterException;
import com.online.shop.service.util.Email;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

// is needed to properly operate with mocks
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private VerificationRepository verificationRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordRecoveryRepository passwordRecoveryRepository;

    private VerificationService verificationService;
    @Mock
    private AsyncEmailSenderService asyncEmailSenderService;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() throws IllegalAccessException {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        PaginationConfig paginationConfig = new PaginationConfig();
        FieldUtils.writeField(paginationConfig, "pageSizeProp", 8, true);

        verificationService = new VerificationService(validator, verificationRepository);
        FieldUtils.writeField(verificationService, "expirationFromNowMS", 10000L, true);
        PasswordRecoveryService passwordRecoveryService = new PasswordRecoveryService(validator, passwordRecoveryRepository);
        userService = new UserServiceImpl(cartRepository,
                userRepository,
                validator,
                asyncEmailSenderService,
                verificationService,
                verificationRepository,
                passwordRecoveryService,
                paginationConfig,
                new PasswordEncoder() {
                    @Override
                    public String encode(CharSequence charSequence) {
                        return charSequence.toString();
                    }

                    @Override
                    public boolean matches(CharSequence charSequence, String s) {
                        return charSequence.toString().equals(s);
                    }
                });
        FieldUtils.writeField(userService, "url", "http://localhost:8080", true);
        FieldUtils.writeField(userService, "verifyEmail", false, true);
        FieldUtils.writeField(userService, "accessUpdateIntervalMinutes", 30L, true);
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

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        cart.setCartItems(Collections.emptyList());
        user.setCart(cart);

        user.setOrders(Collections.emptyList());
        user.setAddresses(Collections.emptyList());
        user.setLastIp("127.0.0.1");
        user.setRegistrationIp(user.getLastIp());
        user.setEmail("test@gmail.com");

        return user;
    }

    private User getDisabledUser() {
        User user = getEnabledUser();
        user.setId(2L);
        user.setUsername("Rick");
        user.setIsEnabled(false);
        return user;
    }

    private User getAdminUser() {
        User user = getEnabledUser();
        user.setRole(Role.ADMIN);
        user.setUsername("Admin");
        user.setId(3L);
        return user;
    }

    private void addOrderToUser(User user, long orderId) {
        Order order = new Order();
        order.setId(orderId);
        order.setUser(user);
        user.setOrders(List.of(order));
    }

    private void addAddressToUser(User user, long addressId) {
        Address address = new Address();
        address.setId(addressId);
        address.setUser(user);
        address.setAddress("Rotterdam");
        user.setAddresses(List.of(address));
    }

    @Test
    void getUserDtoByUsername() {
        User userToReturn = getEnabledUser();
        BDDMockito.given(userRepository.findByUsername(userToReturn.getUsername())).willReturn(Optional.of(userToReturn));
        Assertions.assertDoesNotThrow(() -> userService.getUserDtoByUsername(userToReturn.getUsername()));
        UserDto userDto = userService.getUserDtoByUsername(userToReturn.getUsername());
        Assertions.assertNotNull(userDto.getId());
        Assertions.assertNotNull(userDto.getEmail());
        Assertions.assertNotNull(userDto.getUsername());
        Assertions.assertNotNull(userDto.getCart());
        Assertions.assertNotNull(userDto.getCart().getId());
        Assertions.assertNotNull(userDto.getCart().getOwnerId());
        Assertions.assertNotNull(userDto.getCart().getOwnerUsername());
        Assertions.assertNotNull(userDto.getCart().getCartItems());
        Assertions.assertNotNull(userDto.getOrderIds());
        Assertions.assertNotNull(userDto.getAddresses());
        Assertions.assertNotNull(userDto.getAccessedAt());
        Assertions.assertNotNull(userDto.getRegisteredAt());
        Assertions.assertNotNull(userDto.getRole());
        Assertions.assertNotNull(userDto.getLastIp());
        Assertions.assertNotNull(userDto.getRegistrationIp());
    }

    @Test
    void getUserDtoById() {
        User userToReturn = getEnabledUser();
        BDDMockito.given(userRepository.findById(userToReturn.getId())).willReturn(Optional.of(userToReturn));
        Assertions.assertDoesNotThrow(() -> userService.getUserDtoById(userToReturn.getId()));
        UserDto userDto = userService.getUserDtoById(userToReturn.getId());
        Assertions.assertNotNull(userDto.getId());
        Assertions.assertNotNull(userDto.getEmail());
        Assertions.assertNotNull(userDto.getUsername());
        Assertions.assertNotNull(userDto.getCart());
        Assertions.assertNotNull(userDto.getCart().getId());
        Assertions.assertNotNull(userDto.getCart().getOwnerId());
        Assertions.assertNotNull(userDto.getCart().getOwnerUsername());
        Assertions.assertNotNull(userDto.getCart().getCartItems());
        Assertions.assertNotNull(userDto.getOrderIds());
        Assertions.assertNotNull(userDto.getAddresses());
        Assertions.assertNotNull(userDto.getAccessedAt());
        Assertions.assertNotNull(userDto.getRegisteredAt());
        Assertions.assertNotNull(userDto.getRole());
        Assertions.assertNotNull(userDto.getLastIp());
        Assertions.assertNotNull(userDto.getRegistrationIp());
    }

    @Test
    void isAdmin() {
        User userToReturn = getEnabledUser();
        BDDMockito.given(userRepository.findById(userToReturn.getId())).willReturn(Optional.of(userToReturn));
        Assertions.assertFalse(userService.isAdmin(userToReturn.getId()));

        User adminUser = getAdminUser();
        BDDMockito.given(userRepository.findById(adminUser.getId())).willReturn(Optional.of(adminUser));
        Assertions.assertTrue(userService.isAdmin(adminUser.getId()));
    }

    @Test
    void registerUser() {
        RegistrationForm form = new RegistrationForm("Vasiliy", "test@gmail.com", "%E^JHERth3Q$G", "%E^JHERth3Q$G");
        User rawUser = form.getRawUser();
        BDDMockito.given(userRepository.findByUsername(rawUser.getUsername())).willReturn(Optional.empty());
        BDDMockito.given(userRepository.existsByEmail(rawUser.getEmail())).willReturn(false);
        BDDMockito.given(userRepository.findById(1L)).willReturn(Optional.of(rawUser));

        when(userRepository.save(any(User.class))).then((Answer<User>) invocationOnMock -> {
            User user = invocationOnMock.getArgument(0);
            user.setId(1L);
            return user;
        });

        when(cartRepository.save(any(Cart.class))).then(AdditionalAnswers.returnsFirstArg());
        when(verificationRepository.saveAndFlush(any(EmailVerification.class))).then(AdditionalAnswers.returnsFirstArg());
//        when(verificationRepository.save(any(EmailVerification.class))).then(AdditionalAnswers.returnsFirstArg());
//        BDDMockito.given(verificationRepository.getOne(anyString())).willReturn(new EmailVerification());

//        BDDMockito.given(asyncEmailSenderService.sendEmail(any(Email.class))).willReturn(CompletableFuture.completedFuture(true));

        RegistrationForm finalForm = form;
        Assertions.assertDoesNotThrow(() -> userService.registerUser(finalForm, "127.0.0.1"));

        form = new RegistrationForm("Vasiliy", "test@gmail.com", "%E^JHERth3Q$G", "%E^JHERth3Q$G");
        rawUser = form.getRawUser();
        BDDMockito.given(userRepository.findByUsername(rawUser.getUsername())).willReturn(Optional.of(rawUser));

        RegistrationForm finalForm1 = form;
        Assertions.assertThrows(UserRegisterException.class, () -> userService.registerUser(finalForm1, "127.0.0.1"));

        form = new RegistrationForm("Vasiliy", "test@gmail.com", "%E^JHERth3Q$G", "%E^JHERth3Q$G");
        rawUser = form.getRawUser();
        BDDMockito.given(userRepository.findByUsername(rawUser.getUsername())).willReturn(Optional.empty());
        BDDMockito.given(userRepository.existsByEmail(rawUser.getEmail())).willReturn(true);

        RegistrationForm finalForm2 = form;
        Assertions.assertThrows(UserRegisterException.class, () -> userService.registerUser(finalForm2, "127.0.0.1"));

        form = new RegistrationForm("Vasiliy", "5e08h6ne093458ng9458", "%E^JHERth3Q$G", "%E^JHERth3Q$G");
        RegistrationForm finalForm3 = form;
        Assertions.assertThrows(UserRegisterException.class, () -> userService.registerUser(finalForm3, "127.0.0.1"));
    }

    @Test
    void verifyUserManually() throws NoSuchAlgorithmException {
        User user = getDisabledUser();
        EmailVerification verification = verificationService.createVerification(user.getEmail());
        user.setEmailVerification(verification);
        verification.setHolder(user);
        BDDMockito.given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        BDDMockito.given(verificationRepository.findById(verification.getId())).willReturn(Optional.of(verification));
        // after verifying another call to verifyUserManually should not throw any exception
        Assertions.assertDoesNotThrow(() -> userService.verifyUserManually(user.getId()));
        Assertions.assertDoesNotThrow(() -> userService.verifyUserManually(user.getId()));
    }

    @Test
    void setEnabledToUser() {
        User user = getDisabledUser();
        BDDMockito.given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        BDDMockito.given(userRepository.saveAndFlush(user)).willReturn(user);
        userService.setEnabledToUser(user.getId(), "Admin", true);
        Assertions.assertTrue(user.isEnabled());
        userService.setEnabledToUser(user.getId(), "Admin", false);
        Assertions.assertFalse(user.isEnabled());
        Assertions.assertThrows(EntityUpdateException.class, () -> userService.setEnabledToUser(user.getId(), user.getUsername(), false));
    }

    @Test
    void updateUserRole() {
        User user = getEnabledUser();
        BDDMockito.given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        BDDMockito.given(userRepository.save(user)).willReturn(user);
        userService.updateUserRole(user.getId(), "Admin", Role.ADMIN.getRole());
        Assertions.assertEquals(user.getRole(), Role.ADMIN.getRole());
        userService.updateUserRole(user.getId(), "Admin", Role.USER.getRole());
        Assertions.assertEquals(user.getRole(), Role.USER.getRole());

        Assertions.assertThrows(EntityUpdateException.class, () -> userService.updateUserRole(user.getId(), user.getUsername(), Role.ADMIN.getRole()));
        Assertions.assertThrows(EntityUpdateException.class, () -> userService.updateUserRole(user.getId(), user.getUsername(), Role.USER.getRole()));
        Assertions.assertThrows(EntityUpdateException.class, () -> userService.updateUserRole(user.getId(), "Admin", "sorgn9oe85ng90oe84n"));

    }

    @Test
    void userHasOrderWithId() {
        User user = getEnabledUser();
        BDDMockito.given(userRepository.findByUsername(user.getUsername())).willReturn(Optional.of(user));
        Assertions.assertFalse(userService.userHasOrderWithId(user.getUsername(), 1L));
        addOrderToUser(user, 1L);
        Assertions.assertTrue(userService.userHasOrderWithId(user.getUsername(), 1L));
        Assertions.assertFalse(userService.userHasOrderWithId(user.getUsername(), 2L));
    }

    @Test
    void userHasAddressWithId() {
        User user = getEnabledUser();
        BDDMockito.given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        Assertions.assertFalse(userService.userHasAddressWithId(user.getId(), 1L));
        addAddressToUser(user, 1L);
        Assertions.assertTrue(userService.userHasAddressWithId(user.getId(), 1L));
        Assertions.assertFalse(userService.userHasAddressWithId(user.getId(), 2L));
    }

    @Test
    void resetLastIpIfNeeded() {
        User user = getEnabledUser();
        BDDMockito.given(userRepository.findByUsername(user.getUsername())).willReturn(Optional.of(user));
        BDDMockito.given(userRepository.save(user)).willReturn(user);
        Assertions.assertDoesNotThrow(() -> userService.resetLastIpIfNeeded(user.getUsername(), "154.77.34.85"));
        Assertions.assertEquals("154.77.34.85", user.getLastIp());
    }

    @Test
    void getAddressesOfUser() {
        User user = getEnabledUser();
        BDDMockito.given(userRepository.findByUsername(user.getUsername())).willReturn(Optional.of(user));

        Collection<AddressDto> addresses = userService.getAddressesOfUser(user.getUsername());
        Assertions.assertEquals(0, addresses.size());
        addAddressToUser(user, 1L);

        addresses = userService.getAddressesOfUser(user.getUsername());
        Assertions.assertEquals(1, addresses.size());
        for(AddressDto address : addresses) {
            Assertions.assertNotNull(address.getId());
            Assertions.assertNotNull(address.getOwnerId());
            Assertions.assertNotNull(address.getAddress());
            Assertions.assertNotNull(address.getOwnerUsername());
        }
    }

    @Test
    void getAllUsers() {

        BDDMockito.given(userRepository.findAll()).willReturn(List.of(getAdminUser(), getDisabledUser(), getEnabledUser()));
        Collection<UserDto> users = userService.getAllUsers();
        Assertions.assertEquals(3, users.size());
        for(UserDto user : users) {
            Assertions.assertNotNull(user.getId());
            Assertions.assertNotNull(user.getRegistrationIp());
            Assertions.assertNotNull(user.getLastIp());
            Assertions.assertNotNull(user.getRole());
            Assertions.assertNotNull(user.getRegisteredAt());
            Assertions.assertNotNull(user.getAddresses());
            Assertions.assertNotNull(user.getCart());
            Assertions.assertNotNull(user.getUsername());
            Assertions.assertNotNull(user.getEmail());
            Assertions.assertNotNull(user.getOrderIds());
        }
    }

    @Test
    void changePassword() {
        User user = getEnabledUser();
        BDDMockito.given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        BDDMockito.given(userRepository.save(user)).willReturn(user);
        ChangePasswordForm form = new ChangePasswordForm(user.getPassword(), "123456TEq#", "123456TEq#");
        ChangePasswordForm finalForm = form;
        Assertions.assertDoesNotThrow(() -> userService.changePassword(finalForm, user.getId()));
        Assertions.assertEquals("123456TEq#", user.getPassword());

        // case with non equal new password and its confirmation
        form = new ChangePasswordForm(user.getPassword(), "123456TEq#", "66666#");
        ChangePasswordForm finalForm1 = form;
        Assertions.assertThrows(ChangePasswordException.class, () -> userService.changePassword(finalForm1, user.getId()));
        Assertions.assertEquals("123456TEq#", user.getPassword());

        // case with validation violation on entity level
        form = new ChangePasswordForm(user.getPassword(), "1", "1");
        ChangePasswordForm finalForm2 = form;
        Assertions.assertThrows(ChangePasswordException.class, () -> userService.changePassword(finalForm2, user.getId()));

        user.setPassword("123456TEq#");
        // user's new password must differ from current password
        form = new ChangePasswordForm(user.getPassword(), "123456TEq#", "123456TEq#");
        ChangePasswordForm finalForm3 = form;
        Assertions.assertThrows(ChangePasswordException.class, () -> userService.changePassword(finalForm3, user.getId()));
        Assertions.assertEquals("123456TEq#", user.getPassword());

    }

    @Test
    void sendRecoveryEmail() {
        User user = getEnabledUser();
        BDDMockito.given(userRepository.findByUsername(user.getUsername())).willReturn(Optional.of(user));
        BDDMockito.given(asyncEmailSenderService.sendEmail(any(Email.class))).willReturn(CompletableFuture.completedFuture(true));
        when(userRepository.save(any(User.class))).then(AdditionalAnswers.returnsFirstArg());
        when(passwordRecoveryRepository.save(any(PasswordRecovery.class))).then(AdditionalAnswers.returnsFirstArg());
        doNothing().when(passwordRecoveryRepository).delete(any(PasswordRecovery.class));

        Assertions.assertDoesNotThrow(() -> userService.sendRecoveryEmail(user.getUsername(), "127.0.0.1"));
        Assertions.assertNotNull(user.getPasswordRecovery());
        user.getPasswordRecovery().setUsed(true);

        // second invocation must delete created earlier recovery
        Assertions.assertDoesNotThrow(() -> userService.sendRecoveryEmail(user.getUsername(), "127.0.0.1"));
        Assertions.assertNotNull(user.getPasswordRecovery());

        // this invocation must throw an exception, because user already has not used password recovery
        Assertions.assertThrows(PasswordRecoveryException.class, () -> userService.sendRecoveryEmail(user.getUsername(), "127.0.0.1"));

    }

    @Test
    void recoverPassword() {
        User user = getEnabledUser();
        RecoverPasswordForm form = new RecoverPasswordForm("W$JH$5g345df@#", "W$JH$5g345df@#", "5555555");
        PasswordRecovery recovery = new PasswordRecovery();
        recovery.setId("5555555");
        recovery.setUsed(false);
        recovery.setHolder(user);
        BDDMockito.given(passwordRecoveryRepository.findById("5555555")).willReturn(Optional.of(recovery));
        RecoverPasswordForm finalForm = form;
        Assertions.assertDoesNotThrow(() -> userService.recoverPassword(finalForm));
        Assertions.assertEquals("W$JH$5g345df@#", user.getPassword());

        recovery.setId("5555555");
        recovery.setUsed(false);
        recovery.setHolder(user);
        user.setPasswordRecovery(recovery);

        form = new RecoverPasswordForm("W$JH$5g345dfq@#", "W$JH$5g345df@#", "5555555");
        RecoverPasswordForm finalForm1 = form;
        Assertions.assertThrows(PasswordRecoveryException.class, () -> userService.recoverPassword(finalForm1));
        Assertions.assertEquals("W$JH$5g345df@#", user.getPassword());

        recovery.setId("5555555");
        recovery.setUsed(false);
        recovery.setHolder(user);
        user.setPasswordRecovery(recovery);

        form = new RecoverPasswordForm("1", "1", "5555555");
        RecoverPasswordForm finalForm2 = form;
        Assertions.assertThrows(PasswordRecoveryException.class, () -> userService.recoverPassword(finalForm2));

    }

}