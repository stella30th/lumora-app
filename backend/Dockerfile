# ===== Stage 1: BUILD =====
# Dung image co san JDK 17 (chua Java Development Kit, du de bien dich) de build ra file .jar.
# Image nay chi ton tai trong luc build, KHONG di vao image cuoi cung.
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper + pom.xml TRUOC, chua copy code (src/) voi vinh vao 1 layer Docker rieng.
# Ly do: Docker cache tung layer theo thu tu COPY. Neu chi sua code (khong dong pom.xml),
# layer "tai dependency" ben duoi van duoc lay tu cache (khong tai lai tu Maven Central moi lan build)
# -> build lai nhanh hon rat nhieu khi lap code nhieu lan.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy code that su, build ra file jar chay duoc (fat jar, da gom het dependency ben trong).
# -DskipTests: khong chay test luc build image, vi test integration dung Testcontainers can
# goi Docker tu ben trong container build (Docker-in-Docker) - khong hop trong CI/CD deploy don gian.
# Test van chay o may local (./mvnw test) truoc khi push code nhu binh thuong.
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# ===== Stage 2: RUNTIME =====
# Image cuoi cung CHI co JRE (Java Runtime, khong co compiler/Maven) -> nho hon nhieu so voi
# image lam build. Day la ky thuat "multi-stage build": tach rieng moi truong build va moi
# truong chay, image cuoi khong mang theo rac cua qua trinh build.
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Chi copy DUY NHAT file .jar da build xong tu Stage 1 sang, khong copy gi khac.
COPY --from=build /app/target/*.jar app.jar

# Render/Railway se tu dong gan bien moi truong PORT luc chay (gia tri thay doi tuy nen tang,
# khong duoc hardcode 8080). Spring Boot doc PORT nay qua server.port trong application.properties
# (da them: server.port=${PORT:8080}) - EXPOSE chi la khai bao mang tinh tai lieu, khong bat buoc app dung dung port do.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
