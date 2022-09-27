package com.online.shop.dto.min;

import com.online.shop.entity.User;

import java.util.List;

public class UserDtoMinimized {

    private Long id;
    private String username;
    private List<String> roles;
    private boolean isEnabled;

    public UserDtoMinimized() {
    }

    public UserDtoMinimized(User user) {
        this.setId(user.getId())
                .setUsername(user.getUsername())
                .setRoles(List.of(user.getRole()))
                .setEnabled(user.isEnabled() && user.isAccountNonExpired() &&
                        user.isCredentialsNonExpired() && user.isAccountNonLocked());
    }

    public Long getId() {
        return id;
    }

    public UserDtoMinimized setId(Long id) {
        this.id = id;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public UserDtoMinimized setUsername(String username) {
        this.username = username;
        return this;
    }

    public List<String> getRoles() {
        return roles;
    }

    public UserDtoMinimized setRoles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public UserDtoMinimized setEnabled(boolean enabled) {
        isEnabled = enabled;
        return this;
    }
}
