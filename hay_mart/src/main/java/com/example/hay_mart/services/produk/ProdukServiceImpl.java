package com.example.hay_mart.services.produk;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.rowset.serial.SerialBlob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.example.hay_mart.dao.ProdukDao;
import com.example.hay_mart.dto.PageResponse;
import com.example.hay_mart.dto.produk.ProdukRequest;
import com.example.hay_mart.dto.produk.ProdukResponse;
import com.example.hay_mart.models.Kategori;
import com.example.hay_mart.models.LaporanProduk;
import com.example.hay_mart.models.Produk;
import com.example.hay_mart.repositorys.KategoriRepository;
import com.example.hay_mart.repositorys.LaporanProdukRepository;
import com.example.hay_mart.repositorys.ProdukRepository;
import com.example.hay_mart.services.image.ConvertImageService;

@Service
@Slf4j
public class ProdukServiceImpl implements ProdukService {

    @Autowired
    ProdukDao produkDao;

    @Autowired
    ProdukRepository produkRepository;

    @Autowired
    KategoriRepository kategoriRepository;

    @Autowired
    LaporanProdukRepository laporanProdukRepository;

    @Autowired
    ConvertImageService convertImage;

    @Override
    public Integer getProduksPage() {
        return (int) Math.ceil((double) produkRepository.count() / 10);
    }

    @Override
    public void create(ProdukRequest request, MultipartFile image) {
        try {
            System.out.println("PRODUK : " + request.getNama());
            Produk produk = toProduk(request, image);
            produkRepository.save(produk);

            LaporanProduk laporan = LaporanProduk.builder()
                    .produk(produk)
                    .namaProduk(produk.getNama())
                    .jumlahTerjual(0)
                    .hargaSatuan(produk.getHarga())
                    .total(0)
                    .build();
            laporanProdukRepository.save(laporan);
        } catch (Exception e) {
            log.error("Error creating product: {}", e.getMessage());
            throw new RuntimeException("Gagal menyimpan produk: " + e.getMessage());
        }
    }

    @Override
    public PageResponse<ProdukResponse> getAllProduks(String nama, String kategori, int page, int size, String sortBy,
            String sortOrder,
            Integer minPrice, Integer maxPrice) {
        Kategori namaKategori = kategoriRepository.findKategoriByNama(kategori);
        PageResponse<Produk> produkPage = produkDao.getAll(nama, namaKategori, page, size, sortBy, sortOrder, minPrice,
                maxPrice);

        List<ProdukResponse> produkResponses = produkPage.getItems().stream()
                .map(this::toProdukResponse)
                .collect(Collectors.toList());

        return PageResponse.success(produkResponses, produkPage.getPage(), produkPage.getSize(),
                produkPage.getTotalItem());
    }

    private ProdukResponse toProdukResponse(Produk produk) {
        try {
            return ProdukResponse.builder()
                    .nama(produk.getNama())
                    .harga(produk.getHarga())
                    .stok(produk.getStok())
                    .image(convertImage.convertImage(produk.getFotoProduk()))
                    .keterangan(produk.getKeterangan())
                    .status(produk.getStatus())
                    .kategori(produk.getKategori().getNama())
                    .build();
        } catch (IOException | SQLException e) {
            log.error("Error converting product response: {}", e.getMessage());
            throw new RuntimeException("Gagal mengkonversi data produk: " + e.getMessage());
        }
    }

    private Produk toProduk(ProdukRequest request, MultipartFile image) {
        try {
            if (request.getKategori() == null || request.getKategori().isBlank()) {
                throw new RuntimeException("Kategori tidak boleh kosong! " + request.getNama());
            }

            Kategori kategori = kategoriRepository.findKategoriByNama(request.getKategori());
            if (kategori == null) {
                throw new RuntimeException("Kategori tidak ditemukan: " + request.getKategori());
            }

            if (produkRepository.existsByNamaIgnoreCase(request.getNama())) {
                throw new RuntimeException("Produk dengan nama '" + request.getNama() + "' sudah ada!");
            }

            String status = request.getStok() > 0 ? "Tersedia" : "Tidak Tersedia";
            return Produk.builder()
                    .nama(request.getNama())
                    .harga(request.getHarga())
                    .stok(request.getStok())
                    .fotoProduk(convertImage.convertBlob(image))
                    .keterangan(request.getKeterangan())
                    .status(status)
                    .kategori(kategori)
                    .build();
        } catch (IOException | SQLException e) {
            log.error("Error creating product entity: {}", e.getMessage());
            throw new RuntimeException("Gagal memproses data produk: " + e.getMessage());
        }
    }

    @Override
    public void update(int id, ProdukRequest uproduk, MultipartFile image) {
        try {
            Produk produk = produkRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Produk dengan id " + id + " tidak ditemukan"));

            if (uproduk.getKategori() == null || uproduk.getKategori().isBlank()) {
                throw new RuntimeException("Kategori tidak boleh kosong! " + uproduk.getNama());
            }
            Kategori kategori = kategoriRepository.findKategoriByNama(uproduk.getKategori());
            if (kategori == null) {
                throw new RuntimeException("Kategori tidak ditemukan: " + uproduk.getKategori());
            }

            if (produkRepository.existsByNamaIgnoreCase(uproduk.getNama()) &&
                    !produk.getNama().equalsIgnoreCase(uproduk.getNama())) {
                throw new RuntimeException("Produk dengan nama '" + uproduk.getNama() + "' sudah ada!");
            }

            String status = uproduk.getStok() > 0 ? "Tersedia" : "Tidak Tersedia";

            produk.setNama(uproduk.getNama());
            produk.setHarga(uproduk.getHarga());
            produk.setStok(uproduk.getStok());
            produk.setKeterangan(uproduk.getKeterangan());
            produk.setFotoProduk(new SerialBlob(image.getBytes()));
            produk.setStatus(status);
            produk.setKategori(kategori);
            produkRepository.save(produk);

        } catch (IOException | SQLException e) {
            log.error("Error updating product: {}", e.getMessage());
            throw new RuntimeException("Gagal memperbarui produk: " + e.getMessage());
        }
    }

    public void softDeleteProduk(int id) {
        Produk produk = produkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produk tidak ditemukan"));

        produk.setDeleted(true); // flag sebagai dihapus
        produkRepository.save(produk);
    }
}
