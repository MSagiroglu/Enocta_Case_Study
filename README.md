# Enocta Test Automation Case Study

Bu proje, Yazılım Test Mühendisi pozisyonu için hazırlanmış gelişmiş bir test otomasyon mimarisi projesidir. Proje kapsamında hem API testleri hem de Amazon Türkiye üzerinde E2E (Uçtan Uca) UI testleri gerçekleştirilmektedir. Basit bir otomasyon betiğinden ziyade; CI/CD entegrasyonu, paralel koşum, mock server yönetimi ve %100 nesne yönelimli (OOP/POM) tasarım desenlerini içeren kurumsal bir çatı sunar.

## 🏗️ Mimari ve Teknolojiler
- **Dil:** Java 17
- **Test Framework:** TestNG & Cucumber BDD
- **Tarayıcı Otomasyonu:** Selenium WebDriver (v4.33.0)
- **API Testleri:** REST Assured
- **Raporlama:** Allure Report & Cucumber HTML Report
- **Mock Server:** Node.js (Express.js tabanlı özel Middleware destekli)
- **CI/CD:** GitHub Actions (Paralel Job Mimarisi)
- **Tasarım Deseni:** Strict Page Object Model (POM) & ThreadLocal (Thread-Safety)

## 🌟 Öne Çıkan Özellikler (Advanced Features)

1. **Tam Paralel Koşum (Parallel Execution) ve Thread Safety:**
   `testng-ui.xml` üzerinden UI testleri Chrome, Firefox ve Edge tarayıcılarında aynı anda (paralel) çalıştırılır. `Driver` sınıfında kullanılan **ThreadLocal** yapısı sayesinde tarayıcı oturumları birbirini ezmez.
2. **Kusursuz GitHub Actions CI/CD Mimarisi:**
   GitHub Actions'ta UI testleri ve API testleri birbirini beklemez, farklı sanal makinelerde eşzamanlı (paralel) koşar. `generate-report` adında üçüncü bir Job, bu iki farklı koşumun sonuçlarını (Artifacts) indirir, birleştirir ve GitHub Pages'e tek bir rapor olarak yayına alır.
3. **Allure Rapor Yaması (Jackson Allure Patcher):**
   Aynı Cucumber senaryosu farklı tarayıcılarda koştuğunda Allure bunları tek testin tekrarı (retry) olarak algılar. Bunu çözmek için `verify` fazında devreye giren özel bir `AllurePatcher` yazılmıştır. Jackson JSON kütüphanesi kullanarak her test sonucunun `historyId` değerine tarayıcı adı eklenir. Böylece 3 UI ve 1 API testi, 4 ayrı test olarak mükemmel bir şekilde raporlanır.
4. **Katı (Strict) Page Object Model (POM):**
   Kod yazımı sırasında "Clean Code" prensiplerine tamamen sadık kalınmıştır. Step Definition dosyalarının içerisinde hiçbir şekilde `By.id()` veya `Driver.findElement()` gibi teknik locator tanımlamaları bulunmaz. Tüm locator'lar ve dinamik bulma işlemleri Page sınıflarının (Örn: `ProductPage`, `SearchPage`) içinde `@FindBy` ve helper metotlarla soyutlanmıştır.
5. **Dahili API Mock Server:**
   Dış servislere bağımlılığı ortadan kaldırmak için Node.js ile `json-server` altyapısı kullanılmıştır. Üstelik projeye özel bir middleware (`server.js`) eklenerek, POST isteklerinde (Token alma) özel JSON response'ları simüle edilir. CI/CD aşamasında bu sunucu otomatik ayağa kalkar ve test bitince kapanır.
6. **Amazon Bot Koruması Bypass Stratejisi:**
   Canlı e-ticaret sitelerinde bot engeline (Captcha) takılmamak için testlere insansı davranışlar eklenmiştir (dinamik bekleme, çerez yönetimi, rastgele ürün seçimi, login aşamasını misafir kullanıcı olarak geçme vb.).

## 🚀 Gereksinimler
- Java 17 (JDK)
- Maven
- Node.js (Mock Server kurulumu için)

## 💻 Çalıştırma Komutları

### Lokal Ortamda Tüm Testleri Çalıştırma
Projeyi lokalde çalıştırdığınızda (API dahil) tüm UI tarayıcıları arka planda paralel ayağa kalkacaktır:
```bash
mvn clean verify
```

### 📊 Raporlama
Testler tamamlandığında iki farklı rapor oluşturulur:
1. **Cucumber HTML Report:** `target/cucumber-report-all/cucumber-html-reports/overview-features.html`
2. **Allure Report:** Raporu lokalde görüntülemek için şu komutu çalıştırabilirsiniz:
```bash
mvn allure:serve
```
*Not: GitHub Actions üzerinde çalıştırılan testlerin Allure raporları otomatik olarak GitHub Pages'e deploy edilmektedir.*

## ⚠️ Bilinen Kısıtlar ve Bot Koruması
Bu projede e-ticaret UI senaryosu doğrudan canlı **amazon.com.tr** üzerinde koşulmaktadır. 
- Amazon'un gelişmiş bot koruması (Captcha vb.) bulut IP'lerinden gelen (GitHub Actions vb.) otomasyon isteklerini engelleyebilmektedir. 
- Bu sebeple, **GitHub Actions pipeline'ında UI testleri non-blocking (continue-on-error: true)** olarak ayarlanmıştır.
- Test eğer bot korumasına takılırsa `Failed` yerine profesyonel bir şekilde **`Skipped`** durumuna alınır, hata anındaki ekran görüntüsü rapora eklenir. Lokal çalışmalarda testler başarıyla (Passed) sonuçlanmaktadır.
