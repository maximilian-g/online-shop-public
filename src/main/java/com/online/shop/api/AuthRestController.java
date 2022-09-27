package com.online.shop.api;

import com.online.shop.api.util.ResponseObject;
import com.online.shop.dto.LoginDto;
import com.online.shop.service.abstraction.UserService;
import com.online.shop.service.impl.TokenService;
import com.online.shop.service.util.TokenValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/v1/auth")
public class AuthRestController extends BaseRestController {

    public static final String INVALID_CREDENTIALS = "Invalid credentials were present in request.";

    private final TokenService tokenService;
    private final UserService userService;

    @Autowired
    public AuthRestController(TokenService tokenService, UserService userService) {
        this.tokenService = tokenService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> getAccessToken(@RequestBody @Valid LoginDto loginDto,
                                            HttpServletRequest request) {
        if(userService.login(loginDto)) {
            Map<String, TokenValue> tokens = tokenService.createTokens(loginDto.getUsername(),
                    request.getRequestURL().toString(),
                    request.getRemoteAddr());
            return ResponseEntity.ok(tokens);
        }
        return new ResponseEntity<>(new ResponseObject(false, INVALID_CREDENTIALS), HttpStatus.BAD_REQUEST);

    }

}
