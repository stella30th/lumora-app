package com.angeltlh31.lumora.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.net.URI;

// Muc tieu: giong trai nghiem cua Rider (.NET) - an Run la trinh duyet tu mo thang Swagger,
// khong can tu tay go URL. Viet bang CODE (khong phai cau hinh rieng cua IntelliJ) de hanh vi
// nay chay dung y het du ban dung IntelliJ, VSCode, hay chi go lenh "./mvnw spring-boot:run"
// tu terminal - dung tinh than "du an cho nguoi khac chay duoc" cua Lumora.
//
// ApplicationListener<ApplicationReadyEvent>: Spring Boot ban 1 chuoi "vong doi" (lifecycle
// event) trong luc khoi dong app - ApplicationReadyEvent la su kien CUOI CUNG, ban ra dung
// luc Tomcat da mo cong va san sang nhan request that su (khac ContextRefreshedEvent - ban som
// hon, luc do Tomcat co the chua mo cong xong). Bat dung event nay moi dam bao mo trinh duyet
// khong bi "Connection refused" vi mo qua som.
@Slf4j
@Component
public class SwaggerAutoLauncher implements ApplicationListener<ApplicationReadyEvent> {

    // Key dat vao System Property (KHONG phai bien instance thuong) - ly do giai thich o duoi.
    private static final String ALREADY_OPENED_FLAG = "lumora.swagger.browser.opened";

    @Value("${server.port:8080}")
    private String port;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // spring-boot-devtools: moi lan ban luu file .java, devtools tao lai TOAN BO
        // ApplicationContext (goi la "restart"), nghia la ApplicationReadyEvent ban ra LAI
        // tu dau - neu khong chan, moi lan luu file se bi mo them 1 tab trinh duyet moi.
        // Dung System.getProperty/setProperty (khong phai 1 field "boolean opened = false"
        // binh thuong) vi devtools dung 2 classloader khac nhau: class cua ban (vd class nay)
        // nam trong "restart classloader" - bi NAP LAI (nghia la moi field/static bi reset ve
        // gia tri ban dau) sau moi lan restart; con System Properties cua JVM nam ngoai ca 2
        // classloader do, nen gia tri set 1 lan la con nguyen cho toi khi JVM that su tat.
        if (System.getProperty(ALREADY_OPENED_FLAG) != null) {
            return;
        }

        // GraphicsEnvironment.isHeadless() = true khi may chay KHONG co man hinh/GUI - dung
        // trong moi server that (Docker container, may chu deploy, Vercel, CI...). Desktop
        // .isDesktopSupported() la 1 lop kiem tra nua (vd 1 so Linux desktop cung khong ho tro).
        // Nho 2 dieu kien nay, method se TU DONG khong lam gi ca khi deploy that - khong can
        // xoa code hay tat bang profile rieng, code nay an toan 100% o moi moi truong.
        if (GraphicsEnvironment.isHeadless() || !Desktop.isDesktopSupported()) {
            log.debug("Moi truong khong co GUI (headless) - bo qua tu dong mo Swagger.");
            return;
        }

        String url = "http://localhost:" + port + "/swagger-ui/index.html";
        try {
            System.setProperty(ALREADY_OPENED_FLAG, "true");
            Desktop.getDesktop().browse(new URI(url));
            log.info("Da tu dong mo Swagger UI tai {}", url);
        } catch (Exception ex) {
            // Khong de loi mo trinh duyet (vd may khong co trinh duyet mac dinh) lam
            // anh huong app - day chi la tien ich, khong phai chuc nang cot loi.
            log.warn("Khong the tu dong mo trinh duyet toi {}: {}", url, ex.getMessage());
        }
    }
}
