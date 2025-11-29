package com.PPPL.backend.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeadScoringResponse {
    private String skorPrioritas;
    private String kategori;
    private String alasan;
    private Integer confidence;
    private String rekomendasi;
}
