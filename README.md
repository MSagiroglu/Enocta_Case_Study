# Test Otomasyon Çerçevesi (Amazon E-Ticaret & Mock API)

Bu proje, Yazılım Test Mühendisi vaka çalışması (case study) için hazırlanmış, **POM (Page Object Model)** tasarım desenine sıkı sıkıya bağlı, **Paralel test koşumuna** uygun, **BDD (Behavior Driven Development)** altyapısına sahip tam donanımlı, kurumsal seviyede bir test otomasyon çerçevesidir.

## 🛠 Kullanılan Teknolojiler ve Mimari Yaklaşım

- **Dil:** Java 17
- **Proje Yönetimi:** Maven
- **UI Otomasyonu:** Selenium WebDriver (v4.25.0)
- **API Otomasyonu:** REST Assured (v5.4.0)
- **BDD Framework:** Cucumber (v7.14.0)
- **Test Runner & Paralel Test:** TestNG
- **Konteyner Mimarisi:** Docker & Docker Compose
- **Raporlama:** Masterthought (Maven Cucumber Reporting) & **Allure Report**
- **Mock Server:** Node.js (Express)

---

## 🏆 Bonus Şartların Karşılanması ve Mimari Kararlar

Proje değerlendirmesinde yer alan bonus özellikler, modern test mühendisliği yaklaşımlarıyla aşağıdaki gibi sisteme entegre edilmiştir:

1. **Testlerin Docker Üzerinde Koşması (Selenium Grid / Standalone)**
   - Proje içerisinde yer alan `docker-compose.yml` dosyası, tüm testleri CI/CD veya herhangi bir sunucu ortamında izole bir şekilde çalıştırmak üzere tasarlanmıştır. Tarayıcı bağımlılıklarından kurtulmak ve Selenium Grid mimarisini simüle etmek amacıyla entegre edilmiştir.

2. **Paralel Test Koşumu (Cross-Browser)**
   - Testler; **Chrome, Firefox ve Edge** tarayıcılarında aynı anda koşacak şekilde `testng.xml` üzerinden `parallel="tests"` parametresiyle yapılandırılmıştır.
   - Her tarayıcı için ayrı bir Runner sınıfı (`ChromeRunner.java`, `FirefoxRunner.java` vb.) tasarlanmıştır. Bu sayede testler birbirini ezmez.
   - Çakışmaları önlemek için `Driver.java` dosyasında `InheritableThreadLocal<WebDriver>` kullanılarak her işlemci parçacığının (thread) sadece kendi WebDriver'ına ve tarayıcı definesine ulaşması garanti altına alınmıştır (Thread-Safe mimari).

3. **Gelişmiş Raporlama Sistemleri (Masterthought & Allure)**
   - Testler paralel koştuğunda raporların birbirini ezmesi kronik bir problemdir. Bu projede, her runner'ın JSON çıktısı ayrı üretilir. Maven Verify fazında bu raporlar toplanıp;
     - Ayrı ayrı klasörlerde `cucumber-report-chrome`, `cucumber-report-firefox`, `cucumber-report-edge` olarak,
     - Tüm testlerin özetini barındıran `cucumber-report-all` olarak konsolide HTML raporlarına dönüştürülür.
   - **Allure Report:** Raporların Timeline (Zaman Çizelgesi) olarak da görülebilmesi ve testlerin paralel başladığının grafiksel gösterimi için sisteme Allure Report entegre edilmiştir.

---

## 📌 Case (Vaka) Şartlarının Karşılanması & Teknik Detaylar

### 1. Page Object Model (POM) ve Atomik Yapı
Step Definition (`UI_StepDefs.java`) dosyalarında hiçbir şekilde Selenium locator (`By.id`, `Driver.findElement` vb.) yer almaz. Tüm elementler `pages` paketi altındaki sınıflarda (`@FindBy`) tutulur. Bu yaklaşım kod tekrarını önler ve Amazon'un sürekli değişen arayüzlerinde bakımı (maintenance) sadece sayfa sınıflarına indirger.

### 2. Gelişmiş "Fallback" Mekanizması (Esneklik)
Amazon'un UI yapısı sıklıkla değişir (A/B testing). Örneğin, "Fiyat Filtresi" veya "Diğer Satıcılar" butonları DOM'da her zaman aynı yapıda bulunmayabilir. Projede, sabit 20 saniyelik beklemeler (Implicit Wait) optimize edilerek, eleman bulunamadığı an ışık hızıyla **B Planına** (URL manipülasyonu veya varsayılan sepete ekle butonu) geçen "try-catch" fallback algoritmaları geliştirilmiştir. Bu sayede testler asla boş yere beklemez (Test koşum süresi 2 dakikadan 40 saniyeye indirilmiştir).

### 3. API Testlerinin Tarayıcı Açmadan (%100 Headless) Çalışması
UI senaryoları `@ui`, API senaryoları `@api` tagiyle işaretlenmiştir. `Hooks.java` dosyasında tarayıcı ayağa kaldırma (`Driver.getDriver()`) kodları yalnızca `@ui` tagine sahip senaryolara bağlanmıştır. Ayrı bir `ApiRunner.java` üzerinden koşan API testleri, hiçbir şekilde arka planda Chrome başlatmadan direkt RestAssured ile HTTP isteklerini atar ve anında sonuç döner. 

### 4. Mock Server
Gerçekte var olmayan API servislerini simüle etmek için Node.js + Express tabanlı çok hafif bir Mock Server ayağa kaldırılmıştır. Testler başladığında Java kodları (MockServerManager) arka planda otomatik olarak Node.js'i tetikler ve sunucuyu başlatır.

### 5. Loglama ve Senkronizasyon (Thread ID)
Oluşturulan `LoggerUtils.java` sınıfı, paralel koşan testlerin loglarının karışmaması için her thread'i `Worker-1`, `Worker-2` şeklinde numaralandırarak işlemleri yazar. Hem konsolda renkli şekilde görülür hem de geriye dönük inceleme için `target/logs` klasörüne zaman damgalı dosyalar halinde kaydedilir.

---

## 🚀 Projeyi Çalıştırma Adımları

### Ön Koşul (Sadece İlk Kurulum İçin)
Mock sunucusunun çalışması için `mock-server` klasörüne gidip bağımlılıkları yüklemeniz gerekmektedir:
```bash
cd mock-server
npm install
cd ..
```

### 1. Testleri Lokalde Çalıştırma (Paralel Cross-Browser)
Aşağıdaki komut `testng.xml`'i tetikler. Chrome, Firefox ve Edge tarayıcıları aynı anda (paralel) başlar ve API testleri de bunlardan bağımsız, tarayıcısız olarak arka planda koşar.
```bash
mvn clean verify
```

### 2. Raporları İnceleme
Testler bittikten sonra raporları iki farklı araçla inceleyebilirsiniz:

**A) Masterthought Raporları:**
`target` klasörü altında otomatik oluşur. İçerisindeki `.html` dosyalarını tarayıcınızda açabilirsiniz.
- `target/cucumber-report-all/overview-features.html` (Tüm testlerin özeti)
- `target/cucumber-report-chrome/overview-features.html` (Sadece Chrome)

**B) Allure Raporunu Görüntüleme:**
Projede Allure Report entegrasyonu mevcuttur. Testlerin tam olarak paralel çalışıp çalışmadığını, hangi saniyede başlayıp bittiğini (Zaman Çizelgesi/Timeline) ve grafiksel dağılımlarını görmek için komut satırına şunu yazın:
```bash
mvn allure:serve
```
*(ÖNEMLİ: Eğer `target/allure-results not found` hatası alırsanız, bu raporların henüz oluşmadığı anlamına gelir. Öncelikle `mvn clean verify` komutuyla testleri bir kez çalıştırıp bitmesini beklemeniz, ardından `mvn allure:serve` komutunu çalıştırmanız gerekmektedir.)*

Bu komut arka planda bir web sunucusu başlatır ve muazzam detaylı Allure raporunu varsayılan tarayıcınızda otomatik olarak açar. (İncelemeniz bittiğinde terminalde `Ctrl + C` yaparak sunucuyu kapatabilirsiniz).

> [!NOTE]
> `target/site/allure-maven-plugin/index.html` dosyasına çift tıklayarak açarsanız rapor sonsuz "Loading..." ekranında kalır. Bu bir hata değildir; tarayıcılar güvenlik gereği (CORS) `file://` üzerinden JSON okumaya izin vermez. Raporu her zaman `mvn allure:serve` ile (yerel web sunucusu üzerinden) açın.

---

## ☁️ CI/CD – GitHub Actions & Canlı Allure Raporu

`.github/workflows/test-execution.yml` dosyası; her `push`'ta, her gece 00:00'da (cron) ve elle tetiklendiğinde (`workflow_dispatch`) çalışır:

1. Ubuntu sunucusunda JDK 17 ve Node.js kurulur, mock server bağımlılıkları yüklenir.
2. `mvn clean verify` ile API + Chrome/Firefox/Edge testleri paralel koşar. GitHub Actions `CI=true` değişkenini set ettiği için `Driver.java` tarayıcıları otomatik olarak **headless** modda açar.
3. `mvn allure:report` ile Allure raporu üretilir.
4. Masterthought raporları, Allure raporu ve loglar **Artifacts** olarak indirilebilir hale gelir.
5. Allure raporu **GitHub Pages**'e yayınlanır → `https://<kullanici-adi>.github.io/<repo-adi>/` adresinden kurulum gerektirmeden canlı incelenebilir.

**Tek seferlik ayar:** Repo → *Settings* → *Pages* → *Build and deployment* → *Source* = **GitHub Actions** seçilmelidir.

---

**Not:** Hata anında (Assertion Failure vs.) alınan ekran görüntüleri hem Masterthought raporlarına hem de Allure raporuna otomatik olarak "Attachment" (ek) şeklinde yapıştırılır.
