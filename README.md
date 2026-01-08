# Trafik Isigi Kontrol Sistemi - Arac Yogunluguna Dayali

## Proje Ozeti

Bu proje, dort yonlu bir kavsak icin arac yogunluguna gore dinamik olarak calisan bir trafik isigi simülasyonu sunmaktadir. JavaFX kullanilarak gelistirilmis olup, MVC (Model-View-Controller) mimarisine siki sikiya baglidir.

## Mimari Yapi

Proje uc ana katmandan olusmaktadir:

### Model Katmani
- **Direction**: Dort yonu temsil eden enum (NORTH, SOUTH, EAST, WEST)
- **LightColor**: Isik renklerini temsil eden enum (RED, YELLOW, GREEN)
- **SignalPhase**: Sinyal fazlarini temsil eden enum
- **TrafficLight**: Trafik isiginin durumunu ve kalan suresini tutan sinif
- **Vehicle**: Araclarin konumunu ve durumunu tutan sinif
- **TrafficState**: Tum simülasyon durumunu yoneten sinif
- **TimingCalculator**: Yesil isik surelerini hesaplayan sinif

### View Katmani
- **TrafficSimulationView**: Tum gorsel bilesenleri iceren JavaFX arayuzu

### Controller Katmani
- **TrafficController**: Kullanici etkilesimlerini ve simülasyon akisini yoneten sinif

## Nasil Calistirilir

### Gereksinimler
- Java 21 veya ustu
- Maven

### Calistirma Adimlari

1. Projeyi derleyin:
```
./mvnw clean compile
```

2. Uygulamayi baslatin:
```
./mvnw javafx:run
```

### Kullanim

1. Her yon icin arac sayisini manuel olarak girin veya "Random" butonuna tiklayarak rastgele degerler uretin
2. "Start" butonuna tiklayarak simülasyonu baslatin
3. "Pause" butonu ile simülasyonu durdurup devam ettirebilirsiniz
4. "Reset" butonu ile simülasyonu sifirlayin

## Temel Tasarim Kararlari

### Zamanlama Hesaplamasi
- Toplam cevrim suresi: 120 saniye (sabit)
- Sari isik suresi: 3 saniye (sabit)
- Yesil isik suresi: Arac yogunluguna orantili olarak hesaplanir
- Minimum yesil sure: 10 saniye
- Maksimum yesil sure: 60 saniye

### Hesaplama Formulu
1. Toplam sari suresi = 4 yon x 3 saniye = 12 saniye
2. Kullanilabilir yesil sure = 120 - 12 = 108 saniye
3. Her yon icin yesil sure = (yondeki arac / toplam arac) x 108 saniye
4. Sonuc 10-60 saniye araligina sinirlandirilir

### Arac Davranisi
- Araclar kirmizi isikta durur
- Yesil isikta hareket eder
- Kavsagi gectikten sonra sahneden silinir
- Araclar birbirine carpmaz

---

# Traffic Light Control System - Based on Vehicle Density

## Project Summary

This project provides a traffic light simulation for a four-way intersection that dynamically operates based on vehicle density. Developed using JavaFX, it strictly adheres to the MVC (Model-View-Controller) architecture.

## Architecture

The project consists of three main layers:

### Model Layer
- **Direction**: Enum representing four directions (NORTH, SOUTH, EAST, WEST)
- **LightColor**: Enum representing light colors (RED, YELLOW, GREEN)
- **SignalPhase**: Enum representing signal phases
- **TrafficLight**: Class holding traffic light state and remaining time
- **Vehicle**: Class holding vehicle position and state
- **TrafficState**: Class managing the entire simulation state
- **TimingCalculator**: Class calculating green light durations

### View Layer
- **TrafficSimulationView**: JavaFX interface containing all visual components

### Controller Layer
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

