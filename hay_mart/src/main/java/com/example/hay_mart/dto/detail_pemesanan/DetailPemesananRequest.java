package com.example.hay_mart.dto.detail_pemesanan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DetailPemesananRequest {
    private String pemesanan;
    private String produk;
    private Integer jumlah;
    private Integer hargaSatuan;
    private Integer subtotal;
    
}
