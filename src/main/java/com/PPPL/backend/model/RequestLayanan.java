package com.PPPL.backend.model;

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
    
    @Column(name = "keterangan_penolakan", columnDefinition = "TEXT")
    private String keteranganPenolakan;

    // Data dari form client
    @Column(name = "perusahaan", length = 200)
    private String perusahaan;
    
    @Column(name = "topic", length = 100)
    private String topic;
    
    @Column(name = "pesan", columnDefinition = "TEXT")
    private String pesan;
    
    @Column(name = "anggaran", length = 50)
    private String anggaran;
    
    @Column(name = "waktu_implementasi", length = 50)
    private String waktuImplementasi;

    @Column(name = "skor_prioritas", length = 20)
    private String skorPrioritas;
    
    @Column(name = "kategori_lead", length = 50)
    private String kategoriLead;
    
    @Column(name = "alasan_skor", columnDefinition = "TEXT")
    private String alasanSkor;
    
    @Column(name = "tgl_analisa_ai")
    @Temporal(TemporalType.TIMESTAMP)
    private Date tglAnalisaAi;
    
    @Column(name = "ai_analyzed")
    private Boolean aiAnalyzed = false;
}