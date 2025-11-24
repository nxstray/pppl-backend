package com.PPPL.backend.data;

import com.PPPL.backend.model.StatusKlien;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KlienDTO {
    private Integer idKlien;
    private String namaKlien;
    private String emailKlien;
    private String noTelp;
    private StatusKlien status;
    private Date tglRequest;
}
