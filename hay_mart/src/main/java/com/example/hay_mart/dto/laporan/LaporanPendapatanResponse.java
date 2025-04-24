package com.example.hay_mart.dto.laporan;

import java.math.BigDecimal;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaporanPendapatanResponse {
    private String periode;
    private BigDecimal pendapatan;
    private BigDecimal modal;
    private BigDecimal keuntungan;
}