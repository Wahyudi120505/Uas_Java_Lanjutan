package com.example.hay_mart.services.laporan;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.hay_mart.dto.laporan.LaporanPendapatanResponse;
import com.example.hay_mart.enums.TipeLaporan;
import com.example.hay_mart.models.DetailPemesanan;
import com.example.hay_mart.models.LaporanPendapatan;
import com.example.hay_mart.models.Pemesanan;
import com.example.hay_mart.repositorys.LaporanPendapatanRepository;
import com.example.hay_mart.repositorys.PemesananRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LaporanPendapatanServiceImpl implements LaporanPendapatanService {

    private final PemesananRepository pemesananRepository;
    private final LaporanPendapatanRepository laporanPendapatanRepository;
    private static final BigDecimal PERSEN_MODAL = new BigDecimal("0.85");

    private BigDecimal hitungTotalPendapatan(List<Pemesanan> pemesananList) {
        BigDecimal totalPendapatan = BigDecimal.ZERO;
        for (Pemesanan pemesanan : pemesananList) {
            for (DetailPemesanan detail : pemesanan.getDetails()) {
                BigDecimal hargaSatuan = BigDecimal.valueOf(detail.getHargaSatuan());
                BigDecimal jumlah = BigDecimal.valueOf(detail.getJumlah());
                totalPendapatan = totalPendapatan.add(hargaSatuan.multiply(jumlah));
            }
        }
        return totalPendapatan;
    }

    private void generateLaporan(LocalDate startDate, LocalDate endDate, TipeLaporan tipe) {
        laporanPendapatanRepository.deleteByTipeAndStartDateAndEndDate(tipe, startDate, endDate);
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        List<Pemesanan> pemesananList = pemesananRepository.findByTanggalPembelianBetween(startDateTime, endDateTime);
        
        BigDecimal totalPendapatan = hitungTotalPendapatan(pemesananList);
        BigDecimal totalModal = totalPendapatan.multiply(PERSEN_MODAL);
        BigDecimal totalKeuntungan = totalPendapatan.subtract(totalModal);
        
        LaporanPendapatan laporan = LaporanPendapatan.builder()
                .startDate(startDate)
                .endDate(endDate)
                .pendapatan(totalPendapatan)
                .modal(totalModal)
                .keuntungan(totalKeuntungan)
                .tipe(tipe)
                .build();
        
        laporanPendapatanRepository.save(laporan);
    }

    @Override
    @Scheduled(cron = "0 59 23 * * *")
    public void generateLaporanHarian() {
        LocalDate hariIni = LocalDate.now();
        generateLaporan(hariIni, hariIni, TipeLaporan.HARIAN);
    }

    @Override
    @Scheduled(cron = "0 50 23 * * SUN")
    public void generateLaporanMingguan() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        generateLaporan(startOfWeek, today, TipeLaporan.MINGGUAN);
    }

    @Override
    @Scheduled(cron = "0 1 0 1 * *")
    public void generateLaporanBulanan() {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        generateLaporan(startOfMonth, endOfMonth, TipeLaporan.BULANAN);
    }

    @Override
    public List<LaporanPendapatanResponse> getLaporanPendapatan(TipeLaporan tipe) {
        List<LaporanPendapatan> laporanList = laporanPendapatanRepository.findByTipe(tipe);
        return laporanList.stream()
                .map(laporan -> LaporanPendapatanResponse.builder()
                        .startDate(laporan.getStartDate())
                        .endDate(laporan.getEndDate())
                        .modal(laporan.getModal())
                        .pendapatan(laporan.getPendapatan())
                        .keuntungan(laporan.getKeuntungan())
                        .build())
                .collect(Collectors.toList());
    }
}
