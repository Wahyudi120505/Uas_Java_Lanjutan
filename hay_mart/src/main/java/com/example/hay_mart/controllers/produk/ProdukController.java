package com.example.hay_mart.controllers.produk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.example.hay_mart.dto.GenerikResponse;
import com.example.hay_mart.dto.PageResponse;
import com.example.hay_mart.dto.produk.ProdukRequest;
import com.example.hay_mart.dto.produk.ProdukResponse;
import com.example.hay_mart.dto.produk.UpdateProduk;
import com.example.hay_mart.services.produk.ProdukService;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/produk")
@RestController
@Slf4j
public class ProdukController {
    @Autowired
    ProdukService produkService;

    @PostMapping("/create")
    public ResponseEntity<GenerikResponse<Object>> create(@RequestBody ProdukRequest prequest){
        produkService.create(prequest);
        return ResponseEntity.ok().body(GenerikResponse.builder()
                            .success(true)
                            .message("Data berhasil di tambahkan")
                            .data(null)
                            .build());
    }

    @GetMapping("/get-produk-page")
    public ResponseEntity<Object> getProdukPagEntity() {
        try {
            return ResponseEntity.ok().body(produkService.getProduksPage());
        } catch (Exception e) {
            log.info(e.getMessage());
            return null;
        }
    }

    @GetMapping("/get-all-produks")
    public ResponseEntity<Object> getAll(@RequestParam int page, 
            @RequestParam(required = false) String nama,
            @RequestParam(required = false) String kategori,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice) {
        try {
            PageResponse<ProdukResponse> response = produkService.getAllProduks(nama, kategori, page, 10, sortBy,
                    minPrice, maxPrice);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            log.info(e.getMessage());
            return ResponseEntity.internalServerError().body("invalid");
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<GenerikResponse<Object>> update(@PathVariable int id ,
                                         @RequestBody UpdateProduk uproduk){
                                        
        try{
            produkService.update(id, uproduk);

        }catch(ResponseStatusException ex){
            ex.printStackTrace();
            return ResponseEntity.status(ex.getStatusCode()).body(GenerikResponse.builder()
                                .success(true)
                                .message(ex.getReason())
                                .data(null)
                                .build());

        } catch(Exception e){
            return ResponseEntity.internalServerError().body(GenerikResponse.builder()
                                .success(false)
                                .message("Terjadi kesalahan di sistem internal")
                                .data(null)
                                .build());
        }

        return ResponseEntity.ok().body(GenerikResponse.builder()
                            .success(true)
                            .message("Data berhasil di update")
                            .data(null)
                            .build());
    }
}
