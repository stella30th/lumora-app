package com.angeltlh31.lumora.exception;

// Nem ra khi mot user DA dang nhap hop le (Authentication OK - JwtAuthenticationFilter da
// xac nhan token dung) nhung KHONG co quyen voi tai nguyen cu the dang thao tac
// (Authorization that bai - vd sua Deck khong phai cua minh).
//
// Co tinh KHONG dat ten "AccessDeniedException": Spring Security da co san class
// org.springframework.security.access.AccessDeniedException tren classpath (dung noi bo cho
// @PreAuthorize). Neu dat trung ten, IDE rat de auto-import nham class cua Spring thay vi
// class tu viet - GlobalExceptionHandler se khong bat duoc exception nay, loi rat kho nhan ra.
// Dat ten rieng "ForbiddenException" tranh hoan toan kieu nham lan do.
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
