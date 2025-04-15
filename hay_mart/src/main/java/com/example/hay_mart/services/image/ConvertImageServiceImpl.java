package com.example.hay_mart.services.image;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Base64;
import javax.sql.rowset.serial.SerialBlob;
import org.springframework.stereotype.Service;

@Service
public class ConvertImageServiceImpl implements ConvertImageService {
    @Override
    public String convertImage(Blob image) throws IOException, SQLException {
        byte[] imageBytes = image.getBytes(1, (int) image.length());
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    @Override
    public Blob convertString(String base64Image) throws IOException, SQLException {
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        return new SerialBlob(imageBytes);
    }
}
