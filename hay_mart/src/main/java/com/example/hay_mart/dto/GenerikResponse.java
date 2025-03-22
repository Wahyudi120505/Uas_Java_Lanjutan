package com.example.hay_mart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerikResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> GenerikResponse<T> success(T data, String message){
        return GenerikResponse.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .build();
    }

    public static <T> GenerikResponse<T> error(String message){
        return GenerikResponse.<T>builder()
            .success(false)
            .message(message)
            .build();
    }
}
