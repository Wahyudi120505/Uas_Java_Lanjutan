package com.example.hay_mart.dto.kasir;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KasirResponse {
    private String nama;
    private String email;
    private String status;
    private String role;
    private String image;
}
