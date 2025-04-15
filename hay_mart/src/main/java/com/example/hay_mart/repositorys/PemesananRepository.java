package com.example.hay_mart.repositorys;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.hay_mart.models.Pemesanan;
import com.example.hay_mart.models.User;

public interface PemesananRepository extends JpaRepository<Pemesanan, Integer> {
  List<Pemesanan> findByUserKasir(User userKasir);

  Optional<Pemesanan> findById(int id);

  List<Pemesanan> findByTanggalPembelianBetween(LocalDateTime start, LocalDateTime end);
}
