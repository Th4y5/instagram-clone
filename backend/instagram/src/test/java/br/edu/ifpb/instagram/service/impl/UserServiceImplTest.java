package br.edu.ifpb.instagram.service.impl;

import br.edu.ifpb.instagram.exception.FieldAlreadyExistsException;
import br.edu.ifpb.instagram.model.dto.UserDto;
import br.edu.ifpb.instagram.model.entity.UserEntity;
import br.edu.ifpb.instagram.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserServiceImplTest {

    @MockBean
    UserRepository userRepository;

    @MockBean
    PasswordEncoder passwordEncoder;

    @Autowired
    UserServiceImpl userService;

    private UserEntity userEntity;
    private UserDto userDto;

    @BeforeEach
    void setup() {
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setFullName("Paulo Pereira");
        userEntity.setUsername("paulo");
        userEntity.setEmail("paulo@ppereira.dev");
        userEntity.setEncryptedPassword("encoded-pass");

        userDto = new UserDto(
                1L,
                "Paulo Pereira",
                "paulo",
                "paulo@ppereira.dev",
                "123456",
                null
        );
    }

    // ------------------ CREATE USER ------------------

    @Test
    void testCreateUser_Success() {
        when(userRepository.existsByEmail(userDto.email())).thenReturn(false);
        when(userRepository.existsByUsername(userDto.username())).thenReturn(false);
        when(passwordEncoder.encode(userDto.password())).thenReturn("encoded-pass");
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        UserDto result = userService.createUser(userDto);

        assertNotNull(result);
        assertEquals(userEntity.getFullName(), result.fullName());
        assertEquals(userEntity.getEmail(), result.email());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testCreateUser_ThrowsException_WhenEmailExists() {
        when(userRepository.existsByEmail(userDto.email())).thenReturn(true);

        assertThrows(FieldAlreadyExistsException.class, () -> userService.createUser(userDto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testCreateUser_ThrowsException_WhenUsernameExists() {
        when(userRepository.existsByUsername(userDto.username())).thenReturn(true);

        assertThrows(FieldAlreadyExistsException.class, () -> userService.createUser(userDto));
        verify(userRepository, never()).save(any());
    }

    // ------------------ UPDATE USER ------------------

    @Test
    void testUpdateUser_Success() {
        when(userRepository.findById(userDto.id())).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        UserDto result = userService.updateUser(userDto);

        assertNotNull(result);
        assertEquals(userEntity.getFullName(), result.fullName());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testUpdateUser_UpdatesPassword_WhenProvided() {
        userDto = new UserDto(1L, "Novo Nome", "novoUser", "novo@email.com", "novaSenha", null);

        when(userRepository.findById(userDto.id())).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.encode("novaSenha")).thenReturn("encoded-new-pass");
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        userService.updateUser(userDto);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());

        assertEquals("encoded-new-pass", captor.getValue().getEncryptedPassword());
    }

    @Test
    void testUpdateUser_ThrowsException_WhenUserNotFound() {
        when(userRepository.findById(userDto.id())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.updateUser(userDto));
        assertTrue(ex.getMessage().contains("User not found with id:"));
    }

    @Test
    void testUpdateUser_ThrowsException_WhenDtoIsNull() {
        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(null));
    }

    // ------------------ DELETE USER ------------------

    @Test
    void testDeleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteUser_ThrowsException_WhenNotExists() {
        when(userRepository.existsById(1L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.deleteUser(1L));
        assertTrue(ex.getMessage().contains("User not found with id:"));
    }

    // ------------------ FIND BY ID ------------------

    @Test
    void testFindById_ReturnsUserDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));

        UserDto result = userService.findById(1L);

        assertNotNull(result);
        assertEquals(userEntity.getFullName(), result.fullName());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testFindById_ThrowsException_WhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.findById(99L));
        assertTrue(ex.getMessage().contains("User not found with id:"));
    }

    // ------------------ FIND ALL ------------------

    @Test
    void testFindAll_ReturnsList() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(userEntity));

        List<UserDto> result = userService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Paulo Pereira", result.get(0).fullName());
    }

    @Test
    void testFindAll_ReturnsEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserDto> result = userService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
