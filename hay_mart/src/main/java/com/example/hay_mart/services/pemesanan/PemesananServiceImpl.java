package com.example.hay_mart.services.pemesanan;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

@Service
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

        long totalHarga = 0;

        for (ItemRequest item : request.getItems()) {
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
}
