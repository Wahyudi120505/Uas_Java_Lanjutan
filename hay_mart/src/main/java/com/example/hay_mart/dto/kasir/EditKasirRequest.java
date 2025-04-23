package com.example.hay_mart.dto.kasir;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditKasirRequest {
    private String nama;
    private String email;
    private String password;
}
