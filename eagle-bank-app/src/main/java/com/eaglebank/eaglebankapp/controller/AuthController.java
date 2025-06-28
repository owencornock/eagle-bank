package com.eaglebank.eaglebankapp.controller;

import com.eaglebank.eaglebankapp.security.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    public record LoginRequest(String email, String password) {}
    public record LoginResponse(String token) {}

    @Operation(summary = "Authenticate and get JWT")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        String token = auth.login(req.email(), req.password());
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
