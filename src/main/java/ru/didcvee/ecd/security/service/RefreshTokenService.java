package ru.didcvee.ecd.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.didcvee.ecd.security.exception.TokenRefreshException;
import ru.didcvee.ecd.security.repository.JdbcRefreshTokenRepository;
import ru.didcvee.ecd.security.repository.JdbcUserRepository;
import ru.didcvee.ecd.security.repository.model.RefreshToken;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Value("${jwt.jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;

    @Autowired
    private JdbcRefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JdbcUserRepository userRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(Long userId) {
        Optional<RefreshToken> byUserId = refreshTokenRepository.findByUserId(userId);

        RefreshToken refreshToken = new RefreshToken();
        if (byUserId.isEmpty()) {
            refreshToken.setUserId(userRepository.findById(userId).orElseThrow().getId());
            refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
            refreshToken.setToken(UUID.randomUUID().toString());
        } else {
            refreshToken = byUserId.get();
            refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        }

        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.deleteByUserId(token.getUserId());
            throw new TokenRefreshException(
                    token.getToken(), "Refresh token was expired. Please make a new signin request");
        }

        return token;
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(
                userRepository.findById(userId).get().getId());
    }
}
