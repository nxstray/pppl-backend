package com.PPPL.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Entity
@Table(name = "request_layanan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestLayanan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_request")
    private Integer idRequest;
    
    @ManyToOne
    @JoinColumn(name = "id_layanan", nullable = false)
    private Layanan layanan;
    
    @ManyToOne
    @JoinColumn(name = "id_klien", nullable = false)
    private Klien klien;
    
    @Column(name = "tgl_request")
    @Temporal(TemporalType.DATE)
    private Date tglRequest;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusRequest status = StatusRequest.MENUNGGU_VERIFIKASI;
    
    @Column(name = "tgl_verifikasi")
    @Temporal(TemporalType.DATE)
    private Date tglVerifikasi;
    
    // Menambahkan field untuk keterangan jika ditolak
    @Column(name = "keterangan_penolakan", columnDefinition = "TEXT")
    private String keteranganPenolakan;
}