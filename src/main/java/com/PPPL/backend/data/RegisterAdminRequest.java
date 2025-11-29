package com.PPPL.backend.data;

import com.PPPL.backend.model.AdminRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterAdminRequest {
    private String username;
    private String password;
    private String namaLengkap;
    private String email;
    private AdminRole role;
}
