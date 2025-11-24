package com.PPPL.backend.controller;

import com.PPPL.backend.data.ApiResponse;
import com.PPPL.backend.data.KlienDTO;
import com.PPPL.backend.model.Klien;
import com.PPPL.backend.model.StatusKlien;
import com.PPPL.backend.handler.ResourceNotFoundException;
import com.PPPL.backend.repository.KlienRepository;
import com.PPPL.backend.util.EntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/klien")
@CrossOrigin(origins = "http://localhost:4200")
public class KlienController {
    
    @Autowired
    private KlienRepository klienRepository;
    
    @Autowired
    private EntityMapper mapper;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<KlienDTO>>> getAllKlien() {
        List<KlienDTO> klienList = klienRepository.findAll()
            .stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(klienList));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KlienDTO>> getKlienById(@PathVariable Integer id) {
        Klien klien = klienRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Klien tidak ditemukan dengan ID: " + id));
        return ResponseEntity.ok(ApiResponse.success(mapper.toDTO(klien)));
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<KlienDTO>>> getKlienByStatus(@PathVariable StatusKlien status) {
        List<KlienDTO> klienList = klienRepository.findByStatus(status)
            .stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(klienList));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<KlienDTO>> createKlien(@RequestBody KlienDTO dto) {
        if (klienRepository.existsByEmailKlien(dto.getEmailKlien())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Email sudah terdaftar"));
        }
        
        Klien klien = new Klien();
        klien.setNamaKlien(dto.getNamaKlien());
        klien.setEmailKlien(dto.getEmailKlien());
        klien.setNoTelp(dto.getNoTelp());
        klien.setStatus(StatusKlien.BELUM); // default status
        klien.setTglRequest(new Date());
        
        Klien saved = klienRepository.save(klien);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Klien berhasil dibuat", mapper.toDTO(saved)));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<KlienDTO>> updateKlien(
            @PathVariable Integer id, 
            @RequestBody KlienDTO dto) {
        Klien klien = klienRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Klien tidak ditemukan"));
        
        klien.setNamaKlien(dto.getNamaKlien());
        klien.setEmailKlien(dto.getEmailKlien());
        klien.setNoTelp(dto.getNoTelp());
        klien.setStatus(dto.getStatus());
        
        Klien updated = klienRepository.save(klien);
        return ResponseEntity.ok(ApiResponse.success("Klien berhasil diupdate", mapper.toDTO(updated)));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteKlien(@PathVariable Integer id) {
        if (!klienRepository.existsById(id)) {
            throw new ResourceNotFoundException("Klien tidak ditemukan");
        }
        klienRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Klien berhasil dihapus", null));
    }
}
