package com.example.hay_mart.controllers.kasir;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.hay_mart.dto.GenericResponse;
import com.example.hay_mart.dto.PageResponse;
import com.example.hay_mart.dto.kasir.EditKasirRequest;
import com.example.hay_mart.dto.kasir.KasirResponse;
import com.example.hay_mart.dto.kasir.KasirUpdateSatatusRequest;
import com.example.hay_mart.dto.pemesanan.PemesananResponse;
import com.example.hay_mart.services.kasir.KasirService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/kasir")
@Slf4j
public class KasirController {

    @Autowired
    KasirService kasirService;

    @GetMapping("/get-all-kasir")
    public ResponseEntity<Object> getAllKasir(
            @RequestParam(required = false) String nama,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {
        try {
            PageResponse<KasirResponse> response = kasirService.getAllKasir(nama, page, 10, sortBy, sortOrder);
            return ResponseEntity.ok().body(GenericResponse.success(response, "Success Get All Kasir"));
        } catch (Exception e) {
            log.info("Error saat ambil data kasir: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(GenericResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/update-status/{id}")
    public ResponseEntity<Object> update(@PathVariable("id") int id, @RequestBody KasirUpdateSatatusRequest req) {
        try {
            kasirService.update(id, req);
            return ResponseEntity.ok().body(GenericResponse.success(null, "Succes Update status kasir"));
        } catch (ResponseStatusException e) {
            log.info(e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(GenericResponse.error(e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(GenericResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/history-all-kasir")
    public ResponseEntity<GenericResponse<List<PemesananResponse>>> getHistory() {
        List<PemesananResponse> data = kasirService.getAllHistorysKasir();
        try {
            return ResponseEntity.ok(GenericResponse.success(data, "Historys"));
        } catch (Exception e) {
            log.info(e.getMessage());
            return ResponseEntity.internalServerError().body(GenericResponse.error("Internal Server Error!"));
        }
    }

    @PutMapping(value = "/edit-kasir/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> editKasir(@PathVariable int id, EditKasirRequest request,
            @RequestParam("Image Kasir") MultipartFile image) {
        try {
            kasirService.editKasir(id, request, image);
            return ResponseEntity.ok().body(GenericResponse.success(null, "Data kasir berhasil diperbarui"));
        } catch (ResponseStatusException e) {
            log.info(e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(GenericResponse.error(e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(GenericResponse.error(e.getMessage()));
        }
    }

}
