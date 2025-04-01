package com.example.hay_mart.services.image;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;

public interface ConvertImageService{
    public String convertImage(Blob blob) throws IOException, SQLException;
    public Blob convertString(String image) throws IOException, SQLException;
}
