package com.example.hay_mart.models;

import java.sql.Blob;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
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
public class Produk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "produk_id", nullable = false)
    private Integer produkId;

    private String nama;
    private Integer harga;
    private Integer stok;
    private String keterangan;

    @Lob
    private Blob fotoProduk;

    @ManyToOne
    @JoinColumn(name = "kategori_id", referencedColumnName = "kategori_id", nullable = false)
    private Kategori kategori;
}
