package com.online.shop.entity;

import com.online.shop.util.DateUtil;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false, unique = true)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotNull(message = "Username {app.validation.notnull.msg}")
    @Size(min = 3, max = 45, message = "Username {app.validation.size.msg} but provided ${validatedValue}")
    @Pattern(regexp = "[a-zA-Z0-9]+", message = "Username must contain only latin symbols and numbers from 0 to 9")
    private String username;

    @Column(nullable = false, unique = true)
    @NotNull(message = "Email {app.validation.notnull.msg}")
    @Email
    private String email;

    @Column(nullable = false)
    @NotNull(message = "Password {app.validation.notnull.msg}")
    @Size(min = 2/*6*/, max = 255, message = "Password {app.validation.size.msg} but provided ${validatedValue}")
    // TODO add pattern
    private String password;

    @Column(name = "roles", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "User role {app.validation.notnull.msg}")
    private Role role;

    @Column(name = "create_date", nullable = false)
    @NotNull
    private LocalDateTime creationDate;

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @OneToOne(mappedBy = "holder", orphanRemoval = true)
    private EmailVerification emailVerification;

    @OneToOne(mappedBy = "holder", orphanRemoval = true)
    private PasswordRecovery passwordRecovery;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, orphanRemoval = true)
    private Collection<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, orphanRemoval = true)
    private Collection<Address> addresses = new ArrayList<>();

    @Column(name = "non_expired", nullable = false)
    private boolean isAccountNonExpired;

    @Column(name = "non_locked", nullable = false)
    private boolean isAccountNonLocked;

    @Column(name = "credentials_non_expired", nullable = false)
    private boolean isCredentialsNonExpired;

    @Column(name = "enabled", nullable = false)
    private boolean isEnabled;

    @Column(name = "registration_ip", nullable = false)
    @NotNull
    private String registrationIp;

    @Column(name = "last_ip", nullable = false)
    @NotNull
    private String lastIp;

    @Column(name = "last_access_date", nullable = false)
    @NotNull
    private Date lastAccessDate;

    public User() {

    }

    public User(String username,
                String email,
                String password,
                Role role,
                boolean isAccountNonExpired,
                boolean isAccountNonLocked,
                boolean isCredentialsNonExpired,
                boolean isEnabled) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.isAccountNonExpired = isAccountNonExpired;
        this.isAccountNonLocked = isAccountNonLocked;
        this.isCredentialsNonExpired = isCredentialsNonExpired;
        this.isEnabled = isEnabled;
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

    @Override
    public boolean isAccountNonExpired() {
        return isAccountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isAccountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isCredentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role == null ? Collections.emptyList() : List.of(new SimpleGrantedAuthority(role.getRole()));
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Collection<Order> getOrders() {
        return orders;
    }

    public void setOrders(Collection<Order> orders) {
        this.orders = orders;
    }

    public Collection<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(Collection<Address> addresses) {
        this.addresses = addresses;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setIsAccountNonExpired(boolean value) {
        isAccountNonExpired = value;
    }

    public void setIsAccountNonLocked(boolean value) {
        isAccountNonLocked = value;
    }

    public void setIsCredentialsNonExpired(boolean value) {
        isCredentialsNonExpired = value;
    }

    public void setIsEnabled(boolean value) {
        isEnabled = value;
    }

    public EmailVerification getEmailVerification() {
        return emailVerification;
    }

    public void setEmailVerification(EmailVerification emailVerification) {
        this.emailVerification = emailVerification;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public String getFormattedCreationDate(DateTimeFormatter formatter) {
        return creationDate.format(formatter);
    }

    public PasswordRecovery getPasswordRecovery() {
        return passwordRecovery;
    }

    public void setPasswordRecovery(PasswordRecovery passwordRecovery) {
        this.passwordRecovery = passwordRecovery;
    }

    public String getRegistrationIp() {
        return registrationIp;
    }

    public void setRegistrationIp(String registrationIp) {
        this.registrationIp = registrationIp;
    }

    public String getLastIp() {
        return lastIp;
    }

    public void setLastIp(String lastIp) {
        this.lastIp = lastIp;
    }

    public String getRole() { return role.getRole(); }

    public void setRole(Role role) {
        this.role = role;
    }

    public Date getLastAccessDate() {
        return lastAccessDate;
    }

    public void setLastAccessDate(Date lastAccessDate) {
        this.lastAccessDate = lastAccessDate;
    }

    public String getFormattedAccessDate() {
        return DateUtil.formatDateTime(lastAccessDate);
    }
}
