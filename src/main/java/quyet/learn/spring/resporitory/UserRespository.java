package quyet.learn.spring.resporitory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quyet.learn.spring.entity.Users;

import java.util.Optional;

@Repository
public interface UserRespository extends JpaRepository<Users, String> {
    boolean existsByUsername(String username);
    Optional<Users> findByUsername(String username);
}
