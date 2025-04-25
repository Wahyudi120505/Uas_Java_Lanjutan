package com.example.hay_mart.services.laporan;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.hay_mart.dto.laporan.LaporanProdukResponse;
import com.example.hay_mart.models.LaporanProduk;
import com.example.hay_mart.repositorys.LaporanProdukRepository;

@Service
public class LaporanProdukServiceImpl implements LaporanProdukService {
    @Autowired
    private LaporanProdukRepository laporanProdukRepository;

    @Override
    public List<LaporanProdukResponse> getLaporanProduk() {
        List<LaporanProduk> laporanList = laporanProdukRepository.findAll();
        return laporanList.stream()
                .map(laporan -> LaporanProdukResponse.builder()
                        .namaProduk(laporan.getNamaProduk())
                        .jumlahTerjual(laporan.getJumlahTerjual())
                        .stok(laporan.getStok())
                        .hargaSatuan(laporan.getHargaSatuan())
                        .total(laporan.getTotal())
                        .deleted(laporan.getDeleted())
                        .build())
                .collect(Collectors.toList());
    }
}
