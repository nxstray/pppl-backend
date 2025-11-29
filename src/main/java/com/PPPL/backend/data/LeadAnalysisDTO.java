package com.PPPL.backend.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeadAnalysisDTO {
    private Integer idRequest;
    private String namaKlien;
    private String emailKlien;
    private String perusahaan;
    private String layanan;
    private String skorPrioritas;
    private String kategoriLead;
    private String alasanSkor;
    private String statusRequest;
    private java.util.Date tglRequest;
    private java.util.Date tglAnalisaAi;
    private Boolean aiAnalyzed;
}
