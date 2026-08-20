package com.motorcardgame.app.auth.infrastructure.persistence;

import com.motorcardgame.app.auth.domain.User;
import com.motorcardgame.app.auth.domain.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springDataRepository;

    UserRepositoryAdapter(SpringDataUserRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity saved = springDataRepository.save(toEntity(user));
        return toDomain(saved);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return springDataRepository.findById(id).map(UserRepositoryAdapter::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataRepository.findByEmail(email).map(UserRepositoryAdapter::toDomain);
    }

    @Override
    public Optional<User> findByGoogleSubject(String googleSubject) {
        return springDataRepository.findByGoogleSubject(googleSubject).map(UserRepositoryAdapter::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataRepository.existsByEmail(email);
    }

    private static UserJpaEntity toEntity(User domain) {
        return new UserJpaEntity(
                domain.id(),
                domain.email(),
                domain.passwordHash(),
                domain.googleSubject(),
                domain.displayName(),
                domain.createdAt(),
                domain.updatedAt());
    }

    private static User toDomain(UserJpaEntity entity) {
        return User.reconstitute(
                entity.getId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getGoogleSubject(),
                entity.getDisplayName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
