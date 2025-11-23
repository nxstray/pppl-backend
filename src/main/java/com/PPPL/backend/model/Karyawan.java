package com.PPPL.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "karyawan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Karyawan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_karyawan")
    private Integer idKaryawan;
    
    @Column(name = "nama_karyawan", nullable = false, length = 100)
    private String namaKaryawan;
    
    @Column(name = "email_karyawan", nullable = false, length = 100, unique = true)
    private String emailKaryawan;
    
    @Column(name = "no_telp", nullable = false, length = 20)
    private String noTelp;
    
    @Column(name = "jabatan_posisi", nullable = false, length = 50)
    private String jabatanPosisi;
    
    @ManyToOne
    @JoinColumn(name = "id_manager", nullable = false)
    private Manager manager;
}