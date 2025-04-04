package com.example.hay_mart.repositorys;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.hay_mart.models.Pemesanan;
import com.example.hay_mart.models.User;

public interface PemesananRepository extends JpaRepository<Pemesanan, Integer>{
    List<Pemesanan> findByUserKasir(User userKasir);
}
