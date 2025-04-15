package com.example.hay_mart.services.image;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import org.springframework.web.multipart.MultipartFile;

public interface ConvertImageService {
    public String convertImage(Blob blob) throws IOException, SQLException;

    public Blob convertBlob(MultipartFile image) throws IOException, SQLException;
}