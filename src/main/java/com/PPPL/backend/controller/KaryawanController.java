package com.PPPL.backend.controller;

import com.PPPL.backend.data.ApiResponse;
import com.PPPL.backend.data.KaryawanDTO;
import com.PPPL.backend.model.Karyawan;
import com.PPPL.backend.model.Manager;
import com.PPPL.backend.repository.KaryawanRepository;
import com.PPPL.backend.repository.ManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/karyawan")
@CrossOrigin(origins = "http://localhost:4200")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER')")
public class KaryawanController {
    
    @Autowired
    private KaryawanRepository karyawanRepository;
    
    @Autowired
    private ManagerRepository managerRepository;
    
    /**
     * Get all karyawan
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<KaryawanDTO>>> getAllKaryawan() {
        try {
            List<KaryawanDTO> karyawan = karyawanRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(karyawan));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Gagal memuat data karyawan: " + e.getMessage()));
        }
    }
    
    /**
     * Get karyawan by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KaryawanDTO>> getKaryawanById(@PathVariable Integer id) {
        try {
            Karyawan karyawan = karyawanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Karyawan dengan ID " + id + " tidak ditemukan"));
            
            return ResponseEntity.ok(ApiResponse.success(convertToDTO(karyawan)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Create new karyawan
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<KaryawanDTO>> createKaryawan(@RequestBody KaryawanDTO dto) {
        try {
            // Validasi
            if (dto.getNamaKaryawan() == null || dto.getNamaKaryawan().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Nama karyawan wajib diisi"));
            }
            
            if (dto.getEmailKaryawan() == null || dto.getEmailKaryawan().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email wajib diisi"));
            }
            
            if (dto.getIdManager() == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Manager wajib dipilih"));
            }
            
            // Check duplicate email
            if (karyawanRepository.existsByEmailKaryawan(dto.getEmailKaryawan())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email sudah terdaftar"));
            }
            
            // Get manager
            Manager manager = managerRepository.findById(dto.getIdManager())
                .orElseThrow(() -> new RuntimeException("Manager tidak ditemukan"));
            
            // Create entity
            Karyawan karyawan = new Karyawan();
            karyawan.setNamaKaryawan(dto.getNamaKaryawan());
            karyawan.setEmailKaryawan(dto.getEmailKaryawan());
            karyawan.setNoTelp(dto.getNoTelp());
            karyawan.setJabatanPosisi(dto.getJabatanPosisi());
            karyawan.setManager(manager);
            
            Karyawan saved = karyawanRepository.save(karyawan);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Karyawan berhasil ditambahkan", convertToDTO(saved)));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Gagal menambah karyawan: " + e.getMessage()));
        }
    }
    
    /**
     * Update karyawan
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<KaryawanDTO>> updateKaryawan(
            @PathVariable Integer id, 
            @RequestBody KaryawanDTO dto) {
        try {
            Karyawan karyawan = karyawanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Karyawan dengan ID " + id + " tidak ditemukan"));
            
            // Check duplicate email (exclude current)
            if (!karyawan.getEmailKaryawan().equals(dto.getEmailKaryawan()) && 
                karyawanRepository.existsByEmailKaryawan(dto.getEmailKaryawan())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email sudah terdaftar"));
            }
            
            // Get manager
            Manager manager = managerRepository.findById(dto.getIdManager())
                .orElseThrow(() -> new RuntimeException("Manager tidak ditemukan"));
            
            // Update fields
            karyawan.setNamaKaryawan(dto.getNamaKaryawan());
            karyawan.setEmailKaryawan(dto.getEmailKaryawan());
            karyawan.setNoTelp(dto.getNoTelp());
            karyawan.setJabatanPosisi(dto.getJabatanPosisi());
            karyawan.setManager(manager);
            
            Karyawan updated = karyawanRepository.save(karyawan);
            
            return ResponseEntity.ok(
                ApiResponse.success("Karyawan berhasil diupdate", convertToDTO(updated)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Gagal update karyawan: " + e.getMessage()));
        }
    }
    
    /**
     * Delete karyawan
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteKaryawan(@PathVariable Integer id) {
        try {
            if (!karyawanRepository.existsById(id)) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Karyawan dengan ID " + id + " tidak ditemukan"));
            }
            
            karyawanRepository.deleteById(id);
            
            return ResponseEntity.ok(ApiResponse.success("Karyawan berhasil dihapus", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Gagal hapus karyawan: " + e.getMessage()));
        }
    }
    
    /**
     * Search karyawan
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<KaryawanDTO>>> searchKaryawan(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer idManager) {
        try {
            List<Karyawan> karyawan = karyawanRepository.findAll();
            
            // Filter by keyword
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = keyword.toLowerCase();
                karyawan = karyawan.stream()
                    .filter(k -> k.getNamaKaryawan().toLowerCase().contains(kw) ||
                               k.getEmailKaryawan().toLowerCase().contains(kw) ||
                               k.getJabatanPosisi().toLowerCase().contains(kw))
                    .collect(Collectors.toList());
            }
            
            // Filter by manager
            if (idManager != null) {
                karyawan = karyawan.stream()
                    .filter(k -> k.getManager().getIdManager().equals(idManager))
                    .collect(Collectors.toList());
            }
            
            List<KaryawanDTO> result = karyawan.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Gagal search karyawan: " + e.getMessage()));
        }
    }
    
    /**
     * Get karyawan by manager
     */
    @GetMapping("/manager/{idManager}")
    public ResponseEntity<ApiResponse<List<KaryawanDTO>>> getKaryawanByManager(@PathVariable Integer idManager) {
        try {
            List<KaryawanDTO> karyawan = karyawanRepository.findAll()
                .stream()
                .filter(k -> k.getManager().getIdManager().equals(idManager))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(karyawan));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Gagal load karyawan: " + e.getMessage()));
        }
    }
    
    // ========== HELPER METHODS ==========
    
    private KaryawanDTO convertToDTO(Karyawan karyawan) {
        KaryawanDTO dto = new KaryawanDTO();
        dto.setIdKaryawan(karyawan.getIdKaryawan());
        dto.setNamaKaryawan(karyawan.getNamaKaryawan());
        dto.setEmailKaryawan(karyawan.getEmailKaryawan());
        dto.setNoTelp(karyawan.getNoTelp());
        dto.setJabatanPosisi(karyawan.getJabatanPosisi());
        dto.setIdManager(karyawan.getManager().getIdManager());
        dto.setNamaManager(karyawan.getManager().getNamaManager());
        return dto;
    }
}