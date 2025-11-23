package com.PPPL.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "klien")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Klien {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_klien")
    private Integer idKlien;
    
    @Column(name = "nama_klien", nullable = false, length = 100)
    private String namaKlien;
    
    @Column(name = "email_klien", nullable = false, length = 100, unique = true)
    private String emailKlien;
    
    @Column(name = "no_telp", nullable = false, length = 20)
    private String noTelp;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusKlien status = StatusKlien.BELUM;
    
    @Column(name = "tgl_request")
    @Temporal(TemporalType.DATE)
    private Date tglRequest;
    
    @ManyToMany(mappedBy = "klienSet")
    private Set<Manager> managerSet = new HashSet<>();
    
    @OneToMany(mappedBy = "klien", cascade = CascadeType.ALL)
    private Set<RequestLayanan> requestLayananSet = new HashSet<>();
    
    @OneToMany(mappedBy = "klien", cascade = CascadeType.ALL)
    private Set<Rekap> rekapSet = new HashSet<>();
}