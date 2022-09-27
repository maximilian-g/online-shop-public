package com.online.shop.api;

import com.online.shop.api.exception.RestException;
import com.online.shop.dto.EmailDto;
import com.online.shop.dto.UserDto;
import com.online.shop.dto.min.UserDtoMinimized;
import com.online.shop.entity.Role;
import com.online.shop.service.impl.UserServiceImpl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/users")
public class UserRestController extends BaseRestController {

    private final UserServiceImpl userService;

    public UserRestController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserDto> getCurrentUser() {
        return ResponseEntity.ok(userService.getUserDtoByUsername(getCurrentUserUsername()));
    }

    @GetMapping("/authorities")
    public ResponseEntity<Collection<? extends GrantedAuthority>> getCurrentUserAuthorities() {
        return ResponseEntity.ok(userService.getAuthoritiesByName(getCurrentUserUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserDtoById(id));
    }

    @GetMapping("/min")
    public ResponseEntity<List<UserDtoMinimized>> getUsersMin() {
        return ResponseEntity.ok(userService.getUserDtosMinimized());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUserRole(@PathVariable(name = "id") Long id,
                                                  @RequestParam(name = "newRole", defaultValue = "NONE") String newRole) {
        if (!"NONE".equals(newRole)) {
            UserDto user = userService.updateUserRole(id, getCurrentUserUsername(), newRole);
            return ResponseEntity.ok(user);
        }
        throw new RestException("Invalid role.");
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDto> disableOrEnableUser(@PathVariable Long id,
                                                       @RequestParam(name = "enabled", defaultValue = "true") boolean enabled) {
        UserDto user = userService.setEnabledToUser(id, getCurrentUserUsername(), enabled);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{id}")
    public ResponseEntity<UserDto> verifyManually(@PathVariable Long id) {
        UserDto user = userService.verifyUserManually(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping(path = "/{id}/email",
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> changeEMail(@PathVariable Long id,
                                               @RequestBody @Valid EmailDto emailDto) {
        UserDto user = userService.getUserDtoById(id);
        UserDto currentUser = userService.getUserDtoByUsername(getCurrentUserUsername());
        if (user.getUsername().equals(currentUser.getUsername()) ||
                Role.ADMIN.getRole().equals(currentUser.getRole())) {
            return ResponseEntity.ok(userService.changeUserEmail(id, emailDto));
        }
        throw new AccessDeniedException("Access denied");
    }

}
