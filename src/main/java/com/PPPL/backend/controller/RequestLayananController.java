package com.PPPL.backend.controller;

import com.PPPL.backend.data.ApiResponse;
import com.PPPL.backend.data.RequestLayananDTO;
import com.PPPL.backend.model.RequestLayanan;
import com.PPPL.backend.handler.ResourceNotFoundException;
import com.PPPL.backend.repository.RequestLayananRepository;
import com.PPPL.backend.service.RequestLayananService;
import com.PPPL.backend.util.EntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/request-layanan")
@CrossOrigin(origins = "http://localhost:4200")
public class RequestLayananController {
    
    @Autowired
    private RequestLayananService requestLayananService;
    
    @Autowired
    private RequestLayananRepository requestLayananRepository;
    
    @Autowired
    private EntityMapper mapper;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<RequestLayananDTO>>> getAllRequestLayanan() {
        List<RequestLayananDTO> requestList = requestLayananRepository.findAll()
            .stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(requestList));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RequestLayananDTO>> getRequestLayananById(@PathVariable Integer id) {
        RequestLayanan request = requestLayananRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Request layanan tidak ditemukan dengan ID: " + id));
        return ResponseEntity.ok(ApiResponse.success(mapper.toDTO(request)));
    }
    
    @GetMapping("/menunggu-verifikasi")
    public ResponseEntity<ApiResponse<List<RequestLayananDTO>>> getRequestMenungguVerifikasi() {
        List<RequestLayananDTO> requestList = requestLayananService.getRequestMenungguVerifikasi()
            .stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(requestList));
    }
    
    @GetMapping("/klien/{idKlien}")
    public ResponseEntity<ApiResponse<List<RequestLayananDTO>>> getRequestByKlien(@PathVariable Integer idKlien) {
        List<RequestLayananDTO> requestList = requestLayananService.getRequestByKlien(idKlien)
            .stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(requestList));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<RequestLayananDTO>> createRequestLayanan(@RequestBody Map<String, Integer> payload) {
        Integer idKlien = payload.get("idKlien");
        Integer idLayanan = payload.get("idLayanan");
        
        RequestLayanan created = requestLayananService.createRequestLayanan(idKlien, idLayanan);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Request layanan berhasil dibuat", mapper.toDTO(created)));
    }
    
    @PostMapping("/{id}/verifikasi")
    public ResponseEntity<ApiResponse<Void>> verifikasiRequestLayanan(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> payload) {
        Integer idManager = (Integer) payload.get("idManager");
        String hasilMeeting = (String) payload.get("hasilMeeting");
        
        requestLayananService.verifikasiRequestLayanan(id, idManager, hasilMeeting);
        return ResponseEntity.ok(ApiResponse.success("Request berhasil diverifikasi", null));
    }
    
    @PostMapping("/{id}/tolak")
    public ResponseEntity<ApiResponse<Void>> tolakRequestLayanan(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> payload) {
        String keteranganPenolakan = (String) payload.get("keteranganPenolakan");
        Boolean hapusKlien = (Boolean) payload.getOrDefault("hapusKlien", false);
        
        requestLayananService.tolakRequestLayanan(id, keteranganPenolakan, hapusKlien);
        return ResponseEntity.ok(ApiResponse.success("Request berhasil ditolak", null));
    }
}
