package com.example.hay_mart.services.kasir;

import java.util.List;

import com.example.hay_mart.dto.PageResponse;
import com.example.hay_mart.dto.kasir.KasirResponse;
import com.example.hay_mart.dto.kasir.KasirUpdateSatatusRequest;
import com.example.hay_mart.dto.pemesanan.PemesananResponse;

public interface KasirService {
    PageResponse<KasirResponse> getAllKasir(String nama, int page, int size, String sortBy, String sortOrder);
    void update (int id, KasirUpdateSatatusRequest req);
    List<PemesananResponse> getAllHistorysKasir();
}
