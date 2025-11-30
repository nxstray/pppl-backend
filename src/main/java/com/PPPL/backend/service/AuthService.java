package com.PPPL.backend.service;

import com.PPPL.backend.data.AdminDTO;
import com.PPPL.backend.data.LoginRequest;
import com.PPPL.backend.data.LoginResponse;
import com.PPPL.backend.data.RegisterAdminRequest;
import com.PPPL.backend.model.Admin;
import com.PPPL.backend.model.AdminRole;
import com.PPPL.backend.handler.ResourceNotFoundException;
import com.PPPL.backend.repository.AdminRepository;
import com.PPPL.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * Login admin
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // Find admin by username
        Admin admin = adminRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("Username atau password salah"));
        
        // Check if admin is active
        if (!admin.getIsActive()) {
            throw new RuntimeException("Akun Anda telah dinonaktifkan. Hubungi super admin.");
        }
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new RuntimeException("Username atau password salah");
        }
        
        // Update last login
        admin.setLastLogin(new Date());
        adminRepository.save(admin);
        
        // Generate JWT token
        String token = jwtUtil.generateToken(
            admin.getUsername(), 
            admin.getIdAdmin(), 
            admin.getRole().name()
        );
        
        return new LoginResponse(
            token,
            admin.getIdAdmin(),
            admin.getUsername(),
            admin.getNamaLengkap(),
            admin.getEmail(),
            admin.getRole()
        );
    }
    
    /**
     * Register new admin (hanya bisa dilakukan oleh SUPER_ADMIN)
     */
    @Transactional
    public AdminDTO registerAdmin(RegisterAdminRequest request) {
        // Check if username already exists
        if (adminRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username sudah digunakan");
        }
        
        // Check if email already exists
        if (adminRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email sudah digunakan");
        }
        
        // Create new admin
        Admin admin = new Admin();
        admin.setUsername(request.getUsername());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setNamaLengkap(request.getNamaLengkap());
        admin.setEmail(request.getEmail());
        admin.setRole(request.getRole() != null ? request.getRole() : AdminRole.ADMIN);
        admin.setIsActive(true);
        
        Admin saved = adminRepository.save(admin);
        
        return mapToDTO(saved);
    }
    
    /**
     * Ubah password
     */
    @Transactional
    public void changePassword(Integer idAdmin, String oldPassword, String newPassword) {
        Admin admin = adminRepository.findById(idAdmin)
            .orElseThrow(() -> new ResourceNotFoundException("Admin tidak ditemukan"));
        
        // Verify old password
        if (!passwordEncoder.matches(oldPassword, admin.getPassword())) {
            throw new RuntimeException("Password lama tidak sesuai");
        }
        
        // Update password
        admin.setPassword(passwordEncoder.encode(newPassword));
        adminRepository.save(admin);
    }
    
    /**
     * Get admin by username
     */
    public AdminDTO getAdminByUsername(String username) {
        Admin admin = adminRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Admin tidak ditemukan"));
        return mapToDTO(admin);
    }
    
    /**
     * Get all admins
     */
    public List<AdminDTO> getAllAdmins() {
        return adminRepository.findAll()
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Deactivate admin
     */
    @Transactional
    public void deactivateAdmin(Integer idAdmin) {
        Admin admin = adminRepository.findById(idAdmin)
            .orElseThrow(() -> new ResourceNotFoundException("Admin tidak ditemukan"));
        admin.setIsActive(false);
        adminRepository.save(admin);
    }
    
    /**
     * Activate admin
     */
    @Transactional
    public void activateAdmin(Integer idAdmin) {
        Admin admin = adminRepository.findById(idAdmin)
            .orElseThrow(() -> new ResourceNotFoundException("Admin tidak ditemukan"));
        admin.setIsActive(true);
        adminRepository.save(admin);
    }
    
    private AdminDTO mapToDTO(Admin admin) {
        AdminDTO dto = new AdminDTO();
        dto.setIdAdmin(admin.getIdAdmin());
        dto.setUsername(admin.getUsername());
        dto.setNamaLengkap(admin.getNamaLengkap());
        dto.setEmail(admin.getEmail());
        dto.setRole(admin.getRole());
        dto.setIsActive(admin.getIsActive());
        dto.setLastLogin(admin.getLastLogin());
        dto.setCreatedAt(admin.getCreatedAt());
        return dto;
    }
}