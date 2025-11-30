package com.PPPL.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.PPPL.backend.model.RequestLayanan;
import com.PPPL.backend.model.StatusRequest;

import java.util.List;

@Repository
public interface RequestLayananRepository extends JpaRepository<RequestLayanan, Integer> {
    
    List<RequestLayanan> findByStatus(StatusRequest status);
    
    List<RequestLayanan> findByKlien_IdKlien(Integer idKlien);
    
    List<RequestLayanan> findByLayanan_IdLayanan(Integer idLayanan);
    
    // buat mendapatkan request yang sedang menunggu verifikasi
    List<RequestLayanan> findByStatusOrderByTglRequestAsc(StatusRequest status);
}
