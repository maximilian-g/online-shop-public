package com.online.shop.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.online.shop.entity.Order;
import com.online.shop.entity.User;
import com.online.shop.service.impl.UserServiceImpl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class UserDto {

    @NotNull
    private Long id;
    @NotNull
    private String username;
    @NotNull
    private String email;
    @NotNull
    private String role;
    private String registeredAt;
    private String accessedAt;
    @JsonIgnore
    private String lastIp;
    @JsonIgnore
    private String registrationIp;
    @JsonIgnore
    private boolean enabled;
    private Collection<Long> orderIds;
    private Collection<AddressDto> addresses;
    private CartDto cart;

    public UserDto(Long id, String username, String email, String role, String registeredAt, String accessedAt, Collection<Long> orders, Collection<AddressDto> addresses, CartDto cart) {
        this(id, username, email, role, registeredAt, accessedAt, orders, addresses, cart, "", "", true);
    }

    public UserDto(Long id,
                   String username,
                   String email,
                   String role,
                   String registeredAt,
                   String accessedAt,
                   Collection<Long> orders,
                   Collection<AddressDto> addresses,
                   CartDto cart,
                   String lastIp,
                   String registrationIp,
                   boolean enabled) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.registeredAt = registeredAt;
        this.accessedAt = accessedAt;
        this.orderIds = orders;
        this.addresses = addresses;
        this.cart = cart;
        this.lastIp = lastIp;
        this.registrationIp = registrationIp;
        this.enabled = enabled;
    }

    public static UserDto getUserDto(User user) {
        Collection<Long> orderIds = user.getOrders().stream().map(Order::getId).collect(Collectors.toList());
        Collection<AddressDto> addresses = AddressDto.getAddressDtoCollection(user.getAddresses());
        return new UserDto(user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getFormattedCreationDate(UserServiceImpl.FORMATTER),
                user.getFormattedAccessDate(),
                orderIds,
                addresses,
                CartDto.getCartDto(user.getCart()),
                user.getLastIp(),
                user.getRegistrationIp(),
                user.isEnabled() && user.isAccountNonExpired() &&
                        user.isCredentialsNonExpired() && user.isAccountNonLocked());
    }

    public static Collection<UserDto> getUserDto(Collection<User> users) {
        Collection<UserDto> result = new ArrayList<>(users.size());
        for(User user : users) {
            result.add(getUserDto(user));
        }
        return result;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(String registeredAt) {
        this.registeredAt = registeredAt;
    }

    public String getLastIp() {
        return lastIp;
    }

    public void setLastIp(String lastIp) {
        this.lastIp = lastIp;
    }

    public String getRegistrationIp() {
        return registrationIp;
    }

    public void setRegistrationIp(String registrationIp) {
        this.registrationIp = registrationIp;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Collection<Long> getOrderIds() {
        return orderIds;
    }

    public void setOrderIds(Collection<Long> orderIds) {
        this.orderIds = orderIds;
    }

    public Collection<AddressDto> getAddresses() {
        return addresses;
    }

    public void setAddresses(Collection<AddressDto> addresses) {
        this.addresses = addresses;
    }

    public CartDto getCart() {
        return cart;
    }

    public void setCart(CartDto cart) {
        this.cart = cart;
    }

    public String getAccessedAt() {
        return accessedAt;
    }

    public UserDto setAccessedAt(String accessedAt) {
        this.accessedAt = accessedAt;
        return this;
    }
}
