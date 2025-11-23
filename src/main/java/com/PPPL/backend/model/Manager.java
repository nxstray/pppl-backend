package com.PPPL.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "manager")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Manager {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_manager")
    private Integer idManager;
    
    @Column(name = "nama_manager", nullable = false, length = 100)
    private String namaManager;
    
    @Column(name = "email_manager", nullable = false, length = 100, unique = true)
    private String emailManager;
    
    @Column(name = "no_telp", nullable = false, length = 20)
    private String noTelp;
    
    @Column(name = "divisi", nullable = false, length = 50)
    private String divisi;
    
    @Column(name = "tgl_mulai")
    @Temporal(TemporalType.DATE)
    private Date tglMulai;
    
    @OneToMany(mappedBy = "manager", cascade = CascadeType.ALL)
    private Set<Karyawan> karyawanSet = new HashSet<>();
    
    @ManyToMany
    @JoinTable(
        name = "manager_klien",
        joinColumns = @JoinColumn(name = "id_manager"),
        inverseJoinColumns = @JoinColumn(name = "id_klien")
    )
    private Set<Klien> klienSet = new HashSet<>();
}
