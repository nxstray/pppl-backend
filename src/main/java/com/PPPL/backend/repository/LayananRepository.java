package com.PPPL.backend.repository;

import com.PPPL.backend.entity.KategoriLayanan;
import com.PPPL.backend.entity.Layanan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LayananRepository extends JpaRepository<Layanan, Integer> {
    
    List<Layanan> findByKategori(KategoriLayanan kategori);
    
    List<Layanan> findByNamaLayananContainingIgnoreCase(String namaLayanan);
}
