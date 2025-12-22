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

    /**       
     * Helper method to get a pending request and validate its status.
    **/
    @Transactional
    public RequestLayanan create(Integer idKlien, Integer idLayanan) {
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
     * Helper method to get a pending request and validate its status.
    **/
    @Transactional
    public RequestLayanan approve(Integer idRequest) {
        RequestLayanan request = getPendingRequest(idRequest);

        request.setStatus(StatusRequest.VERIFIKASI);
        request.setTglVerifikasi(new Date());
        request.setKeteranganPenolakan(null);

        Klien klien = request.getKlien();
        if (klien.getStatus() == StatusKlien.BELUM) {
            klien.setStatus(StatusKlien.AKTIF);
            klienRepository.save(klien);
        }

        return requestLayananRepository.save(request);
    }

    /**       
     * Helper method to get a pending request and validate its status.
    **/
    @Transactional
    public RequestLayanan reject(Integer idRequest, String alasan) {
        if (alasan == null || alasan.trim().isEmpty()) {
            throw new RuntimeException("Alasan penolakan wajib diisi");
        }

        RequestLayanan request = getPendingRequest(idRequest);
        request.setStatus(StatusRequest.DITOLAK);
        request.setTglVerifikasi(new Date());
        request.setKeteranganPenolakan(alasan);

        return requestLayananRepository.save(request);
    }

    /**       
     * Helper method to get a pending request and validate its status.
    **/
    public List<RequestLayanan> findAll() {
        return requestLayananRepository.findAll();
    }

    public RequestLayanan findById(Integer id) {
        return requestLayananRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request tidak ditemukan"));
    }

    public List<RequestLayanan> findByStatus(StatusRequest status) {
        return requestLayananRepository.findByStatusOrderByTglRequestAsc(status);
    }

    /**       
     * Helper method to get a pending request and validate its status.
    **/
    private RequestLayanan getPendingRequest(Integer idRequest) {
        RequestLayanan request = findById(idRequest);

        if (request.getStatus() != StatusRequest.MENUNGGU_VERIFIKASI) {
            throw new RuntimeException("Request sudah diproses sebelumnya");
        }
        return request;
    }
}
