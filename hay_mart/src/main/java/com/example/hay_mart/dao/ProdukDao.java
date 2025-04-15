package com.example.hay_mart.dao;

import com.example.hay_mart.dto.PageResponse;
import com.example.hay_mart.models.Kategori;
import com.example.hay_mart.models.Produk;

public interface ProdukDao {
     PageResponse<Produk> getAll(String nama, Kategori kategori, int page, int size, String sortBy, String sortOrder,
               Integer minPrice, Integer maxPrice);
}
