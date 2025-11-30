package com.PPPL.backend.controller;

import com.PPPL.backend.data.*;
import com.PPPL.backend.security.JwtUtil;
import com.PPPL.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * Login endpoint (public)
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success("Login berhasil", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Register admin (hanya SUPER_ADMIN)
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminDTO>> registerAdmin(@RequestBody RegisterAdminRequest request) {
        try {
            AdminDTO admin = authService.registerAdmin(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Admin berhasil didaftarkan", admin));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Get current user info
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AdminDTO>> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            
            AdminDTO admin = authService.getAdminByUsername(username);
            return ResponseEntity.ok(ApiResponse.success(admin));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Token tidak valid"));
        }
    }
    
    /**
     * Ubah password
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ChangePasswordRequest request) {
        try {
            String token = authHeader.substring(7);
            Integer idAdmin = jwtUtil.getAdminIdFromToken(token);
            
            authService.changePassword(idAdmin, request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success("Password berhasil diubah", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Validasi token
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            boolean isValid = jwtUtil.validateToken(token);
            
            if (isValid) {
                return ResponseEntity.ok(ApiResponse.success(true));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token tidak valid"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Token tidak valid"));
        }
    }
    
    /**
     * Get all admins (hanya SUPER_ADMIN)
     */
    @GetMapping("/admins")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<AdminDTO>>> getAllAdmins() {
        List<AdminDTO> admins = authService.getAllAdmins();
        return ResponseEntity.ok(ApiResponse.success(admins));
    }
    
    /**
     * Deactivate admin (hanya SUPER_ADMIN)
     */
    @PutMapping("/admins/{id}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateAdmin(@PathVariable Integer id) {
        try {
            authService.deactivateAdmin(id);
            return ResponseEntity.ok(ApiResponse.success("Admin berhasil dinonaktifkan", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Activate admin (hanya SUPER_ADMIN)
     */
    @PutMapping("/admins/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateAdmin(@PathVariable Integer id) {
        try {
            authService.activateAdmin(id);
            return ResponseEntity.ok(ApiResponse.success("Admin berhasil diaktifkan", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}