package com.example.hay_mart.controllers.kasir;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.example.hay_mart.dto.GenericResponse;
import com.example.hay_mart.dto.PageResponse;
import com.example.hay_mart.dto.kasir.EditKasirRequest;
import com.example.hay_mart.dto.kasir.KasirResponse;
import com.example.hay_mart.dto.kasir.KasirUpdateSatatusRequest;
import com.example.hay_mart.dto.pemesanan.PemesananResponse;
import com.example.hay_mart.services.kasir.KasirService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/kasir")
@Slf4j
public class KasirController {

    @Autowired
    private KasirService kasirService;

    @GetMapping("/get-all-kasir")
    public ResponseEntity<Object> getAllKasir(
            @RequestParam(required = false) String nama,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {
        try {
            PageResponse<KasirResponse> response = kasirService.getAllKasir(nama, page, 5, sortBy, sortOrder);
            return ResponseEntity.ok(GenericResponse.success(response, "Berhasil mengambil semua data kasir."));
        } catch (Exception e) {
            log.error("Gagal mengambil data kasir: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(GenericResponse.error("Terjadi kesalahan internal."));
        }
    }

    @PostMapping("/update-status/{id}")
    public ResponseEntity<Object> updateStatusKasir(
            @PathVariable("id") int id,
            @RequestBody KasirUpdateSatatusRequest request) {
        try {
            kasirService.update(id, request);
            return ResponseEntity.ok(GenericResponse.success(null, "Berhasil memperbarui status kasir."));
        } catch (ResponseStatusException e) {
            log.warn("Gagal update status kasir: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(GenericResponse.error(e.getReason()));
        } catch (Exception e) {
            log.error("Kesalahan internal saat update status kasir: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(GenericResponse.error("Terjadi kesalahan internal."));
        }
    }

    @GetMapping("/history-all-kasir")
    public ResponseEntity<GenericResponse<List<PemesananResponse>>> getHistoryKasir() {
        try {
            List<PemesananResponse> data = kasirService.getAllHistorysKasir();
            return ResponseEntity.ok(GenericResponse.success(data, "Berhasil mengambil histori kasir."));
        } catch (Exception e) {
            log.error("Gagal mengambil histori kasir: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(GenericResponse.error("Terjadi kesalahan internal."));
        }
    }

    @PutMapping(value = "/edit-kasir/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Object> editKasir(
            @PathVariable int id,
            EditKasirRequest request,
            @RequestParam("image Kasir") MultipartFile image) {
        try {
            log.info("Request edit kasir: {}", request);
            log.info("Image: {}", image != null ? image.getOriginalFilename() : "tidak ada");
            kasirService.editKasir(id, request, image);
            return ResponseEntity.ok(GenericResponse.success(null, "Silakan periksa email Anda untuk verifikasi."));
        } catch (ResponseStatusException e) {
            log.warn("Gagal edit kasir: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(GenericResponse.error(e.getReason()));
        } catch (Exception e) {
            log.error("Kesalahan internal saat edit kasir: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(GenericResponse.error(e.getMessage()));
        }
    }
}
