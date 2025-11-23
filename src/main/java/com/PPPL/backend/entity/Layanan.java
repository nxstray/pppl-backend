package com.PPPL.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "layanan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Layanan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_layanan")
    private Integer idLayanan;
    
    @Column(name = "nama_layanan", nullable = false, length = 100)
    private String namaLayanan;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "kategori", nullable = false)
    private KategoriLayanan kategori;
    
    @Column(name = "catatan", columnDefinition = "TEXT")
    private String catatan;
    
    @OneToMany(mappedBy = "layanan", cascade = CascadeType.ALL)
    private Set<RequestLayanan> requestLayananSet = new HashSet<>();
    
    @OneToMany(mappedBy = "layanan", cascade = CascadeType.ALL)
    private Set<Rekap> rekapSet = new HashSet<>();
}