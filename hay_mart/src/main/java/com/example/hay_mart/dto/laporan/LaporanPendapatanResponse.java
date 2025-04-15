package com.example.hay_mart.dto.laporan;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LaporanPendapatanResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal modal;
    private BigDecimal pendapatan;
    private BigDecimal keuntungan;
}
