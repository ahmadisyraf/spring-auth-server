package com.isyraf.kuantanfunmap.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isyraf.kuantanfunmap.config.JwtService;
import com.isyraf.kuantanfunmap.exception.InvalidCredential;
import com.isyraf.kuantanfunmap.exception.UserAlreadyExist;
import com.isyraf.kuantanfunmap.exception.UserNotExist;
import com.isyraf.kuantanfunmap.token.Token;
import com.isyraf.kuantanfunmap.token.TokenRepository;
import com.isyraf.kuantanfunmap.token.TokenType;
import com.isyraf.kuantanfunmap.user.User;
import com.isyraf.kuantanfunmap.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {

        var isUserExist = userRepository.findByEmail(request.getEmail());

        if (isUserExist.isPresent()) {
            throw new UserAlreadyExist("USER WITH EMAIL " + request.getEmail() + " ALREADY EXIST");
        }

        User user = new User();
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        userRepository.save(user);

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        saveUserToken(user, jwtToken);

        AuthenticationResponse response = new AuthenticationResponse();
        response.setAccessToken(jwtToken);
        response.setRefreshToken(refreshToken);

        return response;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
            ));
        } catch (AuthenticationException ex) {
            throw new InvalidCredential("Incorrect password");
        }

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotExist("User with " + request.getEmail() + " not exist"));

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserToken(user);
        saveUserToken(user, jwtToken);

        AuthenticationResponse response = new AuthenticationResponse();

        response.setAccessToken(jwtToken);
        response.setRefreshToken(refreshToken);

        return response;
    }

    private void revokeAllUserToken(User user) {
        var validToken = tokenRepository.findAllValidTokenByUser(user.getId());

        if (validToken.isEmpty()) {
            return;
        }

        validToken.forEach(token -> {
            token.setRevoked(true);
            token.setExpired(true);
        });
    }

    private void saveUserToken(User user, String jwtToken) {
        Token token = new Token();
        token.setUser(user);
        token.setToken(jwtToken);
        token.setTokenType(TokenType.BEARER);
        token.setExpired(false);
        token.setRevoked(false);

        tokenRepository.save(token);
    }

    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        final String refreshToken;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        refreshToken = authHeader.substring(7);

        userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail != null) {
            var user = userRepository.findByEmail(userEmail)
                    .orElseThrow();

            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);

                revokeAllUserToken(user);
                saveUserToken(user, accessToken);

                AuthenticationResponse authenticationResponse = new AuthenticationResponse();

                authenticationResponse.setRefreshToken(refreshToken);
                authenticationResponse.setAccessToken(accessToken);

                new ObjectMapper().writeValue(response.getOutputStream(), authenticationResponse);
            }
        }

    }
}
