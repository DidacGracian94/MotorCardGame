package com.motorcardgame.app.auth.application;

import com.motorcardgame.app.auth.domain.Role;
import com.motorcardgame.app.auth.domain.User;
import com.motorcardgame.app.auth.domain.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAdminService {

    private final UserRepository userRepository;

    public UserAdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User changeRole(UUID actingAdminId, UUID targetUserId, Role newRole) {
        if (actingAdminId.equals(targetUserId)) {
            throw new CannotChangeOwnRoleException();
        }
        User target = userRepository.findById(targetUserId).orElseThrow(() -> new UserNotFoundException(targetUserId));
        target.changeRole(newRole);
        return userRepository.save(target);
    }
}
