package com.example.hay_mart.dao;

import com.example.hay_mart.dto.PageResponse;
import com.example.hay_mart.models.User;

public interface UserDao {
    PageResponse<User> getAllKasir(String nama, int page, int size, String sortBy, String sortOrder);
}
