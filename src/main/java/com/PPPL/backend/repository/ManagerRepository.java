package com.PPPL.backend.repository;

import com.PPPL.backend.model.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, Integer> {
    
    Optional<Manager> findByEmailManager(String emailManager);
    
    List<Manager> findByDivisi(String divisi);
    
    boolean existsByEmailManager(String emailManager);
}
