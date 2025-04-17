package com.example.hay_mart.repositorys;

import com.example.hay_mart.models.LaporanPendapatan;
import jakarta.transaction.Transactional;
import com.example.hay_mart.enums.TipeLaporan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import java.time.LocalDate;
import java.util.List;

public interface LaporanPendapatanRepository extends JpaRepository<LaporanPendapatan, Integer> {
    List<LaporanPendapatan> findByTipe(TipeLaporan tipe);
    
    @Modifying
    @Transactional
    void deleteByTanggalBetween(LocalDate startDate, LocalDate endDate);

    void deleteByTanggal(LocalDate tanggal);

    List<LaporanPendapatan> findByTanggalBetween(LocalDate start, LocalDate end);

}
