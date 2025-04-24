package com.example.hay_mart.controllers.laporan;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.hay_mart.dto.laporan.LaporanPendapatanResponse;
import com.example.hay_mart.services.laporan.LaporanPendapatanService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/laporan")
@RequiredArgsConstructor
@Slf4j
public class LaporanPendapatanController {

    private LaporanPendapatanService laporanPendapatanService;

    @Autowired
    LaporanPendapatanController(LaporanPendapatanService laporanPendapatanService) {
        this.laporanPendapatanService = laporanPendapatanService;
    }

    @GetMapping("/pendapatan-harian")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Object> downloadHarian() {
        try {
            List<LaporanPendapatanResponse> data = laporanPendapatanService.generateLaporanHarian();
            if (data == null || data.isEmpty()) {
                return ResponseEntity.badRequest().body("Data laporan harian tidak tersedia.");
            }

            ByteArrayInputStream stream = laporanPendapatanService.generateExcel(data);
            InputStreamResource file = new InputStreamResource(stream);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=laporan_harian.xlsx")
                    .contentType(MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(file);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Gagal mengunduh laporan harian.");
        }
    }

    @GetMapping("/pendapatan-mingguan")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Object> downloadMingguan() {
        try {
            List<LaporanPendapatanResponse> laporanList = laporanPendapatanService.generateLaporanMingguan();
            if (laporanList.isEmpty() || laporanList.stream().allMatch(laporan -> laporan.getPendapatan() == null)) {
                return ResponseEntity.badRequest().body("Data laporan mingguan tidak tersedia.");
            }

            ByteArrayInputStream stream = laporanPendapatanService.generateExcel(laporanList);
            InputStreamResource file = new InputStreamResource(stream);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=laporan_mingguan.xlsx")
                    .contentType(MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(file);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Gagal mengunduh laporan mingguan.");
        }
    }

    @GetMapping("/pendapatan-bulanan")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Object> downloadBulanan() {
        try {
            List<LaporanPendapatanResponse> data = laporanPendapatanService.generateLaporanBulanan();
            if (data.isEmpty()) {
                return ResponseEntity.badRequest().body("Data laporan bulanan tidak tersedia.");
            }

            ByteArrayInputStream stream = laporanPendapatanService.generateExcel(data);
            InputStreamResource file = new InputStreamResource(stream);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=laporan_bulanan.xlsx")
                    .contentType(MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(file);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Gagal mengunduh laporan bulanan.");
        }
    }

    @GetMapping("/pendapatan-tahunan")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Object> downloadTahunan() {
        try {
            List<LaporanPendapatanResponse> dataList = laporanPendapatanService.generateLaporanTahunan();
            if (dataList.isEmpty()) {
                return ResponseEntity.badRequest().body("Data laporan tahunan tidak tersedia.");
            }

            ByteArrayInputStream stream = laporanPendapatanService.generateExcel(dataList);
            InputStreamResource file = new InputStreamResource(stream);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=laporan_tahunan.xlsx")
                    .contentType(MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(file);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Gagal mengunduh laporan tahunan.");
        }
    }

    @GetMapping("/pendapatan-permintaan")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Object> laporanPendapatan(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<LaporanPendapatanResponse> data = laporanPendapatanService.laporanPendapatan(startDate, endDate);

            if (data.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Tidak ada data untuk periode ini.");
            }

            ByteArrayInputStream stream = laporanPendapatanService.generateExcel(data);
            InputStreamResource file = new InputStreamResource(stream);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=laporan_pendapatan.xlsx")
                    .contentType(MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(file);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Gagal mengunduh laporan.");
        }
    }
}