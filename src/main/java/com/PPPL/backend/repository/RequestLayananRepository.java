package com.PPPL.backend.repository;

import com.PPPL.backend.entity.RequestLayanan;
import com.PPPL.backend.entity.StatusRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RequestLayananRepository extends JpaRepository<RequestLayanan, Integer> {
    
    List<RequestLayanan> findByStatus(StatusRequest status);
    
    List<RequestLayanan> findByKlien_IdKlien(Integer idKlien);
    
    List<RequestLayanan> findByLayanan_IdLayanan(Integer idLayanan);
    
    // Untuk mendapatkan request yang sedang menunggu verifikasi
    List<RequestLayanan> findByStatusOrderByTglRequestAsc(StatusRequest status);
}
