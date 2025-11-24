package com.PPPL.backend.controller;

import com.PPPL.backend.data.ApiResponse;
import com.PPPL.backend.data.LayananDTO;
import com.PPPL.backend.model.KategoriLayanan;
import com.PPPL.backend.model.Layanan;
import com.PPPL.backend.handler.ResourceNotFoundException;
import com.PPPL.backend.repository.LayananRepository;
import com.PPPL.backend.util.EntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/layanan")
@CrossOrigin(origins = "http://localhost:4200")
public class LayananController {
    
    @Autowired
    private LayananRepository layananRepository;
    
    @Autowired
    private EntityMapper mapper;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<LayananDTO>>> getAllLayanan() {
        List<LayananDTO> layananList = layananRepository.findAll()
            .stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(layananList));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LayananDTO>> getLayananById(@PathVariable Integer id) {
        Layanan layanan = layananRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Layanan tidak ditemukan dengan ID: " + id));
        return ResponseEntity.ok(ApiResponse.success(mapper.toDTO(layanan)));
    }
    
    @GetMapping("/kategori/{kategori}")
    public ResponseEntity<ApiResponse<List<LayananDTO>>> getLayananByKategori(@PathVariable KategoriLayanan kategori) {
        List<LayananDTO> layananList = layananRepository.findByKategori(kategori)
            .stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(layananList));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<LayananDTO>> createLayanan(@RequestBody LayananDTO dto) {
        Layanan layanan = new Layanan();
        layanan.setNamaLayanan(dto.getNamaLayanan());
        layanan.setKategori(dto.getKategori());
        layanan.setCatatan(dto.getCatatan());
        
        Layanan saved = layananRepository.save(layanan);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Layanan berhasil dibuat", mapper.toDTO(saved)));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LayananDTO>> updateLayanan(
            @PathVariable Integer id, 
            @RequestBody LayananDTO dto) {
        Layanan layanan = layananRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Layanan tidak ditemukan"));
        
        layanan.setNamaLayanan(dto.getNamaLayanan());
        layanan.setKategori(dto.getKategori());
        layanan.setCatatan(dto.getCatatan());
        
        Layanan updated = layananRepository.save(layanan);
        return ResponseEntity.ok(ApiResponse.success("Layanan berhasil diupdate", mapper.toDTO(updated)));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLayanan(@PathVariable Integer id) {
        if (!layananRepository.existsById(id)) {
            throw new ResourceNotFoundException("Layanan tidak ditemukan");
        }
        layananRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Layanan berhasil dihapus", null));
    }
}
