package com.traffic.model;

import java.util.*;

/**
 * Trafik simülasyonunun ana model sınıfı.
 * Tüm araçlar, trafik ışıkları ve zamanlama mantığını içerir.
 */
public class TrafficModel {
    // Sabitler
    public static final int TOTAL_CYCLE_TIME = 120;  // Toplam döngü süresi (saniye)
    public static final int YELLOW_DURATION = 3;     // Sarı ışık süresi (saniye)
    public static final int MIN_GREEN_TIME = 10;     // Minimum yeşil ışık süresi
    public static final int MAX_GREEN_TIME = 60;     // Maksimum yeşil ışık süresi

    // Araç yoğunlukları (her yön için)
    private final Map<Direction, Integer> vehicleCounts;

    // Trafik ışıkları (her yön için)
    private final Map<Direction, TrafficLight> trafficLights;

    // Araçlar listesi
    private final List<Car> cars;

    // Simülasyon durumu
    private boolean isRunning;
    private boolean isDataLoaded;
    private boolean isCycleComplete;

    // Faz yönetimi
    private int currentPhaseIndex;
    private LightState currentLightState;
    private double timeInCurrentState;
    private double totalElapsedTime;

    // İstatistikler
    private int totalCarsPassed;

    public TrafficModel() {
        this.vehicleCounts = new EnumMap<>(Direction.class);
        this.trafficLights = new EnumMap<>(Direction.class);
        this.cars = new ArrayList<>();

        // Trafik ışıklarını başlat
        for (Direction dir : Direction.values()) {
            trafficLights.put(dir, new TrafficLight(dir));
            vehicleCounts.put(dir, 0);
        }

        resetAll();
    }

    /**
     * Araç yoğunluklarını ayarlar ve zamanlamaları hesaplar.
     */
    public void setVehicleCounts(int north, int south, int east, int west) {
        vehicleCounts.put(Direction.NORTH, north);
        vehicleCounts.put(Direction.SOUTH, south);
        vehicleCounts.put(Direction.EAST, east);
        vehicleCounts.put(Direction.WEST, west);

        calculateGreenDurations();
        spawnCars();

        isDataLoaded = true;
        isCycleComplete = false;
        initializeFirstPhase();
    }

    /**
     * Yoğunluğa göre yeşil ışık sürelerini hesaplar.
     * Formül: yeşil_süre = (yön_araç_sayısı / toplam_araç) * kullanılabilir_süre
     */
    private void calculateGreenDurations() {
        int totalVehicles = vehicleCounts.values().stream().mapToInt(Integer::intValue).sum();

        // Toplam kullanılabilir yeşil ışık süresi (sarı ışıklar çıkarıldıktan sonra)
        double availableGreenTime = TOTAL_CYCLE_TIME - (4 * YELLOW_DURATION);

        if (totalVehicles == 0) {
            // Araç yoksa eşit dağıt
            double equalTime = availableGreenTime / 4.0;
            for (Direction dir : Direction.values()) {
                trafficLights.get(dir).setGreenDuration(equalTime);
            }
        } else {
            double totalCalculated = 0;
            Map<Direction, Double> rawDurations = new EnumMap<>(Direction.class);

            // İlk hesaplama - orana göre
            for (Direction dir : Direction.values()) {
                double ratio = (double) vehicleCounts.get(dir) / totalVehicles;
                double rawDuration = ratio * availableGreenTime;
                rawDurations.put(dir, rawDuration);
            }

            // Min-Max sınırlarını uygula
            for (Direction dir : Direction.values()) {
                double duration = rawDurations.get(dir);
                duration = Math.max(MIN_GREEN_TIME, Math.min(MAX_GREEN_TIME, duration));
                trafficLights.get(dir).setGreenDuration(duration);
                totalCalculated += duration;
            }

            // Toplam süreyi kullanılabilir süreye normalize et
            if (totalCalculated != availableGreenTime && totalCalculated > 0) {
                double scaleFactor = availableGreenTime / totalCalculated;
                for (Direction dir : Direction.values()) {
                    double newDuration = trafficLights.get(dir).getGreenDuration() * scaleFactor;
                    newDuration = Math.max(MIN_GREEN_TIME, Math.min(MAX_GREEN_TIME, newDuration));
                    trafficLights.get(dir).setGreenDuration(newDuration);
                }
            }
        }
    }

    /**
     * Araçları başlangıç pozisyonlarında oluşturur.
     */
    private void spawnCars() {
        cars.clear();
        Car.resetIdCounter();

        int northCount = vehicleCounts.get(Direction.NORTH);
        int southCount = vehicleCounts.get(Direction.SOUTH);
        int eastCount = vehicleCounts.get(Direction.EAST);
        int westCount = vehicleCounts.get(Direction.WEST);

        // Kuzeyden gelen araçlar (aşağıdan yukarı hareket)
        // Şerit merkezi: Car.LANE_NORTH_X
        for (int i = 0; i < northCount; i++) {
            double x = Car.LANE_NORTH_X - Car.CAR_WIDTH / 2.0;
            double y = -60 - (i * Car.SAFE_DISTANCE);
            cars.add(new Car(Direction.NORTH, x, y));
        }

        // Güneyden gelen araçlar (yukarıdan aşağı hareket)
        // Şerit merkezi: Car.LANE_SOUTH_X
        for (int i = 0; i < southCount; i++) {
            double x = Car.LANE_SOUTH_X - Car.CAR_WIDTH / 2.0;
            double y = 860 + (i * Car.SAFE_DISTANCE);
            cars.add(new Car(Direction.SOUTH, x, y));
        }

        // Doğudan gelen araçlar (sağdan sola hareket)
        // Şerit merkezi: Car.LANE_EAST_Y
        for (int i = 0; i < eastCount; i++) {
            double x = 860 + (i * Car.SAFE_DISTANCE);
            double y = Car.LANE_EAST_Y - Car.CAR_WIDTH / 2.0;
            cars.add(new Car(Direction.EAST, x, y));
        }

        // Batıdan gelen araçlar (soldan sağa hareket)
        // Şerit merkezi: Car.LANE_WEST_Y
        for (int i = 0; i < westCount; i++) {
            double x = -60 - (i * Car.SAFE_DISTANCE);
            double y = Car.LANE_WEST_Y - Car.CAR_WIDTH / 2.0;
            cars.add(new Car(Direction.WEST, x, y));
        }
    }

    /**
     * İlk fazı başlatır.
     */
    private void initializeFirstPhase() {
        currentPhaseIndex = 0;
        currentLightState = LightState.GREEN;
        totalElapsedTime = 0;
        totalCarsPassed = 0;

        Direction currentDir = Direction.fromIndex(currentPhaseIndex);
        timeInCurrentState = trafficLights.get(currentDir).getGreenDuration();

        // Tüm ışıkları güncelle
        updateTrafficLightStates();
    }

    /**
     * Trafik ışıklarının durumlarını günceller.
     */
    private void updateTrafficLightStates() {
        for (Direction dir : Direction.values()) {
            TrafficLight light = trafficLights.get(dir);

            if (dir.getIndex() == currentPhaseIndex) {
                light.setState(currentLightState);
                light.setCountdown(timeInCurrentState);
            } else {
                light.setState(LightState.RED);
                // Diğer yönler için kalan süreyi hesapla
                double remaining = calculateRemainingTime(dir);
                light.setCountdown(remaining);
            }
        }
    }

    /**
     * Belirli bir yön için yeşil ışığa kadar kalan süreyi hesaplar.
     */
    private double calculateRemainingTime(Direction dir) {
        if (dir.getIndex() == currentPhaseIndex) {
            return timeInCurrentState;
        }

        double remaining = timeInCurrentState;

        // Sarı ışık süresi ekle (mevcut faz yeşil ise)
        if (currentLightState == LightState.GREEN) {
            remaining += YELLOW_DURATION;
        }

        // Aradaki fazların sürelerini ekle
        int nextPhase = (currentPhaseIndex + 1) % 4;
        while (nextPhase != dir.getIndex()) {
            Direction nextDir = Direction.fromIndex(nextPhase);
            remaining += trafficLights.get(nextDir).getGreenDuration() + YELLOW_DURATION;
            nextPhase = (nextPhase + 1) % 4;
        }

        return remaining;
    }

    /**
     * Simülasyonu günceller.
     */
    public void update(double deltaTime) {
        if (!isRunning || !isDataLoaded || isCycleComplete) return;

        totalElapsedTime += deltaTime;

        // Faz zamanlamasını güncelle
        timeInCurrentState -= deltaTime;

        if (timeInCurrentState <= 0) {
            switchPhase();
        }

        updateTrafficLightStates();

        // Araçları güncelle
        updateCars(deltaTime);

        // Döngü tamamlandı mı kontrol et
        checkCycleComplete();
    }

    /**
     * Bir sonraki faza geçer.
     */
    private void switchPhase() {
        if (currentLightState == LightState.GREEN) {
            // Yeşilden sarıya
            currentLightState = LightState.YELLOW;
            timeInCurrentState = YELLOW_DURATION;
        } else if (currentLightState == LightState.YELLOW) {
            // Sarıdan kırmızıya, sonraki yön yeşile
            currentPhaseIndex = (currentPhaseIndex + 1) % 4;

            // Tüm fazlar tamamlandıysa döngü bitti
            if (currentPhaseIndex == 0 && totalElapsedTime > YELLOW_DURATION) {
                isCycleComplete = true;
                isRunning = false;
                return;
            }

            currentLightState = LightState.GREEN;
            Direction currentDir = Direction.fromIndex(currentPhaseIndex);
            timeInCurrentState = trafficLights.get(currentDir).getGreenDuration();
        }
    }

    /**
     * Araçları günceller - hareket, çarpışma önleme ve kavşak güvenliği.
     */
    private void updateCars(double deltaTime) {
        // Araçları mevcut yöne göre grupla (dönüş yapmış olanlar yeni yönlerinde)
        Map<Direction, List<Car>> carsByDirection = new EnumMap<>(Direction.class);
        for (Direction dir : Direction.values()) {
            carsByDirection.put(dir, new ArrayList<>());
        }

        for (Car car : cars) {
            if (!car.hasPassed()) {
                carsByDirection.get(car.getDirection()).add(car);
            }
        }

        // Her yön için araçları işle
        for (Direction dir : Direction.values()) {
            List<Car> dirCars = carsByDirection.get(dir);

            // Araçları sırala (kavşağa en yakın önde)
            sortCarsByPosition(dirCars, dir);

            for (int i = 0; i < dirCars.size(); i++) {
                Car car = dirCars.get(i);

                // Işık kontrolü için orijinal yönü kullan
                TrafficLight light = trafficLights.get(car.getOriginDirection());

                // === ÇARPIŞMA ÖNLEME ===
                double speedFactor = 1.0; // 1.0 = tam hız, 0.0 = dur

                // 1. Öndeki araca olan mesafeyi kontrol et
                if (i > 0) {
                    Car carAhead = dirCars.get(i - 1);
                    double distance = car.getDistanceToCarAhead(carAhead);

                    // Güvenli takip mesafesi hesapla
                    double safeDistance = Car.SAFE_DISTANCE;
                    double criticalDistance = Car.CAR_LENGTH + 5; // Minimum mesafe

                    if (distance < criticalDistance) {
                        // Çok yakın - tamamen dur
                        speedFactor = 0.0;
                    } else if (distance < safeDistance) {
                        // Yaklaşıyor - kademeli yavaşla
                        speedFactor = (distance - criticalDistance) / (safeDistance - criticalDistance);
                        speedFactor = Math.max(0.1, speedFactor); // En az %10 hız
                    }
                }

                // === KAVŞAK GÜVENLİĞİ ===
                // Kavşak içindeki veya dönüş yapan araçlar ASLA durmamalı
                boolean isInIntersectionArea = car.isInIntersection() || car.isTurning() || car.hasTurned();
                boolean hasCrossedStopLine = car.isPastStopLine();

                // === IŞIK KONTROLÜ ===
                // Sadece kavşağa GİRMEMİŞ araçlar ışığa uyar
                if (!isInIntersectionArea && !hasCrossedStopLine) {
                    double distToStop = car.getDistanceToStopLine();

                    // Durak çizgisine yaklaşıyorsa
                    if (distToStop > 0 && distToStop < 50) {
                        if (!light.allowsPass()) {
                            // Işık kırmızı veya sarı - yavaşla ve dur
                            double stopFactor = distToStop / 50.0;
                            speedFactor = Math.min(speedFactor, stopFactor);

                            // Çizgiye çok yakınsa tamamen dur
                            if (distToStop < 10) {
                                speedFactor = 0.0;
                            }
                        }
                    }
                }

                // === HAREKET UYGULA ===
                if (speedFactor <= 0.01) {
                    car.stop();
                } else {
                    car.setSpeedFactor(speedFactor);
                    car.move(deltaTime);
                }

                // === GEÇİŞ SAYACI ===
                if (car.hasPassed()) {
                    totalCarsPassed++;
                }
            }
        }

        // Farklı yönlerden gelen araçlar arasında kavşak içi çarpışma kontrolü
        checkIntersectionCollisions(carsByDirection);
    }

    /**
     * Kavşak içinde farklı yönlerden gelen araçlar arasındaki çarpışmaları kontrol eder.
     */
    private void checkIntersectionCollisions(Map<Direction, List<Car>> carsByDirection) {
        List<Car> carsInIntersection = new ArrayList<>();

        // Kavşak içindeki tüm araçları bul
        for (List<Car> dirCars : carsByDirection.values()) {
            for (Car car : dirCars) {
                if (car.isInIntersection() || car.isTurning()) {
                    carsInIntersection.add(car);
                }
            }
        }

        // Kavşak içindeki araçlar arasında mesafe kontrolü
        for (int i = 0; i < carsInIntersection.size(); i++) {
            Car car1 = carsInIntersection.get(i);
            for (int j = i + 1; j < carsInIntersection.size(); j++) {
                Car car2 = carsInIntersection.get(j);

                // Aynı yönde değillerse çarpışma kontrolü yap
                if (car1.getDirection() != car2.getDirection()) {
                    double distance = car1.getDistanceTo(car2);
                    if (distance < Car.CAR_LENGTH * 1.5) {
                        // Çok yakınlar - daha yeni giren yavaşlasın
                        // (Basit çözüm: ikisi de biraz yavaşlasın)
                        car1.slowDown(0.5);
                        car2.slowDown(0.5);
                    }
                }
            }
        }
    }

    /**
     * Araçları kavşağa olan mesafeye göre sıralar.
     */
    private void sortCarsByPosition(List<Car> cars, Direction dir) {
        cars.sort((a, b) -> {
            switch (dir) {
                case NORTH: return Double.compare(b.getY(), a.getY()); // Büyük y önce
                case SOUTH: return Double.compare(a.getY(), b.getY()); // Küçük y önce
                case EAST: return Double.compare(a.getX(), b.getX()); // Küçük x önce
                case WEST: return Double.compare(b.getX(), a.getX()); // Büyük x önce
            }
            return 0;
        });
    }

    /**
     * Döngü tamamlandı mı kontrol eder.
     */
    private void checkCycleComplete() {
        // Tüm araçlar geçti mi veya döngü süresi doldu mu
        boolean allCarsPassed = cars.stream().allMatch(Car::hasPassed);

        if (allCarsPassed || (isCycleComplete && totalElapsedTime >= TOTAL_CYCLE_TIME)) {
            isCycleComplete = true;
            isRunning = false;
        }
    }

    /**
     * Simülasyonu sıfırlar.
     */
    public void resetAll() {
        isRunning = false;
        isDataLoaded = false;
        isCycleComplete = false;
        currentPhaseIndex = 0;
        currentLightState = LightState.RED;
        timeInCurrentState = 0;
        totalElapsedTime = 0;
        totalCarsPassed = 0;

        cars.clear();

        for (Direction dir : Direction.values()) {
            vehicleCounts.put(dir, 0);
            trafficLights.get(dir).reset();
        }
    }

    /**
     * Sadece simülasyon durumunu sıfırlar (yoğunluk değerleri korunur).
     */
    public void resetSimulationState() {
        if (isDataLoaded) {
            isCycleComplete = false;
            spawnCars();
            initializeFirstPhase();
            isRunning = false;
        }
    }

    // Getter ve Setter metodları
    public boolean isRunning() { return isRunning; }
    public void setRunning(boolean running) { this.isRunning = running; }
    public boolean isDataLoaded() { return isDataLoaded; }
    public boolean isCycleComplete() { return isCycleComplete; }

    public List<Car> getCars() { return Collections.unmodifiableList(cars); }

    public int getCurrentPhaseIndex() { return currentPhaseIndex; }
    public Direction getCurrentDirection() { return Direction.fromIndex(currentPhaseIndex); }
    public LightState getCurrentLightState() { return currentLightState; }
    public double getTimeInCurrentState() { return timeInCurrentState; }
    public double getTotalElapsedTime() { return totalElapsedTime; }

    public TrafficLight getTrafficLight(Direction dir) { return trafficLights.get(dir); }
    public Map<Direction, TrafficLight> getAllTrafficLights() { return Collections.unmodifiableMap(trafficLights); }

    public int getVehicleCount(Direction dir) { return vehicleCounts.get(dir); }
    public double getGreenDuration(Direction dir) { return trafficLights.get(dir).getGreenDuration(); }

    public int getTotalCarsPassed() { return totalCarsPassed; }
    public int getTotalCars() { return cars.size(); }
    public int getRemainingCars() { return (int) cars.stream().filter(c -> !c.hasPassed()).count(); }
}
