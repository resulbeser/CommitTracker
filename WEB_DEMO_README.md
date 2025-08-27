# CommitTracker - Web Demo Kullanım Kılavuzu

## 🚀 Proje Hakkında

CommitTracker, Git platformlarından commit verilerini çeken güçlü bir araçtır. İki farklı kullanım moduna sahiptir:

1. **JavaFX Desktop Uygulaması** - Yerel kullanım için GUI arayüzü
2. **Web Demo** - Portfolio web sitesi için entegrasyon

## 📋 Çalıştırma Seçenekleri

### 1. JavaFX Desktop Uygulaması (Mevcut)
```bash
# IntelliJ'den yeşil run butonuyla
# veya terminal'den:
mvn javafx:run
```

### 2. Web Demo (YENİ!)
```bash
# Web demo başlatmak için:
mvn spring-boot:run

# Web arayüzü: http://localhost:8080
```

## 🌐 Web Demo Özellikleri

- **Responsive Design**: Bootstrap 5 ile modern tasarım
- **Gerçek Zamanlı API**: Mevcut servislerinizle entegre
- **Portfolio Uyumlu**: Web sitenize kolayca entegre edilebilir
- **Güvenli**: Rate limiting ve input validation
- **Multi-Platform**: GitHub, GitLab, Bitbucket, Azure DevOps desteği

## 🔧 Teknik Detaylar

### Web Demo Stack:
- **Backend**: Spring Boot 3.3.3
- **Frontend**: Thymeleaf + Bootstrap 5
- **API**: RESTful JSON endpoints
- **Styling**: Modern CSS3 + Gradients

### Mevcut JavaFX Yapısı:
- **UI Framework**: JavaFX 24.0.1
- **Architecture**: MVC pattern
- **Services**: Modüler VCS service layer

## 📊 API Endpoints

```
GET  /              → Ana sayfa
POST /api/commits   → Commit verilerini getir
GET  /demo          → Demo sayfası
```

## 🎯 Portfolio Entegrasyonu

Web demo, portfolio sitenizde şu şekilde kullanılabilir:

1. **Embedded Demo**: iframe ile entegre
2. **API Integration**: JavaScript ile API'yi doğrudan kullan
3. **Full Page**: Ayrı sayfa olarak göster

## 📱 Kullanım

1. Platform seçin (GitHub, GitLab, etc.)
2. Repository bilgilerini girin
3. Private repo için access token ekleyin
4. "Commit'leri Getir" butonuna tıklayın
5. Sonuçları görüntüleyin

## 🔒 Güvenlik

- Input validation
- Rate limiting
- XSS protection
- CORS configuration
- Secure token handling
