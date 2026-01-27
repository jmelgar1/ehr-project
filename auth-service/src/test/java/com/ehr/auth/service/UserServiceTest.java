package com.ehr.auth.service;

import com.ehr.auth.exception.ResourceNotFoundException;
import com.ehr.auth.exception.SelfDeletionException;
import com.ehr.auth.model.enums.UserRole;
import com.ehr.auth.repository.UserRepository;

import java.util.UUID;

import static com.ehr.auth.utils.UserServiceTestUtils.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    void givenExistingUser_whenDeleteUser_thenUserIsDeleted() {
        var userId = UUID.randomUUID();
        var currentUserId = UUID.randomUUID();
        var testUser = user("testuser", UserRole.NURSE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        userService.deleteUser(userId, currentUserId);

        verify(userRepository).delete(testUser);
    }

    @Test
    void givenNonExistentUser_whenDeleteUser_thenThrowsResourceNotFoundException() {
        var userId = UUID.randomUUID();
        var currentUserId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(userId, currentUserId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void givenSameUserIdAsCurrentUser_whenDeleteUser_thenThrowsSelfDeletionException() {
        var userId = UUID.randomUUID();

        assertThatThrownBy(() -> userService.deleteUser(userId, userId))
                .isInstanceOf(SelfDeletionException.class)
                .hasMessage("Cannot delete your own account");
    }
}
