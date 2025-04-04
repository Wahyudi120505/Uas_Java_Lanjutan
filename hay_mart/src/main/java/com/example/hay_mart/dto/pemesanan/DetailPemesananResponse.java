package com.example.hay_mart.dto.pemesanan;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DetailPemesananResponse {
    private String namaProduk;
    private Integer jumlah;
    private Integer hargaSatuan;
    private Integer subtotal;
}
