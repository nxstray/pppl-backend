package com.PPPL.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.PPPL.backend.model.Klien;

import java.util.List;
import java.util.Optional;

@Repository
public interface KlienRepository extends JpaRepository<Klien, Integer> {
    
    Optional<Klien> findByEmailKlien(String emailKlien);
    
    @Query("""
    SELECT DISTINCT k FROM Klien k
    JOIN k.requestLayananSet r
    WHERE r.status = 'VERIFIKASI'
    """)
    List<Klien> findKlienYangTerverifikasi();
    
    boolean existsByEmailKlien(String emailKlien);
}
