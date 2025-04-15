package com.example.hay_mart.controllers.laporan;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.hay_mart.dto.GenericResponse;
import com.example.hay_mart.dto.laporan.LaporanPendapatanResponse;
import com.example.hay_mart.enums.TipeLaporan;
import com.example.hay_mart.services.laporan.LaporanPendapatanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/laporan")
@RequiredArgsConstructor
@Slf4j
public class LaporanPendapatanController {

    private final LaporanPendapatanService laporanPendapatanService;

    @GetMapping("/laporan-pendapatan-harian")
    public ResponseEntity<GenericResponse<List<LaporanPendapatanResponse>>> getLaporanPendapatanHarian() {
        try {
            laporanPendapatanService.generateLaporanHarian();
            List<LaporanPendapatanResponse> data = laporanPendapatanService.getLaporanPendapatan(TipeLaporan.HARIAN);
            return ResponseEntity.ok().body(GenericResponse.success(data, "Berhasil ambil data harian"));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body(GenericResponse.error("Internal Server Error!"));
        }
    }

    @GetMapping("/laporan-pendapatan-mingguan")
    public ResponseEntity<GenericResponse<List<LaporanPendapatanResponse>>> getLaporanPendapatanMingguan() {
        try {
            laporanPendapatanService.generateLaporanMingguan();
            List<LaporanPendapatanResponse> data = laporanPendapatanService.getLaporanPendapatan(TipeLaporan.MINGGUAN);
            return ResponseEntity.ok().body(GenericResponse.success(data, "Berhasil ambil data mingguan"));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body(GenericResponse.error("Internal Server Error!"));
        }
    }

    @GetMapping("/laporan-pendapatan-bulanan")
    public ResponseEntity<GenericResponse<List<LaporanPendapatanResponse>>> getLaporanPendapatanBulanan() {
        try {
            laporanPendapatanService.generateLaporanBulanan();
            List<LaporanPendapatanResponse> data = laporanPendapatanService.getLaporanPendapatan(TipeLaporan.BULANAN);
            return ResponseEntity.ok().body(GenericResponse.success(data, "Berhasil ambil data bulanan"));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body(GenericResponse.error("Internal Server Error!"));
        }
    }
}
