package com.example.hay_mart.controllers.produk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.hay_mart.dto.PageResponse;
import com.example.hay_mart.dto.produk.ProdukResponse;
import com.example.hay_mart.services.produk.ProdukService;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("/produk")
@RestController
@Slf4j
public class ProdukController {
    @Autowired
    ProdukService produkService;

    @GetMapping("/get-produk-page")
    public ResponseEntity<Object> getProdukPagEntity() {
        try {
            return ResponseEntity.ok().body(produkService.getProduksPage());
        } catch (Exception e) {
            log.info(e.getMessage());
            return null;
        }
    }

    @GetMapping("/get-all-produks")
    public ResponseEntity<Object> getAll(@RequestParam int page, 
            @RequestParam(required = false) String nama,
            @RequestParam(required = false) String kategori,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice) {
        try {
            PageResponse<ProdukResponse> response = produkService.getAllProduks(nama, kategori, page, 10, sortBy,
                    minPrice, maxPrice);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            log.info(e.getMessage());
            return ResponseEntity.internalServerError().body("invalid");
        }
    }
}
