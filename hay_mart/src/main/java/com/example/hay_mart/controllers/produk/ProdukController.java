package com.example.hay_mart.controllers.produk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.hay_mart.dto.GenericResponse;
import com.example.hay_mart.dto.PageResponse;
import com.example.hay_mart.dto.produk.ProdukRequest;
import com.example.hay_mart.dto.produk.ProdukResponse;
import com.example.hay_mart.services.produk.ProdukService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/produk")
@RestController
@Slf4j
public class ProdukController {

    @Autowired
    ProdukService produkService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> create(ProdukRequest prequest,
            @RequestParam("Product Image") MultipartFile file) {
        try {
            produkService.create(prequest, file);
            return ResponseEntity.ok().body(GenericResponse.success(null, "Success Add New Product"));
        } catch (ResponseStatusException e) {
            log.info(e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(GenericResponse.error(e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(GenericResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/get-produk-page")
    public ResponseEntity<Object> getProdukPagEntity() {
        try {
            return ResponseEntity.ok()
                    .body(GenericResponse.success(produkService.getProduksPage(), "Jumlah halaman produk saat ini"));
        } catch (Exception e) {
            log.error("Error saat mengambil halaman produk: " + e.getMessage());
            return ResponseEntity.internalServerError().body(GenericResponse.error("Gagal mengambil halaman produk"));
        }
    }

    @GetMapping("/get-all-produks")
    public ResponseEntity<Object> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String nama,
            @RequestParam(required = false) String kategori,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice) {
        try {
            PageResponse<ProdukResponse> response = produkService.getAllProduks(
                    nama, kategori, page, 3, sortBy, sortOrder, minPrice, maxPrice);

            return ResponseEntity.ok().body(GenericResponse.success(response, "Berhasil mengambil daftar produk"));
        } catch (Exception e) {
            log.error("Error saat mengambil semua produk: " + e.getMessage());
            return ResponseEntity.internalServerError().body(GenericResponse.error("Gagal mengambil data produk"));
        }
    }

    @PostMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Object> update(
            @PathVariable int id,
            ProdukRequest uproduk,
            @RequestParam("Product Image") MultipartFile file) {
        try {
            // if (file.isEmpty()) {
            //     throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,
            //             "Gambar produk tidak boleh kosong");
            // }
            produkService.update(id, uproduk, file);
            return ResponseEntity.ok().body(GenericResponse.success(null, "Produk berhasil diperbarui"));
        } catch (ResponseStatusException e) {
            log.info(e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(GenericResponse.error(e.getReason()));
        } catch (Exception e) {
            log.error("Error saat mengupdate produk: " + e.getMessage());
            return ResponseEntity.internalServerError().body(GenericResponse.error("Gagal memperbarui produk"));
        }
    }

    @DeleteMapping("/delete-produk/{id}")
    public ResponseEntity<Object> deleteProduk(@PathVariable Integer id) {
        try {
            produkService.softDeleteProduk(id);
            return ResponseEntity.ok().body(GenericResponse.success(null, "Produk berhasil dihapus"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Gagal menghapus produk: " + e.getMessage());
        }
    }

}
