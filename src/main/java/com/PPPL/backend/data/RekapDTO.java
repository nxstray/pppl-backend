package com.PPPL.backend.data;

import com.PPPL.backend.model.StatusRekap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RekapDTO {
    private Integer idMeeting;
    private Integer idKlien;
    private String namaKlien; // buat display di FE
    private Integer idManager;
    private String namaManager; // buat display di FE
    private Integer idLayanan;
    private String namaLayanan; // buat display di FE
    private Date tglMeeting;
    private String hasil;
    private StatusRekap status;
    private String catatan;
}
