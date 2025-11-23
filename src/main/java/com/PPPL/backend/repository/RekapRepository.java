package com.PPPL.backend.repository;

import com.PPPL.backend.entity.Rekap;
import com.PPPL.backend.entity.StatusRekap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RekapRepository extends JpaRepository<Rekap, Integer> {
    
    List<Rekap> findByKlien_IdKlien(Integer idKlien);
    
    List<Rekap> findByManager_IdManager(Integer idManager);
    
    List<Rekap> findByLayanan_IdLayanan(Integer idLayanan);
    
    List<Rekap> findByStatus(StatusRekap status);
    
    List<Rekap> findByKlien_IdKlienAndStatus(Integer idKlien, StatusRekap status);
}
