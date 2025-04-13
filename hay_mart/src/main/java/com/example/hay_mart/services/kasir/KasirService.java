package com.example.hay_mart.services.kasir;

import com.example.hay_mart.dto.PageResponse;
import com.example.hay_mart.dto.kasir.KasirRequest;
import com.example.hay_mart.dto.kasir.KasirResponse;

public interface KasirService {
    PageResponse<KasirResponse> getAllKasir(String nama, int page, int size, String sortBy, String sortOrder);
    void update (int id, KasirRequest req);
}
