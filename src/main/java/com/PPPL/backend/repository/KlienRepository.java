package com.PPPL.backend.repository;

import com.PPPL.backend.entity.Klien;
import com.PPPL.backend.entity.StatusKlien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface KlienRepository extends JpaRepository<Klien, Integer> {
    
    Optional<Klien> findByEmailKlien(String emailKlien);
    
    List<Klien> findByStatus(StatusKlien status);
    
    boolean existsByEmailKlien(String emailKlien);
}
