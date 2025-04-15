package com.example.hay_mart.dto.pemesanan;

import java.util.List;
import lombok.Data;

@Data
public class PemesananRequest {
    private List<ItemRequest> items;
}
