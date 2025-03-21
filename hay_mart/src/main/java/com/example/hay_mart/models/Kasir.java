package com.example.hay_mart.models;

import java.sql.Blob;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Kasir {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kasir_id", nullable = false)
    private Integer kasirId;

    private String nama;
    private String email;

    @Lob
    private Blob image;

    @Transient // Tidak akan disimpan di database
    private String base64Image;

    @OneToOne
    @JoinColumn(name = "akun_id", nullable = false)
    private Akun akun;
}
