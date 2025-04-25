package com.example.hay_mart.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaporanProduk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "laporan_produk_id", nullable = false)
    private Integer laporanProdukId;

    @ManyToOne
    @JoinColumn(name = "produk_id", referencedColumnName = "produk_id", nullable = false)
    private Produk produk;

    private String namaProduk;
    private Integer jumlahTerjual;
    private Integer stok;
    private Integer hargaSatuan;
    private Integer total;
    private Boolean deleted;
}
