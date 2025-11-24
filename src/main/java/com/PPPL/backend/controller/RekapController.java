package com.PPPL.backend.controller;

import com.PPPL.backend.data.ApiResponse;
import com.PPPL.backend.data.RekapDTO;
import com.PPPL.backend.model.Rekap;
import com.PPPL.backend.model.StatusRekap;
import com.PPPL.backend.handler.ResourceNotFoundException;
import com.PPPL.backend.repository.RekapRepository;
import com.PPPL.backend.service.RekapService;
import com.PPPL.backend.util.EntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rekap")
@CrossOrigin(origins = "http://localhost:4200")
public class RekapController {
    
    @Autowired
    private RekapService rekapService;
    
    @Autowired
    private RekapRepository rekapRepository;
    
    @Autowired
    private EntityMapper mapper;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<RekapDTO>>> getAllRekap() {
        List<RekapDTO> rekapList = rekapRepository.findAll()
            .stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(rekapList));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RekapDTO>> getRekapById(@PathVariable Integer id) {
        Rekap rekap = rekapRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Rekap tidak ditemukan dengan ID: " + id));
        return ResponseEntity.ok(ApiResponse.success(mapper.toDTO(rekap)));
    }
    
    @GetMapping("/klien/{idKlien}")
    public ResponseEntity<ApiResponse<List<RekapDTO>>> getRekapByKlien(@PathVariable Integer idKlien) {
        List<RekapDTO> rekapList = rekapRepository.findByKlien_IdKlien(idKlien)
            .stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(rekapList));
    }
    
    @GetMapping("/manager/{idManager}")
    public ResponseEntity<ApiResponse<List<RekapDTO>>> getRekapByManager(@PathVariable Integer idManager) {
        List<RekapDTO> rekapList = rekapRepository.findByManager_IdManager(idManager)
            .stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(rekapList));
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<RekapDTO>> updateStatusRekap(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> payload) {
        String statusStr = (String) payload.get("status");
        StatusRekap status = StatusRekap.valueOf(statusStr);
        String catatan = (String) payload.get("catatan");
        
        Rekap updated = rekapService.updateStatusRekap(id, status, catatan);
        return ResponseEntity.ok(ApiResponse.success("Status rekap berhasil diupdate", mapper.toDTO(updated)));
    }
    
    @PostMapping("/{id}/catatan")
    public ResponseEntity<ApiResponse<RekapDTO>> tambahCatatan(
            @PathVariable Integer id,
            @RequestBody Map<String, String> payload) {
        String catatan = payload.get("catatan");
        
        Rekap updated = rekapService.tambahCatatan(id, catatan);
        return ResponseEntity.ok(ApiResponse.success("Catatan berhasil ditambahkan", mapper.toDTO(updated)));
    }
}