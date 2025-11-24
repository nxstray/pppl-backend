package com.PPPL.backend.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KaryawanDTO {
    private Integer idKaryawan;
    private String namaKaryawan;
    private String emailKaryawan;
    private String noTelp;
    private String jabatanPosisi;
    private Integer idManager;
    private String namaManager; // buat display di FE
}