package com.PPPL.backend.controller;

import com.PPPL.backend.data.ApiResponse;
import com.PPPL.backend.data.RequestLayananDTO;
import com.PPPL.backend.data.RequestLayananDetailDTO;
import com.PPPL.backend.data.RequestLayananStatisticsDTO;
import com.PPPL.backend.model.Klien;
import com.PPPL.backend.model.RequestLayanan;
import com.PPPL.backend.model.StatusRequest;
import com.PPPL.backend.service.RequestLayananService;
import com.PPPL.backend.repository.KlienRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/request-layanan")
@CrossOrigin(origins = "http://localhost:4200")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','MANAGER')")
public class RequestLayananController {

    @Autowired
    private RequestLayananService requestLayananService;

    @Autowired
    private KlienRepository klienRepository;

    /**       
     * Get all request layanan
    **/
    @GetMapping
    public ResponseEntity<ApiResponse<List<RequestLayananDTO>>> getAll() {
        List<RequestLayananDTO> data = requestLayananService.findAll()
                .stream()
                .map(this::toDTO)
                .sorted(Comparator.comparing(RequestLayananDTO::getTglRequest).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**       
     * Get request layanan by ID
    **/
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RequestLayananDetailDTO>> getById(@PathVariable Integer id) {

        RequestLayanan r = requestLayananService.findById(id);

        RequestLayananDetailDTO dto = new RequestLayananDetailDTO();
        dto.setIdRequest(r.getIdRequest());
        dto.setTglRequest(r.getTglRequest());
        dto.setStatus(r.getStatus());
        dto.setTglVerifikasi(r.getTglVerifikasi());
        dto.setKeteranganPenolakan(r.getKeteranganPenolakan());

        // Klien
        dto.setIdKlien(r.getKlien().getIdKlien());
        dto.setNamaKlien(r.getKlien().getNamaKlien());
        dto.setEmailKlien(r.getKlien().getEmailKlien());
        dto.setNoTelpKlien(r.getKlien().getNoTelp());
        dto.setPerusahaan(r.getPerusahaan());

        // Layanan
        dto.setIdLayanan(r.getLayanan().getIdLayanan());
        dto.setNamaLayanan(r.getLayanan().getNamaLayanan());
        dto.setKategoriLayanan(r.getLayanan().getKategori().name());

        // Detail form
        dto.setPesan(r.getPesan());
        dto.setAnggaran(r.getAnggaran());
        dto.setWaktuImplementasi(r.getWaktuImplementasi());

        // AI
        dto.setAiAnalyzed(r.getAiAnalyzed());
        dto.setSkorPrioritas(r.getSkorPrioritas());
        dto.setKategoriLead(r.getKategoriLead());
        dto.setAlasanSkor(r.getAlasanSkor());

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    /**       
     * Get request layanan by status
    **/
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<RequestLayananDTO>>> byStatus(
            @PathVariable StatusRequest status) {

        List<RequestLayananDTO> data = requestLayananService.findByStatus(status)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**       
     * Get request layanan statistics
     **/
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<RequestLayananStatisticsDTO>> statistics() {
        return ResponseEntity.ok(
            ApiResponse.success(requestLayananService.getStatistics())
        );
    }

    /**       
     * Get all active klien
     **/    
@GetMapping("/active-klien")
public ResponseEntity<ApiResponse<List<Klien>>> getActiveKlien() {
    return ResponseEntity.ok(
        ApiResponse.success(
            klienRepository.findKlienYangTerverifikasi()
        )
    );
}

    /**       
     * Approve request layanan
    **/
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<RequestLayananDTO>> approve(@PathVariable Integer id) {
        RequestLayanan approved = requestLayananService.approve(id);
        return ResponseEntity.ok(
                ApiResponse.success("Request berhasil diverifikasi", toDTO(approved))
        );
    }

    /**       
     * Reject request layanan
    **/
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<RequestLayananDTO>> reject(
            @PathVariable Integer id,
            @RequestBody RejectRequest body) {

        RequestLayanan rejected = requestLayananService.reject(id, body.keterangan);
        return ResponseEntity.ok(
                ApiResponse.success("Request berhasil ditolak", toDTO(rejected))
        );
    }

    /**       
     * DTO Mapper
    **/
    private RequestLayananDTO toDTO(RequestLayanan r) {
        RequestLayananDTO dto = new RequestLayananDTO();
        dto.setIdRequest(r.getIdRequest());
        dto.setIdLayanan(r.getLayanan().getIdLayanan());
        dto.setNamaLayanan(r.getLayanan().getNamaLayanan());
        dto.setIdKlien(r.getKlien().getIdKlien());
        dto.setNamaKlien(r.getKlien().getNamaKlien());
        dto.setTglRequest(r.getTglRequest());
        dto.setStatus(r.getStatus());
        dto.setTglVerifikasi(r.getTglVerifikasi());
        dto.setKeteranganPenolakan(r.getKeteranganPenolakan());
        return dto;
    }

    /**       
     * Inner class for reject request body
    **/
    public static class RejectRequest {
        public String keterangan;
    }
}
