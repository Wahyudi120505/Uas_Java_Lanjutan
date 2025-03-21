package com.example.hay_mart.dto.produk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProdukResponse {
    private String nama;
    private Integer harga;
    private Integer stok;
    private String keterangan;
    private String image;
    private String kategori;
}
