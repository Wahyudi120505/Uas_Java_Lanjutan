package com.example.hay_mart.services.produk;

import org.springframework.web.multipart.MultipartFile;
import com.example.hay_mart.dto.PageResponse;
import com.example.hay_mart.dto.produk.ProdukRequest;
import com.example.hay_mart.dto.produk.ProdukResponse;

public interface ProdukService {
    PageResponse<ProdukResponse> getAllProduks(String nama, String kategori, int page, int size, String sortBy,
            String sortOrder, Integer minPrice, Integer maxPrice);

    Integer getProduksPage();

    void create(ProdukRequest pRequest, MultipartFile image);

    void update(int id, ProdukRequest uproduk, MultipartFile image);

    void softDeleteProduk(int id);
}
