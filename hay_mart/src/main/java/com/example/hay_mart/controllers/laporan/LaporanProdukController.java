package com.example.hay_mart.controllers.laporan;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.hay_mart.dto.GenericResponse;
import com.example.hay_mart.dto.laporan.LaporanProdukResponse;
import com.example.hay_mart.services.laporan.LaporanProdukService;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/laporan")
@Slf4j
public class LaporanProdukController {

    @Autowired
    private LaporanProdukService laporanProdukService;

    @GetMapping("laporan-produk")
    public ResponseEntity<GenericResponse<List<LaporanProdukResponse>>> getLaporanProduk() {
        List<LaporanProdukResponse> data = laporanProdukService.getLaporanProduk();
        try {
            return ResponseEntity.ok().body(GenericResponse.success(data, "ALL LAPORAN PRODUK SUKSES"));
        } catch (Exception e) {
            log.info(e.getMessage());
            return ResponseEntity.internalServerError().body(GenericResponse.error("Internal Server Error!"));
        }
    }
}