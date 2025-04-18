package com.example.hay_mart.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.example.hay_mart.enums.TipeLaporan;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaporanPendapatan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "laporan_pendapatan_id", nullable = false)
    private Integer laporanPendapatanId;

    private LocalDate tanggal;
    
    private LocalDate tanggalAkhir;
    
    private BigDecimal modal;
    private BigDecimal pendapatan;
    private BigDecimal keuntungan;
    
    @Enumerated(EnumType.STRING)
    private TipeLaporan tipe;
}