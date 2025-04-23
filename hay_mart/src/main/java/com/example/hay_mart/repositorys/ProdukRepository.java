package com.example.hay_mart.repositorys;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.hay_mart.models.Produk;

public interface ProdukRepository extends JpaRepository<Produk, Integer> {
    boolean existsByNamaIgnoreCase(String nama);

    Optional<Produk> findByNama(String nama);

    List<Produk> findByDeletedFalse();
}
