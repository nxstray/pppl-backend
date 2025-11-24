package com.PPPL.backend.data;

import com.PPPL.backend.model.KategoriLayanan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LayananDTO {
    private Integer idLayanan;
    private String namaLayanan;
    private KategoriLayanan kategori;
    private String catatan;
}