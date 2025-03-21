package com.example.hay_mart.repositorys;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.hay_mart.models.Produk;

public interface ProdukRepository extends JpaRepository<Produk, Integer>{
    
}
