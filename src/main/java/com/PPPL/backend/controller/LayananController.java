package com.PPPL.backend.controller;

import com.PPPL.backend.data.ApiResponse;
import com.PPPL.backend.data.LayananDTO;
import com.PPPL.backend.model.KategoriLayanan;
import com.PPPL.backend.model.Layanan;
import com.PPPL.backend.repository.LayananRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/layanan")
@CrossOrigin(origins = "http://localhost:4200")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER')")
public class LayananController {
    
    @Autowired
    private LayananRepository layananRepository;
    
    /**
     * Get all layanan
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<LayananDTO>>> getAllLayanan() {
        try {
            List<LayananDTO> layanan = layananRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(layanan));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Gagal memuat data layanan: " + e.getMessage()));
        }
    }
    
    /**
     * Get layanan by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LayananDTO>> getLayananById(@PathVariable Integer id) {
        try {
            Layanan layanan = layananRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Layanan dengan ID " + id + " tidak ditemukan"));
            
            return ResponseEntity.ok(ApiResponse.success(convertToDTO(layanan)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Create new layanan
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LayananDTO>> createLayanan(@RequestBody LayananDTO dto) {
        try {
            // Validasi
            if (dto.getNamaLayanan() == null || dto.getNamaLayanan().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Nama layanan wajib diisi"));
            }
            
            if (dto.getKategori() == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Kategori wajib dipilih"));
            }
            
            // Check duplicate name
            List<Layanan> existing = layananRepository
                    .findByNamaLayananContainingIgnoreCase(dto.getNamaLayanan());

            if (!existing.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Nama layanan sudah terdaftar"));
            }
            
            // Create entity
            Layanan layanan = new Layanan();
            layanan.setNamaLayanan(dto.getNamaLayanan());
            layanan.setKategori(dto.getKategori());
            layanan.setCatatan(dto.getCatatan());
            
            Layanan saved = layananRepository.save(layanan);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Layanan berhasil ditambahkan", convertToDTO(saved)));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Gagal menambah layanan: " + e.getMessage()));
        }
    }
    
    /**
     * Update layanan
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LayananDTO>> updateLayanan(
            @PathVariable Integer id, 
            @RequestBody LayananDTO dto) {
        try {
            Layanan layanan = layananRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Layanan dengan ID " + id + " tidak ditemukan"));
            
            // Check duplicate name (exclude current)
            List<Layanan> existingList = layananRepository
                    .findByNamaLayananContainingIgnoreCase(dto.getNamaLayanan());

            for (Layanan existing : existingList) {
                if (!existing.getIdLayanan().equals(id)) {
                    throw new RuntimeException("Nama layanan sudah terdaftar");
                }
            }
            
            // Update fields
            layanan.setNamaLayanan(dto.getNamaLayanan());
            layanan.setKategori(dto.getKategori());
            layanan.setCatatan(dto.getCatatan());
            
            Layanan updated = layananRepository.save(layanan);
            
            return ResponseEntity.ok(
                ApiResponse.success("Layanan berhasil diupdate", convertToDTO(updated)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Gagal update layanan: " + e.getMessage()));
        }
    }
    
    /**
     * Delete layanan
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteLayanan(@PathVariable Integer id) {
        try {
            Layanan layanan = layananRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Layanan dengan ID " + id + " tidak ditemukan"));
            
            // Check if layanan has requests
            if (!layanan.getRequestLayananSet().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(
                        "Layanan tidak dapat dihapus karena masih memiliki " + 
                        layanan.getRequestLayananSet().size() + " request"));
            }
            
            layananRepository.delete(layanan);
            
            return ResponseEntity.ok(ApiResponse.success("Layanan berhasil dihapus", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Gagal hapus layanan: " + e.getMessage()));
        }
    }
    
    /**
     * Search layanan
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<LayananDTO>>> searchLayanan(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) KategoriLayanan kategori) {
        try {
            List<Layanan> layanan = layananRepository.findAll();
            
            // Filter by keyword
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = keyword.toLowerCase();
                layanan = layanan.stream()
                    .filter(l -> l.getNamaLayanan().toLowerCase().contains(kw))
                    .collect(Collectors.toList());
            }
            
            // Filter by kategori
            if (kategori != null) {
                layanan = layanan.stream()
                    .filter(l -> l.getKategori() == kategori)
                    .collect(Collectors.toList());
            }
            
            List<LayananDTO> result = layanan.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Gagal search layanan: " + e.getMessage()));
        }
    }
    
    /**
     * Get layanan by kategori
     */
    @GetMapping("/kategori/{kategori}")
    public ResponseEntity<ApiResponse<List<LayananDTO>>> getLayananByKategori(
            @PathVariable KategoriLayanan kategori) {
        try {
            List<LayananDTO> layanan = layananRepository.findAll()
                .stream()
                .filter(l -> l.getKategori() == kategori)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(layanan));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Gagal load layanan: " + e.getMessage()));
        }
    }
    
    // ========== HELPER METHODS ==========
    
    private LayananDTO convertToDTO(Layanan layanan) {
        LayananDTO dto = new LayananDTO();
        dto.setIdLayanan(layanan.getIdLayanan());
        dto.setNamaLayanan(layanan.getNamaLayanan());
        dto.setKategori(layanan.getKategori());
        dto.setCatatan(layanan.getCatatan());
        return dto;
    }
}