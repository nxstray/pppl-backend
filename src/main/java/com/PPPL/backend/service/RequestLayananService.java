package com.PPPL.backend.service;

import com.PPPL.backend.model.*;
import com.PPPL.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;

@Service
public class RequestLayananService {
    
    @Autowired
    private RequestLayananRepository requestLayananRepository;
    
    @Autowired
    private KlienRepository klienRepository;
    
    @Autowired
    private LayananRepository layananRepository;
    
    @Autowired
    private ManagerRepository managerRepository;
    
    @Autowired
    private RekapRepository rekapRepository;
    
    /**
     * Membuat request layanan baru
     */
    @Transactional
    public RequestLayanan createRequestLayanan(Integer idKlien, Integer idLayanan) {
        Klien klien = klienRepository.findById(idKlien)
            .orElseThrow(() -> new RuntimeException("Klien tidak ditemukan"));
        
        Layanan layanan = layananRepository.findById(idLayanan)
            .orElseThrow(() -> new RuntimeException("Layanan tidak ditemukan"));
        
        RequestLayanan request = new RequestLayanan();
        request.setKlien(klien);
        request.setLayanan(layanan);
        request.setTglRequest(new Date());
        request.setStatus(StatusRequest.MENUNGGU_VERIFIKASI);
        
        return requestLayananRepository.save(request);
    }
    
    /**
     * Verifikasi request layanan oleh Manager
     */
    @Transactional
    public void verifikasiRequestLayanan(Integer idRequest, Integer idManager, String hasilMeeting) {
        RequestLayanan request = requestLayananRepository.findById(idRequest)
            .orElseThrow(() -> new RuntimeException("Request tidak ditemukan"));
        
        if (request.getStatus() != StatusRequest.MENUNGGU_VERIFIKASI) {
            throw new RuntimeException("Request sudah diproses sebelumnya");
        }
        
        Manager manager = managerRepository.findById(idManager)
            .orElseThrow(() -> new RuntimeException("Manager tidak ditemukan"));
        
        // Update status request menjadi VERIFIKASI
        request.setStatus(StatusRequest.VERIFIKASI);
        request.setTglVerifikasi(new Date());
        requestLayananRepository.save(request);
        
        // Buat rekap meeting
        Rekap rekap = new Rekap();
        rekap.setKlien(request.getKlien());
        rekap.setManager(manager);
        rekap.setLayanan(request.getLayanan());
        rekap.setTglMeeting(new Date());
        rekap.setHasil(hasilMeeting);
        rekap.setStatus(StatusRekap.MASIH_JALAN);
        rekapRepository.save(rekap);
        
        // Relasi manager dengan klien (jika belum ada)
        if (!manager.getKlienSet().contains(request.getKlien())) {
            manager.getKlienSet().add(request.getKlien());
            managerRepository.save(manager);
        }
        
        // Update status klien menjadi AKTIF jika masih BELUM
        if (request.getKlien().getStatus() == StatusKlien.BELUM) {
            request.getKlien().setStatus(StatusKlien.AKTIF);
            klienRepository.save(request.getKlien());
        }
    }
    
    /**
     * Menolak request layanan
     */
    @Transactional
    public void tolakRequestLayanan(Integer idRequest, String keteranganPenolakan, boolean hapusKlien) {
        RequestLayanan request = requestLayananRepository.findById(idRequest)
            .orElseThrow(() -> new RuntimeException("Request tidak ditemukan"));
        
        if (request.getStatus() != StatusRequest.MENUNGGU_VERIFIKASI) {
            throw new RuntimeException("Request sudah diproses sebelumnya");
        }
        
        // Update status request menjadi ditolak
        request.setStatus(StatusRequest.DITOLAK);
        request.setTglVerifikasi(new Date());
        request.setKeteranganPenolakan(keteranganPenolakan);
        requestLayananRepository.save(request);
        
        // Hapus klien jika diminta
        if (hapusKlien) {
            Klien klien = request.getKlien();
            // Hapus semua request layanan terkait klien
            requestLayananRepository.deleteAll(klien.getRequestLayananSet());
            // Hapus klien
            klienRepository.delete(klien);
        }
    }
    
    /**
     * get semua request yang menunggu verifikasi
     */
    public List<RequestLayanan> getRequestMenungguVerifikasi() {
        return requestLayananRepository.findByStatusOrderByTglRequestAsc(StatusRequest.MENUNGGU_VERIFIKASI);
    }
    
    /**
     * get request berdasarkan klien
     */
    public List<RequestLayanan> getRequestByKlien(Integer idKlien) {
        return requestLayananRepository.findByKlien_IdKlien(idKlien);
    }
}