package com.angeltlh31.lumora.exception;

// Nem ra khi refresh token: khong ton tai trong DB, da bi revoke, hoac da het han.
// Rieng 1 class (khong dung chung InvalidCredentialException) vi day la loi VE TOKEN
// (buoc /refresh), khac han loi VE EMAIL/PASSWORD (buoc /login) - message va ngu canh khac
// nhau, gop chung se gay nham lan khi doc log sau nay.
public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
