package ua.notion.infrastructure.persistence.contract;

import java.util.List;
import ua.notion.domain.entity.User;
import ua.notion.infrastructure.persistence.Repository;

public interface UserRepository extends Repository<User, Integer> {

  List<User> findByUsername(String username);

  List<User> findByEmail(String email);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);
}
