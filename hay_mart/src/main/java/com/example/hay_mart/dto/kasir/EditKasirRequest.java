package com.example.hay_mart.dto.kasir;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditKasirRequest {
    private String nama;
    private String email;
}
