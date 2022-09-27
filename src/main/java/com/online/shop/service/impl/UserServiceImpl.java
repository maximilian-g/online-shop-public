package com.online.shop.service.impl;

import com.online.shop.config.PaginationConfig;
import com.online.shop.controller.forms.ChangePasswordForm;
import com.online.shop.controller.forms.RecoverPasswordForm;
import com.online.shop.controller.forms.RegistrationForm;
import com.online.shop.dto.AddressDto;
import com.online.shop.dto.EmailDto;
import com.online.shop.dto.LoginDto;
import com.online.shop.dto.UserDto;
import com.online.shop.dto.min.UserDtoMinimized;
import com.online.shop.entity.Address;
import com.online.shop.entity.Cart;
import com.online.shop.entity.EmailVerification;
import com.online.shop.entity.PasswordRecovery;
import com.online.shop.entity.Role;
import com.online.shop.entity.User;
import com.online.shop.repository.CartRepository;
import com.online.shop.repository.UserRepository;
import com.online.shop.repository.VerificationRepository;
import com.online.shop.security.config.SecurityConfig;
import com.online.shop.service.abstraction.BaseService;
import com.online.shop.service.abstraction.UserService;
import com.online.shop.service.exception.ChangePasswordException;
import com.online.shop.service.exception.EmailVerificationException;
import com.online.shop.service.exception.EntityUpdateException;
import com.online.shop.service.exception.NotFoundException;
import com.online.shop.service.exception.PasswordRecoveryException;
import com.online.shop.service.exception.UserRegisterException;
import com.online.shop.service.util.CustomPage;
import com.online.shop.service.util.Email;
import com.online.shop.service.util.Pagination;
import com.online.shop.service.util.PaginationInfo;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl extends BaseService implements UserService {

    public static final String USER_ATTRIBUTE_NAME = "users";
    public static final String SINGLE_USER_ATTRIBUTE_NAME = "user";
    public static final String ROLE_ATTRIBUTE_NAME = "roles";
    public static final String USER_PAGE_ATTRIBUTE_NAME = "userPage";
    public static final String EMAIL_ATTRIBUTE_NAME = "email";
    public static final String REGISTERED_AT_ATTRIBUTE_NAME = "registeredAt";
    public static final Long DEFAULT_USER_ID = -1L;
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final VerificationRepository verificationRepository;

    private final AsyncEmailSenderService asyncEmailSenderService;
    private final VerificationService verificationService;
    private final PasswordRecoveryService passwordRecoveryService;

    private final PaginationConfig paginationConfig;

    @Value("${app.url}")
    private String url;
    @Value("${app.access.update.interval.minutes}")
    private Long accessUpdateIntervalMinutes;
    @Value("${app.email.confirmation}")
    private Boolean verifyEmail;
    private final PasswordEncoder encoder;

    @Autowired
    protected UserServiceImpl(CartRepository cartRepository,
                              UserRepository userRepository,
                              Validator validator,
                              AsyncEmailSenderService asyncEmailSenderService,
                              VerificationService verificationService,
                              VerificationRepository verificationRepository,
                              PasswordRecoveryService passwordRecoveryService,
                              PaginationConfig paginationConfig,
                              PasswordEncoder encoder) {
        super(validator, LoggerFactory.getLogger(UserServiceImpl.class));
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.asyncEmailSenderService = asyncEmailSenderService;
        this.verificationService = verificationService;
        this.verificationRepository = verificationRepository;
        this.passwordRecoveryService = passwordRecoveryService;
        this.paginationConfig = paginationConfig;
        this.encoder = encoder;
    }

    @Override
    public UserDto getUserDtoByUsername(String username) {
        return UserDto.getUserDto(getUserByUsername(username));
    }

    @Override
    public UserDtoMinimized getUserDtoByUsernameMinimized(String username) {
        User userByUsername = getUserByUsername(username);
        return new UserDtoMinimized(userByUsername);
    }

    @Override
    public UserDto getUserDtoById(Long userId) {
        return UserDto.getUserDto(getUserById(userId));
    }

    @Override
    public boolean isAdmin(Long userId) {
        User user = getUserById(userId);
        return SecurityConfig.getAdminAuthority().getAuthority().equals(user.getRole());
    }

    @Override
    public UserDto registerUser(RegistrationForm userInfo, String ip) {
        User userToSave = userInfo.getRawUser();
        userToSave.setCreationDate(LocalDateTime.now());
        userToSave.setLastAccessDate(new Date());
        userToSave.setRegistrationIp(ip);
        userToSave.setLastIp(ip);
        Set<ConstraintViolation<User>> violations = getViolations(userToSave);
        if (violations.isEmpty()) {
            if (userRepository.findByUsername(userToSave.getUsername()).isEmpty()) {
                if (!userRepository.existsByEmail(userToSave.getEmail())) {
                    userToSave.setPassword(encoder.encode(userToSave.getPassword()));
                    Cart cart = new Cart();
                    cart = cartRepository.save(cart);
                    userToSave.setCart(cart);
                    cart.setUser(userToSave);
                    userToSave = userRepository.save(userToSave);
                    attachVerificationToUserAndSendEmail(userToSave);
                    return UserDto.getUserDto(userToSave);
                } else {
                    throw new UserRegisterException("User with email '" + userToSave.getEmail() + "' already exists.");
                }
            } else {
                throw new UserRegisterException("User with username '" + userToSave.getUsername() + "' already exists.");
            }
        } else {
            throw new UserRegisterException(getErrorMessagesTotal(violations));
        }
    }

    @Override
    public void updateAccessDateIfNeeded(Long userId, Instant accessDate) {
        User user = getUserById(userId);
        if(Math.abs(accessDate.toEpochMilli() - user.getLastAccessDate().getTime()) >
                TimeUnit.MINUTES.toMillis(accessUpdateIntervalMinutes)) {
            logger.info("Updating access date of " + user.getUsername());
            user.setLastAccessDate(new Date(accessDate.toEpochMilli()));
        }
    }

    @Override
    public UserDto verifyUserManually(Long userId) {
        return UserDto.getUserDto(verifyUserEntityManually(userId));
    }

    @Override
    public UserDto changeUserEmail(Long userId, EmailDto emailDto) {
        User user = getUserById(userId);
        user.setEmail(emailDto.getEmail());
        return UserDto.getUserDto(saveIfValidOrThrow(user, userRepository));
    }

    @Override
    public Long getUserIdByUsername(String username) {
        return userRepository.getIdByUsername(username).orElseThrow(() -> new NotFoundException("User does not exist"));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthoritiesByName(String username) {
        User userByUsername = getUserByUsername(username);
        return userByUsername.getAuthorities();
    }

    @Override
    public UserDto setEnabledToUser(Long userId, String currentUsername, boolean enabled) {
        return UserDto.getUserDto(setEnabledToUserEntity(userId, currentUsername, enabled));
    }

    @Override
    public UserDto updateUserRole(Long userId, String currentUsername, String role) {
        User user = getUserById(userId);
        if (user.getUsername().equals(currentUsername)) {
            throw new EntityUpdateException("Cannot change your role.");
        }
        if (Role.isValidRole(role)) {
            user.setRole(Role.getRoleObject(role));
            Set<ConstraintViolation<User>> violations = getViolations(user);
            if (violations.isEmpty()) {
                return UserDto.getUserDto(userRepository.save(user));
            } else {
                throw new EntityUpdateException(getErrorMessagesTotal(violations));
            }
        }
        throw new EntityUpdateException("Invalid role.");
    }

    @Override
    public boolean userHasOrderWithId(String username, Long id) {
        try {
            User user = userRepository.findByUsername(username).orElseThrow();
            return user.getOrders() != null && user.getOrders().stream().anyMatch(order -> order.getId().equals(id));
        } catch (NoSuchElementException ignored) {
            return false;
        }
    }

    @Override
    public boolean userHasAddressWithId(Long userId, Long addressId) {
        return getUserById(userId).getAddresses().stream().anyMatch(userAddress -> userAddress.getId().equals(addressId));
    }

    @Override
    public void resetLastIpIfNeeded(String username, String ip) {
        User user = getUserByUsername(username);
        if (!user.getLastIp().equals(ip)) {
            user.setLastIp(ip);
            userRepository.save(user);
        }
    }

    @Override
    public Collection<AddressDto> getAddressesOfUser(String username) {
        return AddressDto.getAddressDtoCollection(getAddressEntitiesOfUser(username));
    }

    @Override
    public Collection<UserDto> getAllUsers() {
        return getAllUserEntities().stream().map(UserDto::getUserDto).collect(Collectors.toList());
    }

    @Override
    public Pagination<UserDto> findPaginated(Integer pageSize, Integer currentPage) {
        PaginationInfo paginationInfo =
                getPaginationInfo(userRepository.count(), pageSize, currentPage, paginationConfig.getPageSizeProp());
        Page<User> userPage =
                userRepository.findAll(PageRequest.of(paginationInfo.currentPage - 1, paginationInfo.pageSize));
        return new Pagination<>(
                new CustomPage<>(
                        userPage.getContent().stream().map(UserDto::getUserDto).collect(Collectors.toList()),
                        userPage.getSize(),
                        userPage.getNumber(),
                        userPage.getTotalPages()
                ),
                getPageNumbers(userPage.getTotalPages(), paginationInfo.currentPage));
    }

    @Override
    public int getPageSizeProp() {
        return paginationConfig.getPageSizeProp();
    }

    @Override
    public void changePassword(ChangePasswordForm form, Long userId) {
        if (form.isValid()) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ChangePasswordException("Cannot change password."));
            String currentPassword = form.getCurrentPassword();
            String newPassword = form.getNewPassword();
            // if current password matches
            if (encoder.matches(currentPassword, user.getPassword())) {
                // and new password is not current password
                if (!encoder.matches(newPassword, user.getPassword())) {
                    user.setPassword(form.getNewPasswordRaw());
                    Set<ConstraintViolation<User>> violations = getViolations(user);
                    if (violations.isEmpty()) {
                        user.setPassword(encoder.encode(newPassword));
                        userRepository.save(user);
                    } else {
                        throw new ChangePasswordException(getErrorMessagesTotal(violations));
                    }
                } else {
                    throw new ChangePasswordException("New password must differ from current.");
                }
            } else {
                throw new ChangePasswordException("Invalid current password.");
            }
        } else {
            throw new ChangePasswordException(form.getValidationMessage());
        }
    }

    @Override
    public boolean verify(String verificationId) {
        EmailVerification emailVerification = getVerificationById(verificationId);
        // if email is not verified yet
        if (!emailVerification.getStatus()) {
            User user = emailVerification.getHolder();
            user.setIsEnabled(true);
            emailVerification.setStatus(true);
            emailVerification.setSent(true);
            emailVerification.setTriedToSend(true);
            verificationRepository.save(emailVerification);
            userRepository.save(user);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public boolean login(LoginDto loginDto) {
        logger.debug("User '" + loginDto.getUsername() +
                "' is trying to log in with password '" + loginDto.getPassword() + "'");
        User user = getUserByUsername(loginDto.getUsername());
        return encoder.matches(user.getPassword(), loginDto.getPassword());
    }

    @Override
    public void sendRecoveryEmail(String username, String ip) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        userOptional.ifPresent((user) -> {
            String message = "This is recovery email. Please proceed to link below and create new password. ";
            if (!user.getRegistrationIp().equals(ip)) {
                message += "\nNote, that ip address differs from registration ip address. " +
                        "Email recovery was requested from '" + ip + "'.";
            }
            user = generateAndSaveRecoveryUrlForUser(user);
            PasswordRecovery recovery = user.getPasswordRecovery();
            String fullUrl = url + "/recoverPassword/" + recovery.getId();
            Email emailToSend = getPasswordRecoveryEmail(user.getEmail(), "Password recovery email", message, fullUrl);
            asyncEmailSenderService.sendEmail(emailToSend);
        });
        // if user is not present, we do not need to let someone know that this user does not exist,
        // so there is no need to throw exception if user does not exist
    }

    @Override
    public void recoverPassword(RecoverPasswordForm form) {
        if (form.newPassword != null && form.newPassword.equals(form.confirm)) {
            PasswordRecovery passwordRecovery = passwordRecoveryService.findById(form.recoveryId);
            User user = passwordRecovery.getHolder();
            user.setPassword(form.newPassword);
            Set<ConstraintViolation<User>> violations = getViolations(user);
            if (violations.isEmpty()) {
                user.setPassword(encoder.encode(user.getPassword()));
                user.setPasswordRecovery(null);
                passwordRecovery.setHolder(null);
                passwordRecoveryService.delete(passwordRecovery);
                userRepository.save(user);
            } else {
                throw new PasswordRecoveryException(getErrorMessagesTotal(violations));
            }
        } else {
            throw new PasswordRecoveryException("Passwords are not identical.");
        }
    }

    @Override
    public Collection<UserDto> getActiveUsersOn(Date startDate, Date endDate) {
        return UserDto.getUserDto(userRepository.getUsersActiveBetween(startDate, endDate));
    }

    @Override
    public void addAuthAttribute(Model model, String username) {
        User user = getUserByUsername(username);
        model.addAttribute("auth", user.isEnabled());
        SimpleGrantedAuthority adminAuthority = SecurityConfig.getAdminAuthority();
        model.addAttribute("isAdmin",
                user.getAuthorities().stream().anyMatch(authority -> authority.equals(adminAuthority))
        );
    }

    public Collection<User> getAllUserEntities() {
        Iterable<User> users = userRepository.findAll();
        List<User> result = new LinkedList<>();
        users.forEach(result::add);
        return result;
    }

    public Collection<Address> getAddressEntitiesOfUser(String username) {
        User user = getUserByUsername(username);
        return new ArrayList<>(user.getAddresses());
    }

    public User setEnabledToUserEntity(Long userId, String currentUsername, boolean enabled) {
        User user = getUserById(userId);
        if (user.getUsername().equals(currentUsername)) {
            throw new EntityUpdateException("Cannot change 'enable' state of your account.");
        }
        return setEnabledToUserById(userId, enabled);
    }

    public User verifyUserEntityManually(Long userId) {
        User user = getUserById(userId);
        EmailVerification emailVerification = user.getEmailVerification();
        if (!user.isEnabled() && emailVerification != null && !emailVerification.getStatus()) {
            if (!verify(emailVerification.getId())) {
                throw new EntityUpdateException("Could not verify user '" + user.getUsername() + "', email '" + user.getEmail() + "'.");
            }
        }
        return getUserById(userId);
    }

    private EmailVerification getVerificationById(String verificationId) {
        return verificationRepository.findById(verificationId)
                .orElseThrow(() -> new EmailVerificationException("Verification with id '" + verificationId + "' not found"));
    }

    private void attachVerificationToUserAndSendEmail(User userToSave) {
        try {
            EmailVerification emailVerification = verificationService.createVerification(userToSave.getEmail());
            emailVerification.setHolder(userToSave);
            verificationRepository.saveAndFlush(emailVerification);
            userToSave.setEmailVerification(emailVerification);
            userRepository.save(userToSave);
            if(verifyEmail) {
                Email emailToSend = getVerificationEmail(userToSave.getEmail(), url, emailVerification.getId());
                CompletableFuture<Boolean> sendEmailResult =
                        asyncEmailSenderService.sendEmail(emailToSend);
                sendEmailResult.thenAccept(getSendEmailAction(emailToSend, emailVerification.getId()));
            }
            logger.info("User [" + userToSave.getUsername() + "] " +
                    "was successfully registered with verification [" + emailVerification.getId() + "]");
            if(!verifyEmail) {
                verifyUserEntityManually(userToSave.getId());
                logger.info("User [" + userToSave.getUsername() + "] " +
                        "was successfully verified, email verification is turned off");
            }
        } catch (NoSuchAlgorithmException ex) {
            logger.info("Deleting [" + userToSave.getUsername() + "]");
            userRepository.delete(userToSave);
            logger.error(ex.getMessage());
            throw new UserRegisterException("Could not create verification code.");
        }
    }

    public static Email getVerificationEmail(String to, String baseUrl, String hash) {
        return new Email(to,
                "Email confirmation",
                "To confirm email proceed to following link.\n" +
                        baseUrl + "/verify/" + hash);
    }

    private Consumer<? super Boolean> getSendEmailAction(Email email, String verificationId) {
        EmailVerification verification = getVerificationById(verificationId);
        return (success) -> {
            verification.setTriedToSend(true);
            if (success) {
                verification.setSent(true);
                logger.info("Successfully sent email with verification link to [" + email + "].");
            } else {
                logger.warn("Could not send email with verification link to [" + email + "].");
            }
            verificationRepository.save(verification);
        };
    }

    private User generateAndSaveRecoveryUrlForUser(User user) {
        if (user.getPasswordRecovery() != null && !user.getPasswordRecovery().getUsed()) {
            throw new PasswordRecoveryException("Email with recovery for this user was already sent.");
        } else if (user.getPasswordRecovery() != null) {
            PasswordRecovery passwordRecovery = user.getPasswordRecovery();
            user.setPasswordRecovery(null);
            passwordRecoveryService.delete(passwordRecovery);
            user = userRepository.save(user);
        }
        try {
            PasswordRecovery passwordRecovery = passwordRecoveryService.createPasswordRecovery(user);
            passwordRecovery.setHolder(user);
            passwordRecovery = passwordRecoveryService.save(passwordRecovery);
            user.setPasswordRecovery(passwordRecovery);
            return userRepository.save(user);
        } catch (NoSuchAlgorithmException ex) {
            throw new PasswordRecoveryException("Could not create recovery email for this user.");
        }
    }

    private Email getPasswordRecoveryEmail(String to, String subject, String message, String url) {
        return new Email(to, subject, message + "\n" + url);
    }

    public User getUserById(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Could not find user with id '" + userId + "'."));
    }

    protected User setEnabledToUserById(Long userId, boolean enabled) {
        User user = getUserById(userId);
        user.setIsEnabled(enabled);
        return userRepository.saveAndFlush(user);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User with username '" + username + "' does not exist."));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW,
                   isolation = Isolation.SERIALIZABLE)
    public void deleteUserById(Long id) {
        User user = getUserById(id);
        cartRepository.delete(user.getCart());
        userRepository.delete(user);
    }

    public List<UserDtoMinimized> getUserDtosMinimized() {
        return userRepository.findAll().stream().map(UserDtoMinimized::new).collect(Collectors.toList());
    }

}
