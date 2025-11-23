package com.PPPL.backend.service;

import com.PPPL.backend.model.*;
import com.PPPL.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RekapService {
    
    @Autowired
    private RekapRepository rekapRepository;
    
    /**
     * Update status rekap meeting
     */
    @Transactional
    public Rekap updateStatusRekap(Integer idMeeting, StatusRekap status, String catatanTambahan) {
        Rekap rekap = rekapRepository.findById(idMeeting)
            .orElseThrow(() -> new RuntimeException("Rekap meeting tidak ditemukan"));
        
        rekap.setStatus(status);
        
        if (catatanTambahan != null && !catatanTambahan.isEmpty()) {
            String catatanLama = rekap.getCatatan() != null ? rekap.getCatatan() : "";
            rekap.setCatatan(catatanLama + "\n" + catatanTambahan);
        }
        
        return rekapRepository.save(rekap);
    }
    
    /**
     * Menambahkan catatan baru ke rekap
     */
    @Transactional
    public Rekap tambahCatatan(Integer idMeeting, String catatan) {
        Rekap rekap = rekapRepository.findById(idMeeting)
            .orElseThrow(() -> new RuntimeException("Rekap meeting tidak ditemukan"));
        
        String catatanLama = rekap.getCatatan() != null ? rekap.getCatatan() : "";
        rekap.setCatatan(catatanLama + "\n[" + new java.util.Date() + "] " + catatan);
        
        return rekapRepository.save(rekap);
    }
}