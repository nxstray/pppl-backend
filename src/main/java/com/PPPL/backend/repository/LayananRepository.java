package com.PPPL.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.PPPL.backend.model.KategoriLayanan;
import com.PPPL.backend.model.Layanan;

import java.util.List;

@Repository
public interface LayananRepository extends JpaRepository<Layanan, Integer> {
    
    List<Layanan> findByKategori(KategoriLayanan kategori);
    
    List<Layanan> findByNamaLayananContainingIgnoreCase(String namaLayanan);
}
