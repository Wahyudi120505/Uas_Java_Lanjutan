package com.example.hay_mart.models;

import jakarta.persistence.CascadeType;
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
public class DetailPemesanan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_pemesanan_id", nullable = false)
    private Integer detailPemesananId;

    @ManyToOne
    @JoinColumn(name = "pemesanan_id", referencedColumnName = "pemesanan_id", nullable = false)
    private Pemesanan pemesanan;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "produk_id", referencedColumnName = "produk_id", nullable = false)
    private Produk produk;

    @Column(nullable = false)
    private Integer jumlah;

    @Column(nullable = false)
    private Integer hargaSatuan;

    @Column(nullable = false)
    private Integer subtotal;
}
