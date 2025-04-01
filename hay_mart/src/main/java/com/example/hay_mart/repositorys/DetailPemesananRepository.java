package com.example.hay_mart.repositorys;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.hay_mart.models.DetailPemesanan;

public interface DetailPemesananRepository extends JpaRepository<DetailPemesanan, Integer> {
    
}
