package com.example.hay_mart.controllers.produk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.hay_mart.dto.GenerikResponse;
import com.example.hay_mart.dto.detail_pemesanan.DetailPemesananRequest;
import com.example.hay_mart.services.detail_pemesanan.DetailPemesananService;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/detail-pemessanan")
@RestController
@Slf4j
public class DetailPemesananController {
    @Autowired
    DetailPemesananService detailPemesananService;

    //ini belum dicek karna masih bimbang yg di dto
    @PostMapping("/create")
    public ResponseEntity<GenerikResponse<Object>> create(@RequestBody DetailPemesananRequest drequest){
        detailPemesananService.create(drequest);
        return ResponseEntity.ok().body(GenerikResponse.builder()
                            .success(true)
                            .message("Data berhasil di tambahkan")
                            .data(null)
                            .build());
    }
}
