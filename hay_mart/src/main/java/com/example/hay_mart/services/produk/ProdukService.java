package com.example.hay_mart.services.produk;

import com.example.hay_mart.dto.PageResponse;
import com.example.hay_mart.dto.produk.ProdukResponse;

public interface ProdukService {
    PageResponse<ProdukResponse> getAllProduks(String nama, String kategori, int page, int size, String sortBy, Integer minPrice, Integer maxPrice);
    Integer getProduksPage();
}
