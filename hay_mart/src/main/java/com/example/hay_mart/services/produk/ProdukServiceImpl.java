package com.example.hay_mart.services.produk;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.example.hay_mart.services.image.ConvertImageServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.example.hay_mart.dao.ProdukDao;
import com.example.hay_mart.dto.PageResponse;
import com.example.hay_mart.dto.produk.ProdukRequest;
import com.example.hay_mart.dto.produk.ProdukResponse;
import com.example.hay_mart.dto.produk.UpdateProduk;
import com.example.hay_mart.models.Kategori;
import com.example.hay_mart.models.Produk;
import com.example.hay_mart.repositorys.KategoriRepository;
import com.example.hay_mart.repositorys.ProdukRepository;
import com.example.hay_mart.services.image.ConvertImageService;

@Service
@Slf4j
public class ProdukServiceImpl implements ProdukService {

    private final ConvertImageServiceImpl convertImageServiceImpl;
    @Autowired
    ProdukDao produkDao;

    @Autowired
    ProdukRepository produkRepository;

    @Autowired
    KategoriRepository kategoriRepository;

    @Autowired
    ConvertImageService convertImage;

    ProdukServiceImpl(ConvertImageServiceImpl convertImageServiceImpl) {
        this.convertImageServiceImpl = convertImageServiceImpl;
    }

    @Override
    public Integer getProduksPage() {
        Integer totalPage = (int) Math.ceil((double) produkRepository.findAll().size() / 10);
        return totalPage;
    }

    @Override
    public void create(ProdukRequest request) {
        produkRepository.save(toProduk(request));
    }

    @Override
    public PageResponse<ProdukResponse> getAllProduks(String nama, String kategori, int page, int size, String sortBy,
            Integer minPrice, Integer maxPrice) {

        Kategori namaKategori = kategoriRepository.findKategoriByNama(kategori);
        PageResponse<Produk> produkPage = produkDao.getAll(nama, namaKategori, page, size, sortBy, minPrice, maxPrice);

        List<ProdukResponse> produkResponses = produkPage.getItems().stream()
                .map(this::toProdukResponse)
                .collect(Collectors.toList());

        return PageResponse.success(produkResponses, page, size, size);
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
            e.printStackTrace();
            return null;
        }
    }

    private Produk toProduk(ProdukRequest request) {
        try {
            Kategori kategori = kategoriRepository.findKategoriByNama(request.getKategori());

            if (kategori == null) {
                throw new RuntimeException("Kategori tidak ditemukan: " + request.getKategori());
            }

            boolean exists = produkRepository.existsByNamaIgnoreCase(request.getNama());
            if (exists) {
                throw new IllegalArgumentException("Produk dengan nama '" + request.getNama() + "' sudah ada!");
            }

            String status = request.getStok() > 0 ? "Tersedia" : "Tidak Tersedia";
            return Produk.builder()
                    .nama(request.getNama())
                    .harga(request.getHarga())
                    .stok(request.getStok())
                    .fotoProduk(convertImageServiceImpl.convertString(request.getImage()))
                    .keterangan(request.getKeterangan())
                    .status(status)
                    .kategori(kategori)
                    .build();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void update(int id, UpdateProduk uproduk) {
        Optional<Produk> produk = produkRepository.findById(id);
        Kategori kategori = kategoriRepository.findKategoriByNama(uproduk.getKategori());

        if (kategori == null) {
            throw new RuntimeException("Kategori tidak ditemukan: " + uproduk.getKategori());
        }

        //ini entah mau diapake ap enggak karna tiap ngupdate harus ganti nama jg
        boolean exists = produkRepository.existsByNamaIgnoreCase(uproduk.getNama());
        if (exists) {
            throw new IllegalArgumentException("Produk dengan nama '" + uproduk.getNama() + "' sudah ada!");
        }

        String status = uproduk.getStok() > 0 ? "Tersedia" : "Tidak Tersedia";

        try {
            if (produk.isPresent()) {
                Produk produkToUpdate = produk.get();
                produkToUpdate.setNama(uproduk.getNama());
                produkToUpdate.setHarga(uproduk.getHarga());
                produkToUpdate.setStok(uproduk.getStok());
                produkToUpdate.setKeterangan(uproduk.getKeterangan());
                produkToUpdate.setFotoProduk(convertImageServiceImpl.convertString(uproduk.getImage()));
                produkToUpdate.setStatus(status);
                produkToUpdate.setKategori(kategori);
                produkRepository.save(produkToUpdate);

            } else {
                log.debug("Produk id yang di cari : {}", id);
                log.info("Produk id yang di cari : {}", id);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produk id tidak ditemukan");
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}
