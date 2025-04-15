package com.example.hay_mart.services.laporan;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import com.example.hay_mart.dto.laporan.LaporanPendapatanResponse;
import com.example.hay_mart.models.DetailPemesanan;
import com.example.hay_mart.models.LaporanPendapatan;
import com.example.hay_mart.models.Pemesanan;
import com.example.hay_mart.repositorys.LaporanPendapatanRepository;
import com.example.hay_mart.repositorys.PemesananRepository;
import lombok.RequiredArgsConstructor;

@Service
@Component
@RequiredArgsConstructor
public class LaporanPendapatanServiceImpl implements LaporanPendapatanService {

    @Autowired
    private PemesananRepository pemesananRepository;

    @Autowired
    private LaporanPendapatanRepository laporanPendapatanRepository;

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

    @Override
    @Scheduled(cron = "0 59 23 * * *")
    public void generateLaporanHarian() {
        LocalDate hariIni = LocalDate.now();
        LocalDateTime start = hariIni.atStartOfDay();
        LocalDateTime end = hariIni.atTime(LocalTime.MAX);

        laporanPendapatanRepository.deleteByTanggal(hariIni);
        List<Pemesanan> pemesananList = pemesananRepository.findByTanggalPembelianBetween(start, end);

        BigDecimal totalPendapatan = hitungTotalPendapatan(pemesananList);
        BigDecimal totalModal = totalPendapatan.multiply(PERSEN_MODAL);
        BigDecimal totalKeuntungan = totalPendapatan.subtract(totalModal);

        LaporanPendapatan laporan = new LaporanPendapatan();
        laporan.setTanggal(hariIni);
        laporan.setPendapatan(totalPendapatan);
        laporan.setModal(totalModal);
        laporan.setKeuntungan(totalKeuntungan);
        laporanPendapatanRepository.save(laporan);
    }

    @Override
    @Scheduled(cron = "0 50 23 * * SUN")
    public void generateLaporanMingguan() {
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.with(DayOfWeek.MONDAY);
        LocalDate endDate = now;

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        laporanPendapatanRepository.deleteByTanggalBetween(startDate, endDate);
        List<Pemesanan> pemesananList = pemesananRepository.findByTanggalPembelianBetween(start, end);

        BigDecimal totalPendapatan = hitungTotalPendapatan(pemesananList);
        BigDecimal totalModal = totalPendapatan.multiply(PERSEN_MODAL);
        BigDecimal totalKeuntungan = totalPendapatan.subtract(totalModal);

        LaporanPendapatan laporan = new LaporanPendapatan();
        laporan.setTanggal(startDate);
        laporan.setPendapatan(totalPendapatan);
        laporan.setModal(totalModal);
        laporan.setKeuntungan(totalKeuntungan);

        laporanPendapatanRepository.save(laporan);
    }

    @Override
    @Scheduled(cron = "0 1 0 1 * *")
    public void generateLaporanBulanan() {
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        laporanPendapatanRepository.deleteByTanggalBetween(startDate, endDate);
        List<Pemesanan> pemesananList = pemesananRepository.findByTanggalPembelianBetween(start, end);

        BigDecimal totalPendapatan = hitungTotalPendapatan(pemesananList);
        BigDecimal totalModal = totalPendapatan.multiply(PERSEN_MODAL);
        BigDecimal totalKeuntungan = totalPendapatan.subtract(totalModal);

        LaporanPendapatan laporan = new LaporanPendapatan();
        laporan.setTanggal(startDate);
        laporan.setTanggal(endDate);
        laporan.setPendapatan(totalPendapatan);
        laporan.setModal(totalModal);
        laporan.setKeuntungan(totalKeuntungan);
        laporanPendapatanRepository.save(laporan);
    }

    @Override
    public List<LaporanPendapatanResponse> getLaporanPendapatan() {
        return laporanPendapatanRepository.findAll().stream()
                .map(laporan -> LaporanPendapatanResponse.builder()
                        .startDate(laporan.getTanggal())
                        .pendapatan(laporan.getPendapatan())
                        .modal(laporan.getModal())
                        .keuntungan(laporan.getKeuntungan())
                        .build())
                .collect(Collectors.toList());
    }
}
