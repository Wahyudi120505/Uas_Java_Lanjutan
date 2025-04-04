package com.example.hay_mart.dto.laporan;

import lombok.AllArgsConstructor;
import lombok.Builder;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaporanProdukResponse {
    private String namaProduk;
    private Integer jumlahTerjual;
    private Integer hargaSatuan;
    private Integer total;
}
