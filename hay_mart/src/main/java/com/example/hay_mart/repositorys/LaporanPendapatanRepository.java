package com.example.hay_mart.repositorys;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.example.hay_mart.models.LaporanPendapatan;

import jakarta.transaction.Transactional;

public interface LaporanPendapatanRepository extends JpaRepository<LaporanPendapatan, Integer> {
    Optional<LaporanPendapatan> findByPendapatan(BigDecimal pendapatan);

    @Modifying
    @Transactional
    void deleteByTanggalBetween(LocalDate startDate, LocalDate endDate);

    void deleteByTanggal(LocalDate tanggal);
}
