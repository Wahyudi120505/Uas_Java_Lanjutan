package com.example.hay_mart.models;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pemesanan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pemesanan_id", nullable = false)
    private Integer pemesananId;

    @ManyToOne
    @JoinColumn(name = "kasir_id", referencedColumnName = "user_id", nullable = false)
    private User userKasir;

    @Column(nullable = false)
    private LocalDateTime tanggalPembelian;

    @Column(nullable = false)
    private Long totalHarga;

    @OneToMany(mappedBy = "pemesanan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetailPemesanan> details;
}
