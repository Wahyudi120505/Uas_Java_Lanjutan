package com.example.hay_mart.dto.laporan;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaporanPendapatanResponse {
    private LocalDate startDate;
    private LocalDate endDate; 
    private BigDecimal pendapatan;
    private BigDecimal modal;
    private BigDecimal keuntungan;
    private String tipe; 
}