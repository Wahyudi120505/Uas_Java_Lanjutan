package com.example.hay_mart.dto.pemesanan;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PemesananResponse {
    private String namaKasir;
    private LocalDateTime tanggalPembelian;
    private Long totalHarga;
    private List<DetailPemesananResponse> items;
}
