package com.example.hay_mart.repositorys;

import com.example.hay_mart.models.LaporanPendapatan;
import com.example.hay_mart.enums.TipeLaporan;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import java.time.LocalDate;
import java.util.List;

public interface LaporanPendapatanRepository extends JpaRepository<LaporanPendapatan, Integer> {    
    // @Modifying
    // @Transactional
    // void deleteByTanggalBetween(LocalDate startDate, LocalDate endDate);

    @Modifying
    @Transactional
    void deleteByTanggalAndTipe(LocalDate tanggal, TipeLaporan tipe);

    // void deleteByTanggal(LocalDate tanggal);

    // List<LaporanPendapatan> findByTanggalBetween(LocalDate start, LocalDate end);
    
    List<LaporanPendapatan> findByTipe(TipeLaporan tipe);
}