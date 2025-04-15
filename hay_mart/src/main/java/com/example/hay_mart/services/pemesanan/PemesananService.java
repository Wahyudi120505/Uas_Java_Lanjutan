package com.example.hay_mart.services.pemesanan;

import java.io.ByteArrayOutputStream;
import java.util.List;
import com.example.hay_mart.dto.pemesanan.PemesananRequest;
import com.example.hay_mart.dto.pemesanan.PemesananResponse;

public interface PemesananService {
    PemesananResponse buatPemesanan(PemesananRequest request);

    List<PemesananResponse> getRiwayatPemesanan();
    ByteArrayOutputStream generateStrukPdf(Integer pemesananId);
}
