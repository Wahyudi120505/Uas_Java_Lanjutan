package com.example.hay_mart.repositorys;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.hay_mart.models.Produk;

public interface ProdukRepository extends JpaRepository<Produk, Integer> {
    boolean existsByNamaIgnoreCase(String nama);
    // Produk existsByNamaIgnoreCaseAndDeletedTrue(String nama);

    Produk findByNamaIgnoreCaseAndDeletedTrue(String nama);

    List<Produk> findByDeletedFalse();
}
