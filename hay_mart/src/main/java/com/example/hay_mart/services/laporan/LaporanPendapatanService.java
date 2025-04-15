package com.example.hay_mart.services.laporan;

import java.util.List;
import com.example.hay_mart.dto.laporan.LaporanPendapatanResponse;

public interface LaporanPendapatanService {
    void generateLaporanHarian();

    void generateLaporanMingguan();

    void generateLaporanBulanan();

    List<LaporanPendapatanResponse> getLaporanPendapatan();
}
