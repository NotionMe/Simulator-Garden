package ua.notion.domain.service.auth;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import ua.notion.infrastructure.security.PasswordHasher;

class PasswordHasherTest {

  @Test
  void testHashPassword() {
    String password = "mySecurePassword123";
    String hash = PasswordHasher.hashPassword(password);

    assertNotNull(hash);
    assertNotEquals(password, hash);
    assertTrue(hash.contains(":"));
  }

  @Test
  void testVerifyPasswordCorrect() {
    String password = "mySecurePassword123";
    String hash = PasswordHasher.hashPassword(password);

    boolean verified = PasswordHasher.verifyPassword(password, hash);

    assertTrue(verified);
  }

  @Test
  void testVerifyPasswordIncorrect() {
    String password = "mySecurePassword123";
    String hash = PasswordHasher.hashPassword(password);

    boolean verified = PasswordHasher.verifyPassword("wrongPassword", hash);

    assertFalse(verified);
  }

  @Test
  void testHashingProducesDifferentHashes() {
    String password = "mySecurePassword123";
    String hash1 = PasswordHasher.hashPassword(password);
    String hash2 = PasswordHasher.hashPassword(password);

    assertNotEquals(hash1, hash2);
  }

  @Test
  void testVerifyPasswordWithInvalidFormat() {
    boolean verified = PasswordHasher.verifyPassword("password", "invalid-hash-format");

    assertFalse(verified);
  }

  @Test
  void testVerifyPasswordWithEmptyHash() {
    boolean verified = PasswordHasher.verifyPassword("password", "");

    assertFalse(verified);
  }

  @Test
  void testHashPasswordWithSpecialCharacters() {
    String password = "p@ssw0rd!#$%^&*()";
    String hash = PasswordHasher.hashPassword(password);

    assertTrue(PasswordHasher.verifyPassword(password, hash));
  }

  @Test
  void testHashPasswordWithUnicode() {
    String password = "пароль123";
    String hash = PasswordHasher.hashPassword(password);

    assertTrue(PasswordHasher.verifyPassword(password, hash));
  }
}
