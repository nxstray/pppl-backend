package com.PPPL.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.PPPL.backend.model.Klien;
import com.PPPL.backend.model.StatusKlien;

import java.util.List;
import java.util.Optional;

@Repository
public interface KlienRepository extends JpaRepository<Klien, Integer> {
    
    Optional<Klien> findByEmailKlien(String emailKlien);
    
    List<Klien> findByStatus(StatusKlien status);
    
    boolean existsByEmailKlien(String emailKlien);
}
