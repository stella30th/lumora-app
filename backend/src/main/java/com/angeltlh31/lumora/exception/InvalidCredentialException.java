package com.angeltlh31.lumora.exception;

// Nem ra khi email khong ton tai HOAC password sai.
// Co tinh dung CHUNG 1 exception + 1 message cho ca 2 truong hop (xem UserService.login) -
// neu bao rieng "email khong ton tai" vs "sai password", ke tan cong co the do ngay email nao
// da dang ky trong he thong (user enumeration attack).
public class InvalidCredentialException extends RuntimeException {
    public InvalidCredentialException(String message) {
        super(message);
    }
}
