# Trafik Isigi Kontrol Sistemi

Arac yogunluguna dayali, dinamik yesil isik suresi hesaplayan ve gercekci arac simulasyonu sunan bir trafik kontrol sistemi.

## Proje Hakkinda

Bu proje, dort yonlu bir kavsakta arac yogunlugunu analiz ederek yesil isik surelerini dinamik olarak hesaplayan ve trafik akisini gorsel olarak simule eden bir sistemdir. JavaFX kullanilarak gelistirilmis olup, MVC (Model-View-Controller) mimarisine ve OOP prensiplerine uygun olarak tasarlanmistir.

### Ozellikler

- Dinamik yesil isik suresi hesaplama (yogunluga gore)
- Gercekci arac hareketi ve kavisli donusler (Bezier egrisi)
- Saga ve sola donus destegi (rastgele oran)
- Saat yonunde trafik akisi (Kuzey - Dogu - Guney - Bati)
- Carpismadan korunma sistemi
- Geri sayim gostergesi

## Mimari Yapi

Proje MVC tasarim deseni kullanilarak uc ana katmana ayrilmistir:

```
src/main/java/com/traffic/
├── Launcher.java
├── TrafficLightSystem.java
├── model/
│   ├── Car.java
│   ├── Direction.java
│   ├── LightState.java
│   ├── TrafficLight.java
│   ├── TrafficModel.java
│   └── TurnDirection.java
├── view/
│   └── TrafficView.java
└── controller/
    └── TrafficController.java
```

### Katmanlar

| Katman | Aciklama |
|--------|----------|
| Model | Simulasyon mantigi, arac ve trafik isigi yonetimi |
| View | Gorsel arayuz, Canvas cizimi ve kontrol paneli |
| Controller | Kullanici etkilesimi ve oyun dongusu |

## Teknik Detaylar

### Yesil Isik Suresi Hesaplama

```
Yesil Sure = (Yon Arac Sayisi / Toplam Arac Sayisi) x Kullanilabilir Sure
Kullanilabilir Sure = 120sn - (4 x 3sn sari isik) = 108sn
```

**Sinirlamalar:**
- Minimum yesil isik: 10 saniye
- Maksimum yesil isik: 60 saniye
- Toplam dongu suresi: 120 saniye
- Sari isik suresi: 3 saniye

### Kavisli Donus Sistemi

Araclar kavsakta Quadratic Bezier egrisi kullanarak doner:
- Saga donus: Dar kavis (kucuk yaricap)
- Sola donus: Genis kavis (buyuk yaricap)

### Donus Oranlari

| Donus Tipi | Oran |
|------------|------|
| Duz | %50 |
| Sola | %25 |
| Saga | %25 |

## Kurulum ve Calistirma

### Gereksinimler

- Java 21 veya uzeri
- Maven

### Derleme

```bash
./mvnw clean compile
```

### Calistirma

```bash
./mvnw javafx:run
```

## Kullanim

1. Her yon icin arac sayisini girin veya "Rastgele" butonuna tiklayin
2. "Baslat" butonuna tiklayarak simulasyonu baslatin
3. "Durdur" ile duraklatin, "Sifirla" ile sifirlayin
4. Sol panelde yesil isik sureleri ve simulasyon durumunu takip edin

## Serit Pozisyonlari

| Yon | Serit Merkezi |
|-----|---------------|
| Kuzey | x = 375 |
| Guney | x = 425 |
| Dogu | y = 375 |
| Bati | y = 425 |

## Arac Ozellikleri

- Boyut: 25 x 45 piksel
- Guvenli mesafe: 55 piksel
- Hiz: 80-120 piksel/saniye

## Notlar

- Sadece standart Java SE kutuphaneleri ve JavaFX kullanilmistir
- 3. parti kutuphane kullanilmamistir
- Java Collection Framework (List, Map, EnumMap) kullanilmistir

