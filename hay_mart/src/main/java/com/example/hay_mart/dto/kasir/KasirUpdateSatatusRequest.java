package com.example.hay_mart.dto.kasir;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KasirUpdateSatatusRequest {
    private String status;
}
