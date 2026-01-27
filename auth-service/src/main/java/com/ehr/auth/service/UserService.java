package com.ehr.auth.service;

import com.ehr.auth.exception.ResourceNotFoundException;
import com.ehr.auth.exception.SelfDeletionException;
import com.ehr.auth.model.User;
import com.ehr.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void deleteUser(UUID userId, UUID currentUserId) {
        if (userId.equals(currentUserId)) {
            throw new SelfDeletionException();
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
    }
}
