package com.example.securityjwtrestapistandard.controller;

import com.example.securityjwtrestapistandard.dto.LoginRequest;
import com.example.securityjwtrestapistandard.dto.RegisterRequest;
import com.example.securityjwtrestapistandard.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public Object login(@RequestBody @Validated LoginRequest request){
        return authService.attemptLogin(request.getEmail(), request.getPassword());
    }

    @PostMapping("/registerUser")
    public Object register(@RequestBody @Validated RegisterRequest request) {
        return authService.registerUser(request);
    }
}
