# Enocta Test Automation Case Study

Bu proje, Yazılım Test Mühendisi pozisyonu için hazırlanmış örnek bir test otomasyon projesidir. Proje kapsamında hem API testleri hem de Amazon Türkiye üzerinde E2E (Uçtan Uca) UI testleri gerçekleştirilmektedir.

## 🏗️ Mimari ve Teknolojiler
- **Dil:** Java 17
- **Test Framework:** TestNG & Cucumber BDD
- **Tarayıcı Otomasyonu:** Selenium WebDriver (v4.33.0)
- **API Testleri:** REST Assured
- **Raporlama:** Allure Report & Cucumber HTML Report
- **Mock Server:** Node.js (Express.js)
- **CI/CD:** GitHub Actions
- **Containerization:** Docker & Docker Compose

## 🚀 Gereksinimler
- Java 17 (JDK)
- Maven
- Node.js (Mock Server için)
- Docker & Docker Compose (İsteğe bağlı, Docker ile çalıştırmak için)

## 💻 Çalıştırma Komutları

### Lokal Ortamda Çalıştırma
Tüm testleri varsayılan ayarlarla (Chrome) çalıştırmak için:
```bash
mvn clean verify
```

Sadece belirli bir tarayıcıda çalıştırmak için (chrome, firefox, edge):
```bash
mvn clean verify -Dbrowser=firefox
```

Sadece API testlerini veya sadece UI testlerini çalıştırmak için:
```bash
mvn clean verify -Dtest=ApiRunner
mvn clean verify -Dtest=ChromeRunner,FirefoxRunner,EdgeRunner
```

### 🐳 Docker ile Çalıştırma
Projeyi Docker üzerinden tamamen izole bir ortamda headless (arkaplanda) çalıştırmak çok kolaydır:
```bash
docker-compose up --build
```
Bu komut, hem Maven'i kurar hem de Google Chrome ve Firefox'u yükleyerek testleri container içerisinde koşturur. Çıkan raporlar lokalinizdeki `target` klasörüne yansıyacaktır. Farklı tarayıcı seçmek için:
```bash
BROWSER=firefox docker-compose up --build
```

## 📊 Raporlama
Testler tamamlandığında iki farklı rapor oluşturulur:
1. **Cucumber HTML Report:** `target/cucumber-report-all/cucumber-html-reports/overview-features.html`
2. **Allure Report:** Raporu lokalde görüntülemek için şu komutu çalıştırabilirsiniz:
```bash
mvn allure:serve
```
*Not: GitHub Actions üzerinde çalıştırılan testlerin Allure raporları otomatik olarak GitHub Pages'e deploy edilmektedir.*

## ⚠️ Bilinen Kısıtlar ve Bot Koruması
Bu projede e-ticaret UI senaryosu doğrudan canlı **amazon.com.tr** üzerinde koşulmaktadır. 
- Amazon'un gelişmiş bot koruması (Captcha vb.) veri merkezi ve bulut IP'lerinden gelen (örneğin GitHub Actions veya Azure) otomasyonist istekleri engelleyebilmektedir. 
- Bu sebeple, **GitHub Actions pipeline'ında UI testleri non-blocking (continue-on-error: true)** olarak ayarlanmıştır.
- Test eğer bot korumasına takılırsa `Failed` (Hata) yerine profesyonel bir şekilde **`Skipped` (Atlandı)** durumuna alınır, hata anındaki ekran görüntüsü ve sayfa kaynağı (HTML) rapora eklenir.
- Testler, lokal bilgisayarlarda veya yerel Docker ağlarında herhangi bir bot korumasına takılmadan başarıyla çalışmaktadır.
