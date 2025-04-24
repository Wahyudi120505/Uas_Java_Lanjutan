package com.example.hay_mart.services.laporan;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import com.example.hay_mart.dto.laporan.LaporanPendapatanResponse;

public interface LaporanPendapatanService {
    List<LaporanPendapatanResponse> generateLaporanHarian();

    List<LaporanPendapatanResponse> generateLaporanMingguan();

    List<LaporanPendapatanResponse> generateLaporanBulanan();

    List<LaporanPendapatanResponse> generateLaporanTahunan();

    List<LaporanPendapatanResponse> laporanPendapatan(LocalDate startDate, LocalDate endDate);

    ByteArrayInputStream generateExcel(List<LaporanPendapatanResponse> data) throws IOException;
}
