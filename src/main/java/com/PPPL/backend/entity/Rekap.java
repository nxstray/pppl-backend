package com.PPPL.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Entity
@Table(name = "rekap")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rekap {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_meeting")
    private Integer idMeeting;
    
    @ManyToOne
    @JoinColumn(name = "id_klien", nullable = false)
    private Klien klien;
    
    @ManyToOne
    @JoinColumn(name = "id_manager", nullable = false)
    private Manager manager;
    
    @ManyToOne
    @JoinColumn(name = "id_layanan", nullable = false)
    private Layanan layanan;
    
    @Column(name = "tgl_meeting")
    @Temporal(TemporalType.DATE)
    private Date tglMeeting;
    
    @Column(name = "hasil", columnDefinition = "TEXT")
    private String hasil;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusRekap status = StatusRekap.MASIH_JALAN;
    
    @Column(name = "catatan", columnDefinition = "TEXT")
    private String catatan;
}
