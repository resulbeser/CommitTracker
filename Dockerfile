# Temel image olarak OpenJDK 22 kullanıyoruz
FROM openjdk:22-jdk-slim

# Çalışma dizinini oluştur ve ayarla
WORKDIR /app

# Maven build çıktısını container'a kopyala
COPY target/CommitTracker-1.0-SNAPSHOT.jar app.jar

# Uygulamayı başlat
ENTRYPOINT ["java", "-jar", "app.jar"]
