package com.example.hay_mart.controllers.laporan;

import java.io.ByteArrayInputStream;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.hay_mart.dto.GenericResponse;
import com.example.hay_mart.dto.laporan.LaporanPendapatanResponse;
import com.example.hay_mart.services.laporan.LaporanPendapatanService;
import jakarta.servlet.http.HttpServletResponse;
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

    @GetMapping("/laporan-pendapatan/excel")
    public ResponseEntity<Object> generateLaporanPendapatanExcel(
            @RequestParam(defaultValue = "harian") String jenis,
            HttpServletResponse response) {

        try {
            switch (jenis.toLowerCase()) {
                case "mingguan":
                    laporanPendapatanService.generateLaporanMingguan();
                    break;
                case "bulanan":
                    laporanPendapatanService.generateLaporanBulanan();
                    break;
                default:
                    laporanPendapatanService.generateLaporanHarian();
            }

            List<LaporanPendapatanResponse> data = laporanPendapatanService.getLaporanPendapatan();
            ByteArrayInputStream excelStream = laporanPendapatanService.generateExcel(data);
            InputStreamResource fileResource = new InputStreamResource(excelStream);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=laporan_pendapatan.xlsx")
                    .contentType(MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(fileResource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(GenericResponse.builder()
                            .success(false)
                            .message("Internal Server Error")
                            .data(null)
                            .build());
        }
    }
}
