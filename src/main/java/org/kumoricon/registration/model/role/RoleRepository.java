package org.kumoricon.registration.model.role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RoleRepository extends JpaRepository<Role, Integer> {
    List<Role> findByNameStartsWithIgnoreCase(String lastName);
    Role findByNameIgnoreCase(String roleName);
}