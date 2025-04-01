package com.example.hay_mart.repositorys;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.hay_mart.models.Kategori;

public interface KategoriRepository extends JpaRepository<Kategori, Integer> {
    Kategori findKategoriByNama(String nama);
}
