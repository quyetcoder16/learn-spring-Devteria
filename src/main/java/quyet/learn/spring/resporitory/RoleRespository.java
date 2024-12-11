package quyet.learn.spring.resporitory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quyet.learn.spring.entity.Role;

@Repository
public interface RoleRespository extends JpaRepository<Role, String> {
}
