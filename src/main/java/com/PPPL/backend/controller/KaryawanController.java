package com.PPPL.backend.controller;

import com.PPPL.backend.data.ApiResponse;
import com.PPPL.backend.data.KaryawanDTO;
import com.PPPL.backend.model.Karyawan;
import com.PPPL.backend.model.Manager;
import com.PPPL.backend.handler.ResourceNotFoundException;
import com.PPPL.backend.repository.KaryawanRepository;
import com.PPPL.backend.repository.ManagerRepository;
import com.PPPL.backend.util.EntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/karyawan")
@CrossOrigin(origins = "http://localhost:4200")
public class KaryawanController {
    
    @Autowired
    private KaryawanRepository karyawanRepository;
    
    @Autowired
    private ManagerRepository managerRepository;
    
    @Autowired
    private EntityMapper mapper;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<KaryawanDTO>>> getAllKaryawan() {
        List<KaryawanDTO> karyawanList = karyawanRepository.findAll()
            .stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(karyawanList));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KaryawanDTO>> getKaryawanById(@PathVariable Integer id) {
        Karyawan karyawan = karyawanRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Karyawan tidak ditemukan dengan ID: " + id));
        return ResponseEntity.ok(ApiResponse.success(mapper.toDTO(karyawan)));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<KaryawanDTO>> createKaryawan(@RequestBody KaryawanDTO dto) {
        if (karyawanRepository.existsByEmailKaryawan(dto.getEmailKaryawan())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Email sudah terdaftar"));
        }
        
        Manager manager = managerRepository.findById(dto.getIdManager())
            .orElseThrow(() -> new ResourceNotFoundException("Manager tidak ditemukan"));
        
        Karyawan karyawan = new Karyawan();
        karyawan.setNamaKaryawan(dto.getNamaKaryawan());
        karyawan.setEmailKaryawan(dto.getEmailKaryawan());
        karyawan.setNoTelp(dto.getNoTelp());
        karyawan.setJabatanPosisi(dto.getJabatanPosisi());
        karyawan.setManager(manager);
        
        Karyawan saved = karyawanRepository.save(karyawan);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Karyawan berhasil dibuat", mapper.toDTO(saved)));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<KaryawanDTO>> updateKaryawan(
            @PathVariable Integer id, 
            @RequestBody KaryawanDTO dto) {
        Karyawan karyawan = karyawanRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Karyawan tidak ditemukan"));
        
        Manager manager = managerRepository.findById(dto.getIdManager())
            .orElseThrow(() -> new ResourceNotFoundException("Manager tidak ditemukan"));
        
        karyawan.setNamaKaryawan(dto.getNamaKaryawan());
        karyawan.setEmailKaryawan(dto.getEmailKaryawan());
        karyawan.setNoTelp(dto.getNoTelp());
        karyawan.setJabatanPosisi(dto.getJabatanPosisi());
        karyawan.setManager(manager);
        
        Karyawan updated = karyawanRepository.save(karyawan);
        return ResponseEntity.ok(ApiResponse.success("Karyawan berhasil diupdate", mapper.toDTO(updated)));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteKaryawan(@PathVariable Integer id) {
        if (!karyawanRepository.existsById(id)) {
            throw new ResourceNotFoundException("Karyawan tidak ditemukan");
        }
        karyawanRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Karyawan berhasil dihapus", null));
    }
}