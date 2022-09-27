package com.online.shop.security.config;

import com.online.shop.entity.Role;
import com.online.shop.security.filter.CustomAuthFilter;
import com.online.shop.security.filter.JwtTokenVerifier;
import com.online.shop.security.handler.CustomAccessDeniedHandler;
import com.online.shop.security.handler.ForbiddenEntryPoint;
import com.online.shop.service.impl.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;
    private final JwtConfig config;
    private final TokenService tokenService;
    private final PasswordEncoder encoder;

    @Autowired
    public SecurityConfig(@Qualifier("userRepositoryUserDetailsService") UserDetailsService userDetailsService,
                          JwtConfig config,
                          TokenService tokenService,
                          PasswordEncoder encoder) {
        this.userDetailsService = userDetailsService;
        this.config = config;
        this.tokenService = tokenService;
        this.encoder = encoder;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(encoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilter(new CustomAuthFilter(authenticationManager(), tokenService))
                .addFilterBefore(new JwtTokenVerifier(config, tokenService), CustomAuthFilter.class)
                .authorizeRequests()
                .antMatchers("/", "/accessDenied",
                        "/register", "/verify/**", "/images/**",
                        "/style/**", "/js/pages/**", "/js/lib/**", "/js/clients/**", "/items",
                        "/item", "/items/**", "/forgetPassword", "/recoverPassword/**", "/api/v1/auth").permitAll()
                .antMatchers("/account", "/orders", "/cart").authenticated()
                .antMatchers(HttpMethod.GET, "/api/v1/images/**")
                .permitAll()
                .antMatchers(HttpMethod.POST, "/api/v1/images/**")
                .hasAnyAuthority(Role.ADMIN.getRole())
                .antMatchers(HttpMethod.PUT, "/api/v1/images/**")
                .hasAnyAuthority(Role.ADMIN.getRole())
                .antMatchers(HttpMethod.DELETE, "/api/v1/images/**")
                .hasAnyAuthority(Role.ADMIN.getRole())
                .antMatchers(HttpMethod.GET, "/api/v1/items/{id}/quantity")
                .hasAnyAuthority(Role.ADMIN.getRole())
                .antMatchers(HttpMethod.GET,
                        "/api/v1/categories/**",
                        "/api/v1/items/**",
                        "/api/v1/item_types/**",
                        "/api/v1/properties/**",
                        "/api/v1/property_values/**")
                .permitAll()
                .antMatchers(HttpMethod.POST,
                        "/api/v1/users/{id}/email")
                .authenticated()
                .antMatchers(HttpMethod.GET,
                        "/api/v1/addresses/**",
                        "/api/v1/carts/**",
                        "/api/v1/orders/**",
                        "/api/v1/prices/**",
                        "/api/v1/users",
                        "/api/v1/users/authorities").authenticated()
                .antMatchers(HttpMethod.POST,
                        "/api/v1/addresses/**",
                        "/api/v1/carts/**",
                        "/api/v1/orders/**").authenticated()
                .antMatchers(HttpMethod.PUT,
                        "/api/v1/addresses/**").authenticated()
                .antMatchers(HttpMethod.DELETE,
                        "/api/v1/addresses/**",
                        "/api/v1/carts/**").authenticated()
                .antMatchers(HttpMethod.GET, "/api/v1/users/*", "/js/admin/**", "/reports/**", "/api/v1/reports/**", "/swagger-ui/**")
                .hasAnyAuthority(Role.ADMIN.getRole())
                .antMatchers(HttpMethod.GET, "/js/operator/**")
                .hasAnyAuthority(Role.ADMIN.getRole(), Role.OPERATOR.getRole())
                .antMatchers(HttpMethod.PUT, "/api/v1/orders/{id}/status")
                .hasAnyAuthority(Role.OPERATOR.getRole(), Role.ADMIN.getRole())
                .antMatchers(HttpMethod.GET, "/api/v1/orders/statuses", "/operator/**")
                .hasAnyAuthority(Role.OPERATOR.getRole(), Role.ADMIN.getRole())
                .antMatchers(HttpMethod.POST, "/api/v1/categories/**", "/api/v1/items/**", "/api/v1/prices/**", "/api/v1/users/**", "/api/v1/item_types/**", "/api/v1/properties/**", "/api/v1/property_values/**")
                .hasAnyAuthority(Role.ADMIN.getRole())
                .antMatchers(HttpMethod.PUT, "/api/v1/categories/**", "/api/v1/items/**", "/api/v1/orders/**", "/api/v1/prices/**", "/api/v1/users/**", "/api/v1/item_types/**", "/api/v1/properties/**", "/api/v1/property_values/**")
                .hasAnyAuthority(Role.ADMIN.getRole())
                .antMatchers(HttpMethod.DELETE, "/api/v1/categories/**", "/api/v1/items/**", "/api/v1/orders/**", "/api/v1/prices/**", "/api/v1/users/**", "/api/v1/item_types/**", "/api/v1/properties/**", "/api/v1/property_values/**")
                .hasAnyAuthority(Role.ADMIN.getRole())
                .antMatchers(HttpMethod.PATCH, "/api/v1/users/**")
                .hasAnyAuthority(Role.ADMIN.getRole())
                .antMatchers("/admin", "/admin/**").hasAnyAuthority(Role.ADMIN.getRole())
                .anyRequest().authenticated()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(new ForbiddenEntryPoint())
                .accessDeniedHandler(new CustomAccessDeniedHandler())
                .and()
                .formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/account", true)
                .permitAll()
                .and()
                .logout().permitAll().logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .deleteCookies("JSESSIONID", config.getAccessTokenKeyName())
                .invalidateHttpSession(true)
                .permitAll();
    }

    public static SimpleGrantedAuthority getAdminAuthority() {
        return new SimpleGrantedAuthority(Role.ADMIN.getRole());
    }

}
