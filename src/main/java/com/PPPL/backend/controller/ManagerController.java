package com.PPPL.backend.controller;

import com.PPPL.backend.data.ApiResponse;
import com.PPPL.backend.data.ManagerDTO;
import com.PPPL.backend.model.Manager;
import com.PPPL.backend.handler.ResourceNotFoundException;
import com.PPPL.backend.repository.ManagerRepository;
import com.PPPL.backend.util.EntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/manager")
@CrossOrigin(origins = "http://localhost:4200")
public class ManagerController {
    
    @Autowired
    private ManagerRepository managerRepository;
    
    @Autowired
    private EntityMapper mapper;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<ManagerDTO>>> getAllManager() {
        List<ManagerDTO> managerList = managerRepository.findAll()
            .stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(managerList));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ManagerDTO>> getManagerById(@PathVariable Integer id) {
        Manager manager = managerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Manager tidak ditemukan dengan ID: " + id));
        return ResponseEntity.ok(ApiResponse.success(mapper.toDTO(manager)));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<ManagerDTO>> createManager(@RequestBody ManagerDTO dto) {
        if (managerRepository.existsByEmailManager(dto.getEmailManager())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Email sudah terdaftar"));
        }
        
        Manager manager = new Manager();
        manager.setNamaManager(dto.getNamaManager());
        manager.setEmailManager(dto.getEmailManager());
        manager.setNoTelp(dto.getNoTelp());
        manager.setDivisi(dto.getDivisi());
        manager.setTglMulai(dto.getTglMulai());
        
        Manager saved = managerRepository.save(manager);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Manager berhasil dibuat", mapper.toDTO(saved)));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ManagerDTO>> updateManager(
            @PathVariable Integer id, 
            @RequestBody ManagerDTO dto) {
        Manager manager = managerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Manager tidak ditemukan"));
        
        manager.setNamaManager(dto.getNamaManager());
        manager.setEmailManager(dto.getEmailManager());
        manager.setNoTelp(dto.getNoTelp());
        manager.setDivisi(dto.getDivisi());
        manager.setTglMulai(dto.getTglMulai());
        
        Manager updated = managerRepository.save(manager);
        return ResponseEntity.ok(ApiResponse.success("Manager berhasil diupdate", mapper.toDTO(updated)));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteManager(@PathVariable Integer id) {
        if (!managerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Manager tidak ditemukan");
        }
        managerRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Manager berhasil dihapus", null));
    }
}