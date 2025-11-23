package com.PPPL.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.PPPL.backend.model.Karyawan;

import java.util.List;
import java.util.Optional;

@Repository
public interface KaryawanRepository extends JpaRepository<Karyawan, Integer> {
    
    Optional<Karyawan> findByEmailKaryawan(String emailKaryawan);
    
    List<Karyawan> findByManager_IdManager(Integer idManager);
    
    List<Karyawan> findByJabatanPosisi(String jabatanPosisi);
    
    boolean existsByEmailKaryawan(String emailKaryawan);
}
