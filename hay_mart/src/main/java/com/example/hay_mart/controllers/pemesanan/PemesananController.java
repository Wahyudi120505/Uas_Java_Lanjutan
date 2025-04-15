package com.example.hay_mart.controllers.pemesanan;

import java.io.ByteArrayOutputStream;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.example.hay_mart.dto.GenericResponse;
import com.example.hay_mart.dto.pemesanan.PemesananRequest;
import com.example.hay_mart.dto.pemesanan.PemesananResponse;
import com.example.hay_mart.services.pemesanan.PemesananService;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/pemesanan")
@Slf4j
public class PemesananController {
    @Autowired
    private PemesananService pemesananService;

    @PostMapping("create-pemesanan")
    public ResponseEntity<Object> buatPemesanan(@RequestBody PemesananRequest request) {
        try {
            return ResponseEntity.ok()
                    .body(GenericResponse.success(pemesananService.buatPemesanan(request), "Success"));
        } catch (ResponseStatusException e) {
            log.info(e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(GenericResponse.error(e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(GenericResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<GenericResponse<List<PemesananResponse>>> getHistory() {
        List<PemesananResponse> data = pemesananService.getRiwayatPemesanan();
        try {
            return ResponseEntity.ok(GenericResponse.success(data, "Historys"));
        } catch (Exception e) {
            log.info(e.getMessage());
            return ResponseEntity.internalServerError().body(GenericResponse.error("Internal Server Error!"));
        }
    }

    @GetMapping("/struk/{pemesananId}")
    public ResponseEntity<byte[]> downloadStruk(@PathVariable Integer pemesananId) {
        try {
            ByteArrayOutputStream pdfStream = pemesananService.generateStrukPdf(pemesananId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);

            String filename = "struk-" + pemesananId + ".pdf";
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(pdfStream.toByteArray());

        } catch (Exception e) {
            log.error("Error generating struk PDF", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
