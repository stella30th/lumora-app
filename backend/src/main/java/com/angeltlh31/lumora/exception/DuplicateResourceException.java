package com.angeltlh31.lumora.exception;

// Dung khi client co tao du lieu bi trung (vd: username/email da ton tai).
// Se duoc GlobalExceptionHandler chuyen thanh HTTP 409 Conflict.
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
