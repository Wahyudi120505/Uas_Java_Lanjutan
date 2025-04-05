package com.example.hay_mart.services.kasir;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.hay_mart.dao.UserDao;
import com.example.hay_mart.dto.PageResponse;
import com.example.hay_mart.dto.kasir.KasirResponse;
import com.example.hay_mart.models.User;
import com.example.hay_mart.services.image.ConvertImageService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KasirServiceImpl implements KasirService {
    @Autowired
    UserDao userDao;

    @Autowired
    ConvertImageService convertImage;
    
    @Override
    public PageResponse<KasirResponse> getAllKasir(String nama, int page, int size, String sortBy, String sortOrder) {
        PageResponse<User> userPage = userDao.getAllKasir(nama, page, size, sortBy, sortOrder);

        List<KasirResponse> userResponses = userPage.getItems().stream()
                .map(this::toKasirResponse)
                .collect(Collectors.toList());

        return PageResponse.success(userResponses, userPage.getPage(), userPage.getSize(), userPage.getTotalItem());
    }

    private KasirResponse toKasirResponse(User user) {
        try {
            return KasirResponse.builder()
                    .nama(user.getNama())
                    .email(user.getEmail())
                    .status(user.getStatus())
                    .role(user.getRole().getRoleName())
                    .image(convertImage.convertImage(user.getImage()))
                    .build();
        } catch (IOException | SQLException e) {
            log.error("Gagal konversi image untuk user: {}", e.getMessage());
            throw new RuntimeException("Gagal konversi user: " + e.getMessage());
        }
    }

}
