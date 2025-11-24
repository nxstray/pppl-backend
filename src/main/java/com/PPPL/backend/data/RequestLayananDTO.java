package com.PPPL.backend.data;

import com.PPPL.backend.model.StatusRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestLayananDTO {
    private Integer idRequest;
    private Integer idLayanan;
    private String namaLayanan; // buat display di FE
    private Integer idKlien;
    private String namaKlien; // buat display di FE
    private Date tglRequest;
    private StatusRequest status;
    private Date tglVerifikasi;
    private String keteranganPenolakan;
}
