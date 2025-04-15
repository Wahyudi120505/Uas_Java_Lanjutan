package com.example.hay_mart.models;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private BigDecimal modal;
    private BigDecimal pendapatan;
    private BigDecimal keuntungan;
}
