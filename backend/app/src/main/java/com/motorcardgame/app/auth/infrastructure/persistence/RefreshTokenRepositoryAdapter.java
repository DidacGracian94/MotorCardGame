package com.motorcardgame.app.auth.infrastructure.persistence;

import com.motorcardgame.app.auth.domain.RefreshToken;
import com.motorcardgame.app.auth.domain.RefreshTokenRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final SpringDataRefreshTokenRepository springDataRepository;

    RefreshTokenRepositoryAdapter(SpringDataRefreshTokenRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenJpaEntity saved = springDataRepository.save(toEntity(refreshToken));
        return toDomain(saved);
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return springDataRepository.findByTokenHash(tokenHash).map(RefreshTokenRepositoryAdapter::toDomain);
    }

    private static RefreshTokenJpaEntity toEntity(RefreshToken domain) {
        return new RefreshTokenJpaEntity(
                domain.id(),
                domain.userId(),
                domain.tokenHash(),
                domain.expiresAt(),
                domain.revokedAt(),
                domain.createdAt());
    }

    private static RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        return RefreshToken.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.getRevokedAt(),
                entity.getCreatedAt());
    }
}
