package com.example.hay_mart.dto.laporan;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PendapatanResponse {
    public BigDecimal modal;
    public BigDecimal pendapatan;
    public BigDecimal keuntungan;
}
