package com.PPPL.backend.service;

import com.PPPL.backend.data.RequestLayananStatisticsDTO;
import com.PPPL.backend.event.NotificationEventPublisher;
import com.PPPL.backend.model.RequestLayanan;
import com.PPPL.backend.model.StatusRequest;
import com.PPPL.backend.repository.RequestLayananRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class RequestLayananService {

    private final RequestLayananRepository requestLayananRepository;
    private final NotificationEventPublisher notificationPublisher;

    public RequestLayananService(
            RequestLayananRepository requestLayananRepository,
            NotificationEventPublisher notificationPublisher
    ) {
        this.requestLayananRepository = requestLayananRepository;
        this.notificationPublisher = notificationPublisher;
    }

    public List<RequestLayanan> findAll() {
        return requestLayananRepository.findAll();
    }

    public RequestLayanan findById(Integer id) {
        return requestLayananRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request layanan tidak ditemukan"));
    }

    public List<RequestLayanan> findByStatus(StatusRequest status) {
        return requestLayananRepository.findByStatus(status);
    }

    public RequestLayananStatisticsDTO getStatistics() {
        long total = requestLayananRepository.count();
        long menungguVerifikasi = requestLayananRepository.countByStatus(StatusRequest.MENUNGGU_VERIFIKASI);
        long diverifikasi = requestLayananRepository.countByStatus(StatusRequest.VERIFIKASI);
        long ditolak = requestLayananRepository.countByStatus(StatusRequest.DITOLAK);

        return new RequestLayananStatisticsDTO(total, menungguVerifikasi, diverifikasi, ditolak);
    }

    /**
     * APPROVE REQUEST - dengan notifikasi realtime
     */
    @Transactional
    public RequestLayanan approve(Integer id) {
        RequestLayanan request = findById(id);
        
        if (request.getStatus() == StatusRequest.VERIFIKASI) {
            throw new RuntimeException("Request sudah diverifikasi sebelumnya");
        }

        request.setStatus(StatusRequest.VERIFIKASI);
        request.setTglVerifikasi(new Date());
        request.setKeteranganPenolakan(null);

        RequestLayanan saved = requestLayananRepository.save(request);

        // PUBLISH NOTIFICATION TO RABBITMQ (Realtime)
        String namaKlien = saved.getKlien().getNamaKlien();
        String namaLayanan = saved.getLayanan().getNamaLayanan();
        
        notificationPublisher.publishFullNotification(
                "REQUEST_VERIFIED",
                "Request Berhasil Diverifikasi",
                String.format("%s meminta layanan %s. Menunggu verifikasi.", namaKlien, namaLayanan),
                "/admin/request-layanan/" + saved.getIdRequest(),
                saved.getKlien().getEmailKlien()
        );

        log.info("Request {} APPROVED and notification sent", id);
        return saved;
    }

    /**
     * REJECT REQUEST - dengan notifikasi realtime
     */
    @Transactional
    public RequestLayanan reject(Integer id, String keterangan) {
        RequestLayanan request = findById(id);
        
        if (request.getStatus() == StatusRequest.DITOLAK) {
            throw new RuntimeException("Request sudah ditolak sebelumnya");
        }

        request.setStatus(StatusRequest.DITOLAK);
        request.setTglVerifikasi(new Date());
        request.setKeteranganPenolakan(keterangan);

        RequestLayanan saved = requestLayananRepository.save(request);

        // PUBLISH NOTIFICATION TO RABBITMQ
        String namaKlien = saved.getKlien().getNamaKlien();
        String namaLayanan = saved.getLayanan().getNamaLayanan();
        
        notificationPublisher.publishFullNotification(
                "REQUEST_REJECTED",
                "Request Ditolak",
                String.format("Request dari %s untuk layanan %s telah ditolak. Alasan: %s", 
                        namaKlien, namaLayanan, keterangan),
                "/admin/request-layanan/" + saved.getIdRequest(),
                saved.getKlien().getEmailKlien()
        );

        log.info("Request {} REJECTED and notification sent", id);
        return saved;
    }

    public RequestLayanan save(RequestLayanan request) {
        return requestLayananRepository.save(request);
    }
}