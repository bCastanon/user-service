package com.brandon.user_service.service;

import com.brandon.user_service.dto.UserRequest;
import com.brandon.user_service.dto.UserResponse;
import com.brandon.user_service.exception.DuplicateEmailException;
import com.brandon.user_service.exception.UserNotFoundException;
import com.brandon.user_service.model.User;
import com.brandon.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserRequest request;
    private User user;

    @BeforeEach
    void setUp() {
        request = new UserRequest();
        request.setName("Ana");
        request.setEmail("ana@test.com");

        user = new User();
        user.setId(1L);
        user.setName("Ana");
        user.setEmail("ana@test.com");
    }

    @Test
    void createUser_shouldSaveAndReturnUser_whenEmailNotTaken() {
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.createUser(request);

        assertThat(response.getName()).isEqualTo("Ana");
        assertThat(response.getEmail()).isEqualTo("ana@test.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_shouldThrow_whenEmailAlreadyExists() {
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("ana@test.com");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_shouldReturnUser_whenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(1L);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void getUserById_shouldThrow_whenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void deleteUser_shouldThrow_whenNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).deleteById(any());
    }
}
