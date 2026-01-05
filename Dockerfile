# Sử dụng Java 21 để chạy
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy file .jar từ máy thật vào Docker
# Dấu * để tự nhận tên file dù version có đổi
COPY target/*.jar app.jar

EXPOSE 8281
ENTRYPOINT ["java", "-jar", "app.jar"]