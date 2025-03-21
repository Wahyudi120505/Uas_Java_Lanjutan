package com.example.hay_mart.services.produk;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.hay_mart.dao.ProdukDao;
import com.example.hay_mart.dto.PageResponse;
import com.example.hay_mart.dto.produk.ProdukResponse;
import com.example.hay_mart.models.Kategori;
import com.example.hay_mart.models.Produk;
import com.example.hay_mart.repositorys.KategoriRepository;
import com.example.hay_mart.repositorys.ProdukRepository;
import com.example.hay_mart.services.image.ConvertImageService;

@Service
public class ProdukServiceImpl implements ProdukService{
    @Autowired
    ProdukDao produkDao;

    @Autowired
    ProdukRepository produkRepository;

    @Autowired
    KategoriRepository kategoriRepository;

    @Autowired
    ConvertImageService convertImage;

    @Override
    public Integer getProduksPage() {
        Integer totalPage = (int) Math.ceil((double) produkRepository.findAll().size() / 10);
        return totalPage;
    }

    @Override
    public PageResponse<ProdukResponse> getAllProduks(String nama, String kategori, int page, int size, String sortBy,
            Integer minPrice, Integer maxPrice) {
                
                Kategori namaKategori = kategoriRepository.findKategoriByNama(kategori);
                PageResponse<Produk> produkPage = produkDao.getAll(nama, namaKategori, page, size, sortBy, minPrice, maxPrice);

                List<ProdukResponse> produkResponses = produkPage.getItems().stream()
                    .map(this::toProduk)
                    .collect(Collectors.toList());

        return PageResponse.success(produkResponses, page, size, size);
    }

    private ProdukResponse toProduk(Produk produk){
        try {
            return ProdukResponse.builder()
            .nama(produk.getNama())
            .harga(produk.getHarga())
            .stok(produk.getStok())
            .image(convertImage.convertImage(produk.getFotoProduk()))
            .keterangan(produk.getKeterangan())
            .kategori(produk.getKategori().getNama())
            .build();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
