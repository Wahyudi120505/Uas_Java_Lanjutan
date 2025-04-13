package com.example.hay_mart.dto.kasir;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KasirRequest {
    private String nama;
    private String email;
    private String status;
}
