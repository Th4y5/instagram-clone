package br.edu.ifpb.instagram.repository;

import br.edu.ifpb.instagram.model.entity.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Deve salvar e recuperar um usuário")
    void testSaveAndFindById() {
        UserEntity user = new UserEntity();
        user.setFullName("Maria Silva");
        user.setUsername("mariasilva");
        user.setEmail("maria@email.com");
        user.setEncryptedPassword("senha123");

        UserEntity savedUser = userRepository.save(user);
        Optional<UserEntity> foundUser = userRepository.findById(savedUser.getId());

        assertTrue(foundUser.isPresent());
        assertEquals("mariasilva", foundUser.get().getUsername());
    }

    @Test
    @DisplayName("Deve retornar verdadeiro se o e-mail já existir")
    void testExistsByEmail() {
        UserEntity user = new UserEntity();
        user.setFullName("José Silva");
        user.setUsername("jose");
        user.setEmail("jose@email.com");
        user.setEncryptedPassword("senha123");

        userRepository.save(user);
        assertTrue(userRepository.existsByEmail("jose@email.com"));
    }

    @Test
    @DisplayName("Deve retornar verdadeiro se o username já existir")
    void testExistsByUsername() {
        UserEntity user = new UserEntity();
        user.setFullName("Ana Souza");
        user.setUsername("anasouza");
        user.setEmail("ana@email.com");
        user.setEncryptedPassword("senha123");

        userRepository.save(user);
        assertTrue(userRepository.existsByUsername("anasouza"));
    }

    @Test
    @DisplayName("Deve deletar o usuário com sucesso")
    void testDeleteById() {
        UserEntity user = new UserEntity();
        user.setFullName("Carlos Lima");
        user.setUsername("carlos");
        user.setEmail("carlos@email.com");
        user.setEncryptedPassword("senha123");

        UserEntity savedUser = userRepository.save(user);
        Long userId = savedUser.getId();

        assertTrue(userRepository.existsById(userId));

        userRepository.deleteById(userId);

        assertFalse(userRepository.existsById(userId));
    }
}
