package com.PPPL.backend.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeadScoringRequest {
    private Integer idRequest;
    private String namaKlien;
    private String perusahaan;
    private String layanan;
    private String topic;
    private String pesan;
    private String anggaran;
    private String waktuImplementasi;
    private String emailKlien;
    private String noTelp;
}