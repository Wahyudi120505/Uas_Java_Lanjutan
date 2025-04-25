package com.example.hay_mart.repositorys;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.hay_mart.models.LaporanProduk;
import com.example.hay_mart.models.Produk;

public interface LaporanProdukRepository extends JpaRepository<LaporanProduk, Integer> {
    Optional<LaporanProduk> findByProduk(Produk produk);

    LaporanProduk findByProdukProdukId(Integer produkId);
}