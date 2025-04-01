package com.example.hay_mart.services.produk;

import com.example.hay_mart.dto.PageResponse;
import com.example.hay_mart.dto.produk.ProdukRequest;
import com.example.hay_mart.dto.produk.ProdukResponse;
import com.example.hay_mart.dto.produk.UpdateProduk;

public interface ProdukService {
    PageResponse<ProdukResponse> getAllProduks(String nama, String kategori, int page, int size, String sortBy, Integer minPrice, Integer maxPrice);
    Integer getProduksPage();
    void create(ProdukRequest pRequest);
    void update(int id, UpdateProduk uproduk);
}
