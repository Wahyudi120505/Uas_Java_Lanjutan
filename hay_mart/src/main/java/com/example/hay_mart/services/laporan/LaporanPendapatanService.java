package com.example.hay_mart.services.laporan;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import com.example.hay_mart.dto.laporan.LaporanPendapatanResponse;

public interface LaporanPendapatanService {
    void generateLaporanHarian();

    void generateLaporanMingguan();

    void generateLaporanBulanan();

    List<LaporanPendapatanResponse> getLaporanPendapatan();

    ByteArrayInputStream generateExcel(List<LaporanPendapatanResponse> data) throws IOException;
}
