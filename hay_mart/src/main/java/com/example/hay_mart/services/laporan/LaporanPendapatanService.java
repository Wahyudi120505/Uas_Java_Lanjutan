package com.example.hay_mart.services.laporan;

import com.example.hay_mart.dto.laporan.LaporanPendapatanResponse;
import com.example.hay_mart.enums.TipeLaporan;
import java.util.List;

public interface LaporanPendapatanService {
    void generateLaporanHarian();
    void generateLaporanMingguan();
    void generateLaporanBulanan();
    List<LaporanPendapatanResponse> getLaporanPendapatan(TipeLaporan tipe);
}
