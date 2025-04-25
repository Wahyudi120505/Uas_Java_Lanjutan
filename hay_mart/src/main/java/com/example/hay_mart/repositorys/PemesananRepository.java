package com.example.hay_mart.repositorys;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.hay_mart.models.Pemesanan;
import com.example.hay_mart.models.User;

public interface PemesananRepository extends JpaRepository<Pemesanan, Integer> {
        List<Pemesanan> findByUserKasir(User userKasir);

        Optional<Pemesanan> findById(int id);

        @Query("SELECT p FROM Pemesanan p WHERE p.tanggalPembelian BETWEEN :startDate AND :endDate")
        List<Pemesanan> findByTanggalPembelianBetween(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT p FROM Pemesanan p WHERE p.userKasir = :user AND p.tanggalPembelian BETWEEN :start AND :end")
        List<Pemesanan> bulanan(@Param("user") User user, @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        @Query("select p from Pemesanan p where p.userKasir = :user and Year(p.tanggalPembelian) = :year")
        List<Pemesanan> tahunan(User user, String year);

        @Query("SELECT p FROM Pemesanan p WHERE p.userKasir = :user AND p.tanggalPembelian BETWEEN :start AND :end")
        List<Pemesanan> findByUserAndTanggalPembelianBetween(@Param("user") User user,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        @Query("select p from Pemesanan p where p.tanggalPembelian between :startDate and :endDate")
        public List<Pemesanan> pendapatan(LocalDateTime startDate, LocalDateTime endDate);

        @Query("select p from Pemesanan p where p.tanggalPembelian between :startDate and :endDate")
        public List<Pemesanan> modal(LocalDateTime startDate, LocalDateTime endDate);

}
