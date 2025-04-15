package com.example.hay_mart.services.laporan;

import java.util.List;
import com.example.hay_mart.dto.laporan.LaporanProdukResponse;

public interface LaporanProdukService {
    List<LaporanProdukResponse> getLaporanProduk();
}