package com.motorcardgame.app.auth.application;

import com.motorcardgame.app.auth.domain.RefreshToken;
import com.motorcardgame.app.auth.domain.RefreshTokenRepository;
import com.motorcardgame.app.auth.domain.User;
import com.motorcardgame.app.auth.domain.UserRepository;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleTokenVerifier googleTokenVerifier;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            GoogleTokenVerifier googleTokenVerifier) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.googleTokenVerifier = googleTokenVerifier;
    }

    @Transactional
    public AuthResult register(String email, String rawPassword, String displayName) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
        User user = User.registerWithPassword(email, passwordEncoder.encode(rawPassword), displayName);
        return issueTokens(userRepository.save(user));
    }

    @Transactional
    public AuthResult login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
        if (user.passwordHash() == null || !passwordEncoder.matches(rawPassword, user.passwordHash())) {
            throw new InvalidCredentialsException();
        }
        return issueTokens(user);
    }

    @Transactional
    public AuthResult loginWithGoogle(String idToken) {
        GoogleTokenVerifier.GoogleIdentity identity = googleTokenVerifier.verify(idToken);
        User user = userRepository.findByGoogleSubject(identity.subject())
                .orElseGet(() -> linkOrCreateGoogleUser(identity));
        return issueTokens(user);
    }

    @Transactional
    public AuthResult refresh(String rawRefreshToken) {
        RefreshToken stored = validRefreshTokenOrThrow(rawRefreshToken);
        stored.revoke();
        refreshTokenRepository.save(stored);
        User user = userRepository.findById(stored.userId()).orElseThrow(InvalidRefreshTokenException::new);
        return issueTokens(user);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        String hash = jwtService.hashRefreshToken(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
            token.revoke();
            refreshTokenRepository.save(token);
        });
    }

    private User linkOrCreateGoogleUser(GoogleTokenVerifier.GoogleIdentity identity) {
        return userRepository.findByEmail(identity.email())
                .map(existing -> {
                    existing.linkGoogleSubject(identity.subject());
                    return userRepository.save(existing);
                })
                .orElseGet(() -> userRepository.save(
                        User.registerWithGoogle(identity.email(), identity.subject(), identity.displayName())));
    }

    private RefreshToken validRefreshTokenOrThrow(String rawRefreshToken) {
        String hash = jwtService.hashRefreshToken(rawRefreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(InvalidRefreshTokenException::new);
        if (!stored.isValid()) {
            throw new InvalidRefreshTokenException();
        }
        return stored;
    }

    private AuthResult issueTokens(User user) {
        String accessToken = jwtService.issueAccessToken(user.id(), user.role());
        String rawRefreshToken = jwtService.generateRefreshTokenValue();
        String hash = jwtService.hashRefreshToken(rawRefreshToken);
        Instant expiresAt = Instant.now().plus(jwtService.refreshTokenTtl());
        refreshTokenRepository.save(RefreshToken.create(user.id(), hash, expiresAt));
        return new AuthResult(accessToken, rawRefreshToken, user);
    }
}
