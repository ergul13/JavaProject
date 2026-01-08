# 🚦 Akıllı Trafik Işığı Kontrol Sistemi

Araç yoğunluğuna dayalı, dinamik yeşil ışık süresi hesaplayan ve gerçekçi araç simülasyonu sunan bir trafik kontrol sistemi.

---

## 📋 Proje Hakkında

Bu proje, dört yönlü bir kavşakta araç yoğunluğunu analiz ederek yeşil ışık sürelerini dinamik olarak hesaplayan ve trafik akışını görsel olarak simüle eden bir sistemdir. JavaFX kullanılarak geliştirilmiş olup, **MVC (Model-View-Controller)** mimarisine ve **OOP (Nesne Yönelimli Programlama)** prensiplerine uygun olarak tasarlanmıştır.

### ✨ Temel Özellikler

- **Dinamik Yeşil Işık Hesaplama**: Her yönün yeşil ışık süresi, araç yoğunluğu oranına göre otomatik hesaplanır
- **Gerçekçi Araç Hareketi**: Araçlar şerit merkezinden ilerler ve kavşakta kavisli (Bezier eğrisi) dönüşler yapar
- **Sağa-Sola Dönüş Desteği**: Araçlar rastgele olarak düz, sağa veya sola dönüş yapabilir
- **Saat Yönünde Trafik Akışı**: Işıklar Kuzey → Doğu → Güney → Batı sırasıyla değişir
- **Çarpışma Önleme**: Araçlar arası güvenli mesafe korunur
- **Geri Sayım Göstergesi**: Her ışık için kalan süre dijital olarak gösterilir

---

## 🏗️ Mimari Yapı

Proje, **MVC tasarım deseni** kullanılarak üç ana katmana ayrılmıştır:

```
src/main/java/com/traffic/
├── Launcher.java           # Uygulama giriş noktası
├── TrafficLightSystem.java # JavaFX Application sınıfı
├── model/                  # Model Katmanı
│   ├── Car.java           # Araç sınıfı (pozisyon, hız, dönüş mantığı)
│   ├── Direction.java     # Yön enum'u (NORTH, SOUTH, EAST, WEST)
│   ├── LightState.java    # Işık durumu enum'u (RED, YELLOW, GREEN)
│   ├── TrafficLight.java  # Trafik ışığı sınıfı
│   ├── TrafficModel.java  # Ana model sınıfı (simülasyon mantığı)
│   └── TurnDirection.java # Dönüş yönü enum'u (STRAIGHT, LEFT, RIGHT)
├── view/                   # View Katmanı
│   └── TrafficView.java   # Görsel arayüz (Canvas, kontrol paneli)
└── controller/             # Controller Katmanı
    └── TrafficController.java # Kullanıcı etkileşimi ve oyun döngüsü
```

### Model Katmanı

| Sınıf | Açıklama |
|-------|----------|
| `Car` | Araç pozisyonu, hızı, dönüş yönü ve Bezier eğrisi ile kavisli dönüş mantığını içerir |
| `Direction` | Dört ana yönü (Kuzey, Güney, Doğu, Batı) temsil eder |
| `TrafficLight` | Her yön için ışık durumu ve geri sayım bilgisini tutar |
| `TrafficModel` | Tüm simülasyon mantığını, faz yönetimini ve yeşil ışık süre hesaplamasını içerir |

### View Katmanı

`TrafficView` sınıfı, JavaFX Canvas kullanarak:
- Kavşak ve yolları çizer
- Araçları açılarına göre döndürerek çizer
- Trafik ışıklarını ve geri sayım göstergelerini gösterir
- Kontrol paneli ve bilgi göstergelerini yönetir

### Controller Katmanı

`TrafficController` sınıfı:
- Kullanıcı buton tıklamalarını işler
- AnimationTimer ile oyun döngüsünü yönetir
- Model ve View arasındaki iletişimi sağlar

---

## 🔧 Teknik Detaylar

### Yeşil Işık Süresi Hesaplama

```
Yeşil Süre = (Yön Araç Sayısı / Toplam Araç Sayısı) × Kullanılabilir Süre

Kullanılabilir Süre = 120sn - (4 × 3sn sarı ışık) = 108sn
```

**Sınırlamalar:**
- Minimum yeşil ışık: 10 saniye
- Maksimum yeşil ışık: 60 saniye
- Toplam döngü süresi: 120 saniye (sabit)
- Sarı ışık süresi: 3 saniye

### Kavisli Dönüş Sistemi

Araçlar, kavşakta **Quadratic Bezier eğrisi** kullanarak yumuşak dönüşler yapar:

```
B(t) = (1-t)²P₀ + 2(1-t)tP₁ + t²P₂

P₀: Dönüş başlangıç noktası
P₁: Kontrol noktası (dönüş yarıçapını belirler)
P₂: Dönüş bitiş noktası
```

Araç açısı, eğrinin teğetine göre dinamik olarak hesaplanır.

### Dönüş Oranları

| Dönüş Tipi | Oran |
|------------|------|
| Düz | %50 |
| Sola | %25 |
| Sağa | %25 |

---

## 🚀 Kurulum ve Çalıştırma

### Gereksinimler

- **Java 21** veya üzeri
- **Maven** (veya wrapper kullanılabilir)

### Derleme

```bash
./mvnw clean compile
```

### Çalıştırma

```bash
./mvnw javafx:run
```

---

## 🎮 Kullanım

1. **Araç Sayısı Girişi**: Her yön için araç sayısını manuel girin veya **"🎲 Rastgele Oluştur"** butonuna tıklayın

2. **Simülasyonu Başlatın**: **"▶ Başlat"** butonuna tıklayarak simülasyonu başlatın

3. **Kontrol Butonları**:
   - **▶ Başlat**: Simülasyonu başlatır
   - **⏸ Durdur**: Simülasyonu duraklatır
   - **🔄 Sıfırla**: Simülasyonu sıfırlar

4. **Bilgi Paneli**: Sol panelde yeşil ışık süreleri, aktif yön, geçen araç sayısı ve kalan araç sayısı gösterilir

---

## 📐 Tasarım Kararları

### Şerit Pozisyonları

| Yön | Şerit Merkezi |
|-----|---------------|
| Kuzey (Yukarı) | x = 375 |
| Güney (Aşağı) | x = 425 |
| Doğu (Sol) | y = 375 |
| Batı (Sağ) | y = 425 |

### Araç Özellikleri

- **Boyut**: 25 × 45 piksel
- **Güvenli Mesafe**: 55 piksel
- **Hız**: 80-120 piksel/saniye (rastgele)
- **Dönüşte Hız**: Sağa dönüş %80, sola dönüş %60

---

## 📝 Lisans

Bu proje eğitim amaçlı geliştirilmiştir.

---

## 👨‍💻 Geliştirici Notları

- Sadece **standart Java SE** kütüphaneleri ve **JavaFX** kullanılmıştır
- 3. parti kütüphane kullanılmamıştır
- **Java Collection Framework** (List, Map, EnumMap) etkin şekilde kullanılmıştır
- Kod modüler, okunabilir ve MVC yapısına uygun olarak ayrıştırılmıştır
- **TrafficController**: Class managing user interactions and simulation flow

## How to Run

### Requirements
- Java 21 or higher
- Maven

### Running Steps

1. Compile the project:
```
./mvnw clean compile
```

2. Start the application:
```
./mvnw javafx:run
```

### Usage

1. Enter vehicle counts manually for each direction or click "Random" button to generate random values
2. Click "Start" button to begin the simulation
3. Use "Pause" button to pause and resume the simulation
4. Use "Reset" button to reset the simulation

## Key Design Decisions

### Timing Calculation
- Total cycle time: 120 seconds (fixed)
- Yellow light duration: 3 seconds (fixed)
- Green light duration: Calculated proportionally to vehicle density
- Minimum green time: 10 seconds
- Maximum green time: 60 seconds

### Calculation Formula
1. Total yellow time = 4 directions x 3 seconds = 12 seconds
2. Available green time = 120 - 12 = 108 seconds
3. Green time per direction = (vehicles in direction / total vehicles) x 108 seconds
4. Result is clamped to 10-60 seconds range

### Vehicle Behavior
- Vehicles stop at red light
- Vehicles move at green light
- Vehicles are removed from scene after crossing the intersection
- Vehicles never collide with each other

