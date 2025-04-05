package com.example.hay_mart.services.pemesanan;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.hay_mart.dto.pemesanan.DetailPemesananResponse;
import com.example.hay_mart.dto.pemesanan.ItemRequest;
import com.example.hay_mart.dto.pemesanan.PemesananRequest;
import com.example.hay_mart.dto.pemesanan.PemesananResponse;
import com.example.hay_mart.models.DetailPemesanan;
import com.example.hay_mart.models.LaporanProduk;
import com.example.hay_mart.models.Pemesanan;
import com.example.hay_mart.models.Produk;
import com.example.hay_mart.models.User;
import com.example.hay_mart.repositorys.LaporanProdukRepository;
import com.example.hay_mart.repositorys.PemesananRepository;
import com.example.hay_mart.repositorys.ProdukRepository;
import com.example.hay_mart.services.GetAuthorities;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PemesananServiceImpl implements PemesananService {
    @Autowired
    private PemesananRepository pemesananRepository;

    @Autowired
    private ProdukRepository produkRepository;

    @Autowired
    private LaporanProdukRepository laporanProdukRepository;

    @Autowired
    private GetAuthorities getAuthorities;

    @Override
    @Transactional
    public PemesananResponse buatPemesanan(PemesananRequest request) {
        User kasir = getAuthorities.getAuthenticatedUser();

        Pemesanan pemesanan = new Pemesanan();
        pemesanan.setUserKasir(kasir);
        pemesanan.setTanggalPembelian(LocalDateTime.now());

        List<DetailPemesanan> details = new ArrayList<>();
        List<DetailPemesananResponse> detailResponses = new ArrayList<>();

        Map<Integer, ItemRequest> combinedItems = new HashMap<>();

        for (ItemRequest item : request.getItems()) {
            if (combinedItems.containsKey(item.getProdukId())) {
                ItemRequest existingItem = combinedItems.get(item.getProdukId());
                existingItem.setJumlah(existingItem.getJumlah() + item.getJumlah());
            } else {
                combinedItems.put(item.getProdukId(), item);
            }
        }

        long totalHarga = 0;

        for (ItemRequest item : combinedItems.values()) {
            Produk produk = produkRepository.findById(item.getProdukId())
                    .orElseThrow(() -> new RuntimeException("Produk tidak ditemukan"));

            if (item.getJumlah() <= 0) {
                throw new RuntimeException("Jumlah minimal adalah 1");
            }

            Integer jumlah = item.getJumlah();

            if (produk.getStok() < jumlah) {
                throw new RuntimeException("Stok produk \"" + produk.getNama() + "\" tidak cukup");
            }

            produk.setStok(produk.getStok() - jumlah);
            produkRepository.save(produk);

            if (produk.getStok() == 0) {
                produk.setStatus("Tidak Tersedia");
                produkRepository.save(produk);
            }

            Integer hargaSatuan = produk.getHarga();
            Integer subtotal = jumlah * hargaSatuan;

            DetailPemesanan detail = DetailPemesanan.builder()
                    .produk(produk)
                    .jumlah(jumlah)
                    .hargaSatuan(hargaSatuan)
                    .subtotal(subtotal)
                    .pemesanan(pemesanan)
                    .build();

            details.add(detail);
            totalHarga += subtotal;

            detailResponses.add(DetailPemesananResponse.builder()
                    .namaProduk(produk.getNama())
                    .jumlah(jumlah)
                    .hargaSatuan(hargaSatuan)
                    .subtotal(subtotal)
                    .build());

            LaporanProduk laporan = laporanProdukRepository.findByProduk(produk).orElse(null);

            if (laporan != null) {
                laporan.setJumlahTerjual(laporan.getJumlahTerjual() + jumlah);
                laporan.setTotal(laporan.getTotal() + subtotal);
            } else {
                laporan = LaporanProduk.builder()
                        .produk(produk)
                        .namaProduk(produk.getNama())
                        .jumlahTerjual(jumlah)
                        .hargaSatuan(hargaSatuan)
                        .total(subtotal)
                        .build();
            }
            laporanProdukRepository.save(laporan);
        }

        pemesanan.setDetails(details);
        pemesanan.setTotalHarga(totalHarga);
        Pemesanan saved = pemesananRepository.save(pemesanan);

        return PemesananResponse.builder()
                .namaKasir(kasir.getNama())
                .tanggalPembelian(saved.getTanggalPembelian())
                .totalHarga(totalHarga)
                .items(detailResponses)
                .build();
    }

    @Override
    public List<PemesananResponse> getRiwayatPemesanan() {
        User kasir = getAuthorities.getAuthenticatedUser();

        List<Pemesanan> riwayat = pemesananRepository.findByUserKasir(kasir);
        List<PemesananResponse> responseList = new ArrayList<>();

        for (Pemesanan pemesanan : riwayat) {
            List<DetailPemesananResponse> detailList = new ArrayList<>();

            for (DetailPemesanan detail : pemesanan.getDetails()) {
                detailList.add(DetailPemesananResponse.builder()
                        .namaProduk(detail.getProduk().getNama())
                        .jumlah(detail.getJumlah())
                        .hargaSatuan(detail.getHargaSatuan())
                        .subtotal(detail.getSubtotal())
                        .build());
            }

            responseList.add(PemesananResponse.builder()
                    .namaKasir(kasir.getNama())
                    .tanggalPembelian(pemesanan.getTanggalPembelian())
                    .totalHarga(pemesanan.getTotalHarga())
                    .items(detailList)
                    .build());
        }

        return responseList;
    }

    @Override
    @Transactional
    public ByteArrayOutputStream generateStrukPdf(Integer pemesananId) {
        Pemesanan pemesanan = pemesananRepository.findById(pemesananId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pemesanan tidak ditemukan"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PDDocument document = new PDDocument()) {
            PDFont boldFont = PDType1Font.HELVETICA_BOLD;
            PDFont regularFont = PDType1Font.HELVETICA;

            float margin = 50;
            float yStart = PDRectangle.A4.getHeight() - margin;
            float pageWidth = PDRectangle.A4.getWidth() - 2 * margin;
            float yPosition = yStart;

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            try {
                // Title
                contentStream.beginText();
                contentStream.setFont(boldFont, 16);
                float titleWidth = boldFont.getStringWidth("HAY MART") / 1000 * 16;
                contentStream.newLineAtOffset((pageWidth / 2) + margin - (titleWidth / 2), yPosition);
                contentStream.showText("HAY MART");
                contentStream.endText();
                yPosition -= 20;

                // Address
                contentStream.beginText();
                contentStream.setFont(regularFont, 10);
                String address = "Jl. Pasteur No. 123, Bandung";
                float addressWidth = regularFont.getStringWidth(address) / 1000 * 10;
                contentStream.newLineAtOffset((pageWidth / 2) + margin - (addressWidth / 2), yPosition);
                contentStream.showText(address);
                contentStream.endText();
                yPosition -= 30;

                // Transaction Info
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                String formattedDate = pemesanan.getTanggalPembelian().format(formatter);

                contentStream.beginText();
                contentStream.setFont(regularFont, 10);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("No. Transaksi: " + pemesananId);
                contentStream.endText();
                yPosition -= 15;

                contentStream.beginText();
                contentStream.setFont(regularFont, 10);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Tanggal: " + formattedDate);
                contentStream.endText();
                yPosition -= 15;

                contentStream.beginText();
                contentStream.setFont(regularFont, 10);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Kasir: " + pemesanan.getUserKasir().getNama());
                contentStream.endText();
                yPosition -= 25;

                // Separator
                contentStream.setLineWidth(1f);
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(pageWidth + margin, yPosition);
                contentStream.stroke();
                yPosition -= 15;

                // Table header
                contentStream.beginText();
                contentStream.setFont(boldFont, 10);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Produk");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(boldFont, 10);
                contentStream.newLineAtOffset(margin + (pageWidth * 0.5f), yPosition);
                contentStream.showText("Qty");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(boldFont, 10);
                contentStream.newLineAtOffset(margin + (pageWidth * 0.65f), yPosition);
                contentStream.showText("Harga");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(boldFont, 10);
                contentStream.newLineAtOffset(margin + (pageWidth * 0.85f), yPosition);
                contentStream.showText("Subtotal");
                contentStream.endText();

                yPosition -= 15;

                // Items loop
                for (DetailPemesanan detail : pemesanan.getDetails()) {
                    // Ganti halaman jika penuh
                    if (yPosition < 100) {
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        yPosition = yStart;
                    }

                    contentStream.beginText();
                    contentStream.setFont(regularFont, 10);
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(detail.getProduk().getNama());
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.setFont(regularFont, 10);
                    contentStream.newLineAtOffset(margin + (pageWidth * 0.5f), yPosition);
                    contentStream.showText(detail.getJumlah().toString());
                    contentStream.endText();

                    String price = "Rp " + String.format("%,d", detail.getHargaSatuan()).replace(",", ".");
                    float priceWidth = regularFont.getStringWidth(price) / 1000 * 10;
                    contentStream.beginText();
                    contentStream.setFont(regularFont, 10);
                    contentStream.newLineAtOffset(margin + (pageWidth * 0.75f) - priceWidth, yPosition);
                    contentStream.showText(price);
                    contentStream.endText();

                    String subtotal = "Rp " + String.format("%,d", detail.getSubtotal()).replace(",", ".");
                    ;
                    float subtotalWidth = regularFont.getStringWidth(subtotal) / 1000 * 10;
                    contentStream.beginText();
                    contentStream.setFont(regularFont, 10);
                    contentStream.newLineAtOffset(margin + pageWidth - subtotalWidth, yPosition);
                    contentStream.showText(subtotal);
                    contentStream.endText();

                    yPosition -= 15;
                }

                // Separator
                contentStream.setLineWidth(1f);
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(pageWidth + margin, yPosition);
                contentStream.stroke();
                yPosition -= 20;

                // Total
                String total = "TOTAL: Rp " + String.format("%,d", pemesanan.getTotalHarga()).replace(",", ".");
                float totalWidth = boldFont.getStringWidth(total) / 1000 * 12;
                contentStream.beginText();
                contentStream.setFont(boldFont, 12);
                contentStream.newLineAtOffset(margin + pageWidth - totalWidth, yPosition);
                contentStream.showText(total);
                contentStream.endText();
                yPosition -= 30;

                // Footer
                String footer = "Terima kasih atas kunjungan Anda!";
                float footerWidth = boldFont.getStringWidth(footer) / 1000 * 10;
                contentStream.beginText();
                contentStream.setFont(boldFont, 10);
                contentStream.newLineAtOffset((pageWidth / 2) + margin - (footerWidth / 2), yPosition);
                contentStream.showText(footer);
                contentStream.endText();

                contentStream.close();
            } catch (IOException e) {
                log.error("Error saat menulis PDF: ", e);
            }

            document.save(baos);
        } catch (IOException e) {
            log.error("Error generating PDF: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error generating PDF: " + e.getMessage());
        }

        return baos;
    }
}