package com.PPPL.backend.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManagerDTO {
    private Integer idManager;
    private String namaManager;
    private String emailManager;
    private String noTelp;
    private String divisi;
    private Date tglMulai;
}
