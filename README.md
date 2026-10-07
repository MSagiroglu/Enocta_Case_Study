# Test Otomasyon Çerçevesi (Amazon E-Ticaret & Mock API)

Bu proje, Yazılım Test Mühendisi vaka çalışması (case study) için hazırlanmış, **POM (Page Object Model)** tasarım desenine sıkı sıkıya bağlı, **Paralel test koşumuna** uygun, **BDD (Behavior Driven Development)** altyapısına sahip tam donanımlı bir test otomasyon çerçevesidir.

## 🛠 Kullanılan Teknolojiler ve Mimari Yaklaşım

- **Dil:** Java 17
- **Proje Yönetimi:** Maven
- **UI Otomasyonu:** Selenium WebDriver (v4.16.1)
- **API Otomasyonu:** REST Assured (v5.4.0)
- **BDD Framework:** Cucumber (v7.14.0)
- **Test Runner & Paralel Test:** TestNG
- **Konteyner Mimarisi:** Docker & Docker Compose
- **Mock Server:** Node.js (Express)

### Neden Bu Teknolojiler ve Tasarım Tercih Edildi? (Mülakat Soru-Cevapları)

1. **Neden Page Object Model (POM)?**
   - *Atomik Yapı:* Spec (Feature) veya StepDef dosyalarında hiçbir şekilde Selenium Locator (`By.id`, `Driver.findElement` vb.) kullanılmamıştır. Elementler `pages` paketindeki sayfa sınıflarında tutulur. Bu sayede bir elementin XPath'i değiştiğinde test kodlarını değil, sadece ilgili Page sınıfını güncelleriz. Kod tekrarını önler ve bakımı kolaylaştırır.
2. **Neden BDD (Cucumber)?**
   - İş birimlerinin (Product Owner, İş Analisti) de anlayabileceği Gherkin (Given, When, Then) dilini kullanarak canlı dokümantasyon (Living Documentation) sağlar. Test senaryoları bir nevi "kullanım kılavuzu" gibi okunabilir.
3. **Paralel Test Yaklaşımımız Nasıl? (TestNG + Cucumber)**
   - `testng.xml` içerisinde `parallel="tests"` parametresini kullandık. Ayrıca `Driver.java` içerisinde `InheritableThreadLocal` ile WebDriver nesnelerini thread-safe hale getirdik. Böylece aynı anda 3 farklı tarayıcıda (Chrome, Firefox, Edge) testlerimiz **eşzamanlı ve bağımsız (isolated)** olarak çalışabilmektedir. `ThreadLocal` kullanımı, statik driver çakışmalarını tamamen önler.
4. **Hooks Neden `@ui` Tagı İle Kullanıldı?**
   - Case'in en önemli gereksinimlerinden biri API testlerinin tarayıcı açmadan çalışmasıydı. Cucumber'da genel bir `@Before`/`@After` yazdığınızda her senaryoda tarayıcı ayağa kalkar. Bunu önlemek için UI senaryolarımızı `@ui`, API senaryolarımızı `@api` ile etiketledik ve Hooks metodlarına `Hooks.java` içerisinde koşul (`@Before("@ui")`) ekledik. Böylece API senaryosu koşarken arkada boş yere Chrome açılıp kapanmaz; test direkt hızlıca RestAssured ile API'ye istek atar ve konsola sonucu basar.
5. **Mock Server Yaklaşımı (Node.js ile Neden Mock?)**
   - *Mocking Nedir?* Gerçekte olmayan bir servisin (Third Party gibi) davranışını simüle etmektir. 
   - *Nasıl Yaptık?* Basit ve hızlı ayağa kalkan Express.js kullandık. Java (Wiremock vs.) de kullanılabilirdi ancak Node.js dockerize etmesi çok hafif olan (alpine tabanlı) ve dışa bağımlılığı (veritabanı vs.) olmayan mükemmel bir çözümdür. Ayrıca otomasyon mühendisinin farklı bir stack'te de hızlıca test altyapısı kurabildiğini gösterir.

## 🚀 Projeyi Çalıştırma Adımları

### 1. API Mock Server'ı Manuel Çalıştırma (Hata Almamak İçin)
Daha önceki manuel denemelerinizde hata almanızın sebebi, git reposunda `node_modules` klasörünün taşınmamasıdır (en iyi pratikler gereği bu klasör VCS'e atılmaz). Klasörü oluşturmak için aşağıdaki komutla bağımlılıkları indirmelisiniz:

```bash
cd mock-server
npm install    # Bu komut package.json'ı okur ve express kütüphanesini indirir
node server.js # Sunucuyu 3000 portunda başlatır
```
*(Not: Otomasyon kodu arka planda ProcessBuilder ile node'u ayağa kaldırmaya çalışır ancak ilk kurulumda npm install yapılmamışsa express bulunamadı hatası alır. Bu yüzden ilk çalıştırmada npm install zorunludur.)*

### 2. Testleri Lokalde Çalıştırma (Tüm Tarayıcılar - Paralel)
Projenin kök dizininde (pom.xml'in olduğu yerde) şu komutu çalıştırın:
```bash
mvn clean verify
```
Bu komut `testng.xml` dosyasını okuyacak ve Chrome, Firefox, Edge tarayıcılarında UI testini paralel olarak koşturacaktır. Ayrıca API testlerini de tarayıcı açmadan koşturacaktır.

### 3. Docker ile Çalıştırma
Projeyi sıfırdan sanal konteynerlerde (Selenium Grid üzerinde) çalıştırmak isterseniz:
```bash
docker-compose up --build
```
*Not: Bu işlemin başarılı olması için bilgisayarınızda Docker Engine'in çalışıyor olması (Docker Desktop'ın açık olması) gerekmektedir.*

## 📌 Senaryo Detayları ve Case Şartlarının Karşılanması

1. **Cep telefonu araması, Fiyat filtresi ve Sıralama:** "15000-20000" TL filtrelenir, ardından Amazon'un filtreleme menüsü kullanılarak "Düşükten Yükseğe" sıralaması yapılır. En üstte çıkan (en düşük fiyatlı) telefon (aksesuar olmadığı kontrol edilerek) seçilir.
2. **Sepete Ekleme:** Ürün, "Diğer Satıcılar" (other sellers) panelindeki ilk seçenekten (veya varsayılan sepete ekle butonundan) sepete eklenir. Amazon puan yapısı çok dinamik olduğu için bu fallback mekanizması kurulmuştur.
3. **Doğrulama (Assertion):** Sepetteki ürünün başlığı ile ürün sayfasındaki başlık eşleştirilir (Amazon bazen title'ı sepette kısaltır, bu yüzden String manipulation ile partial kontrol yapılır). Ayrıca ücretleri birebir aynı olmalıdır kuralı işletilir.
4. **Tarayıcı Açmama (API):** API testlerinde `Driver.getDriver()` hiç çağrılmadığı ve `Hooks` sınırlı tutulduğu için API testleri %100 headless çalışır, sonuçlar hem dosyaya yazılır hem de konsola loglanır. 
5. **Atomik Metotlar ve POM:** `UI_StepDefs.java` dosyasında Selenium metodu yoktur. Bütün locators `HomePage`, `SearchPage`, `ProductPage` ve `CartPage` içerisindedir.
6. **Ekran Görüntüsü (Screenshot):** Test başarısız olursa `@After` metodu devreye girer, ekran görüntüsünü alır ve `target/cucumber-reports.html` dosyasına ekler.
