package quyet.learn.spring.resporitory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import quyet.learn.spring.entity.Permission;

@Repository
public interface PermissionRespository extends JpaRepository<Permission, String> {}
