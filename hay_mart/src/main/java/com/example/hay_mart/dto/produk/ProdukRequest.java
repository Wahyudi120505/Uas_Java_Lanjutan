package com.example.hay_mart.dto.produk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProdukRequest {
    private String nama;
    private Integer harga;
    private Integer stok;
    private String keterangan;
    private String kategori;
}
