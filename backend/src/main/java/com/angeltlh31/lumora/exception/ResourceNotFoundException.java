package com.angeltlh31.lumora.exception;

// RuntimeException (unchecked) de khong bat buoc noi goi phai try-catch hay khai bao "throws".
// Se duoc mot @ControllerAdvice bat va chuyen thanh response 404 o buoc sau.
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
