package com.online.shop.service.abstraction;

import com.online.shop.controller.forms.ChangePasswordForm;
import com.online.shop.controller.forms.RecoverPasswordForm;
import com.online.shop.controller.forms.RegistrationForm;
import com.online.shop.dto.AddressDto;
import com.online.shop.dto.EmailDto;
import com.online.shop.dto.LoginDto;
import com.online.shop.dto.UserDto;
import com.online.shop.dto.min.UserDtoMinimized;
import com.online.shop.service.util.Pagination;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.ui.Model;

import java.time.Instant;
import java.util.Collection;
import java.util.Date;

public interface UserService {

    UserDto getUserDtoByUsername(String username);

    UserDtoMinimized getUserDtoByUsernameMinimized(String username);

    UserDto getUserDtoById(Long userId);

    UserDto setEnabledToUser(Long userId, String currentUsername, boolean enabled);

    UserDto updateUserRole(Long userId, String currentUsername, String role);

    UserDto verifyUserManually(Long userId);

    UserDto changeUserEmail(Long userId, EmailDto emailDto);

    Long getUserIdByUsername(String username);

    Collection<? extends GrantedAuthority> getAuthoritiesByName(String username);

    Collection<AddressDto> getAddressesOfUser(String username);

    Collection<UserDto> getAllUsers();

    Pagination<UserDto> findPaginated(Integer pageSize, Integer currentPage);

    int getPageSizeProp();

    boolean isAdmin(Long userId);

    boolean userHasAddressWithId(Long userId, Long addressId);

    boolean userHasOrderWithId(String username, Long id);

    boolean verify(String verificationId);

    boolean login(LoginDto loginDto);

    void resetLastIpIfNeeded(String username, String ip);

    UserDto registerUser(RegistrationForm userInfo, String ip);

    void updateAccessDateIfNeeded(Long userId, Instant accessDate);

    void addAuthAttribute(Model model, String username);

    void changePassword(ChangePasswordForm form, Long userId);

    void sendRecoveryEmail(String username, String ip);

    void recoverPassword(RecoverPasswordForm form);

    Collection<UserDto> getActiveUsersOn(Date startDate, Date endDate);

}
