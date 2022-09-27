package com.online.shop.service.impl;

import com.online.shop.entity.User;
import com.online.shop.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

// is needed to properly operate with mocks
@ExtendWith(MockitoExtension.class)
class UserRepositoryUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserRepositoryUserDetailsService userRepositoryUserDetailsService;

    @BeforeEach
    void setUp() {
        userRepositoryUserDetailsService = new UserRepositoryUserDetailsService(userRepository);
    }

    @Test
    void loadUserByUsername() {
        User user = new User();
        user.setUsername("Admin");
        BDDMockito.given(userRepository.findByUsername("Admin")).willReturn(Optional.of(user));
        BDDMockito.given(userRepository.findByUsername("NonExistent")).willReturn(Optional.empty());
        Assertions.assertDoesNotThrow(() -> userRepositoryUserDetailsService.loadUserByUsername(user.getUsername()));
        Assertions.assertThrows(UsernameNotFoundException.class, () -> userRepositoryUserDetailsService.loadUserByUsername("NonExistent"));
    }
}