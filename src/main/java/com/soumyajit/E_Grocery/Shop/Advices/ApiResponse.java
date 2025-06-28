package com.soumyajit.E_Grocery.Shop.Advices;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiResponse<T>{
    private LocalDateTime localDateTime;
    private T data;
    private ApiError apiError;


    public ApiResponse() {
        this.localDateTime = LocalDateTime.now();
    }

    public ApiResponse(ApiError apiError) {
        this();
        this.apiError = apiError;
    }

    public ApiResponse(T data) {
        this();
        this.data = data;
    }
}
