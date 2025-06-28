package com.eaglebank.eaglebankapp.security;

import com.eaglebank.eaglebankdomain.user.EmailAddress;
import com.eaglebank.eaglebanklogic.user.UserService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserService     userService;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwt;

    public AuthService(UserService userService,
                       PasswordEncoder encoder,
                       JwtTokenProvider jwt) {
        this.userService = userService;
        this.encoder     = encoder;
        this.jwt         = jwt;
    }

    public String login(String emailRaw, String rawPassword) {
        var email = new EmailAddress(emailRaw);
        var user  = userService.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid login"));
        if (!encoder.matches(rawPassword, user.getPasswordHash().value())) {
            throw new BadCredentialsException("Invalid login");
        }
        return jwt.createToken(user.getId().value().toString());
    }
}
