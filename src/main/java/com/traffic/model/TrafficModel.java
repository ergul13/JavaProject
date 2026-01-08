package com.traffic.model;

import java.util.*;

/**
 * MVC Model sinifi - Simulasyon mantigi ve veri yonetimi.
 */
public class TrafficModel {
    public static final int TOTAL_CYCLE_TIME = 120;
    public static final int YELLOW_DURATION = 1;
    public static final int MIN_GREEN_TIME = 10;
    public static final int MAX_GREEN_TIME = 60;

    private final Map<Direction, Integer> vehicleCounts;
    private final Map<Direction, TrafficLight> trafficLights;
    private final List<Car> cars;

    private boolean isRunning;
    private boolean isDataLoaded;
    private boolean isCycleComplete;

    private int currentPhaseIndex;
    private LightState currentLightState;
    private double timeInCurrentState;
    private double totalElapsedTime;
    private int totalCarsPassed;

    public TrafficModel() {
        this.vehicleCounts = new EnumMap<>(Direction.class);
        this.trafficLights = new EnumMap<>(Direction.class);
        this.cars = new ArrayList<>();

        for (Direction dir : Direction.values()) {
            trafficLights.put(dir, new TrafficLight(dir));
            vehicleCounts.put(dir, 0);
        }

        resetAll();
    }

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

    private void switchPhase() {
        if (currentLightState == LightState.GREEN) {
            currentLightState = LightState.YELLOW;
            timeInCurrentState = YELLOW_DURATION;
        } else if (currentLightState == LightState.YELLOW) {
            currentPhaseIndex = (currentPhaseIndex + 1) % 4;

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

    private void updateCars(double deltaTime) {
        Map<Direction, List<Car>> carsByDirection = new EnumMap<>(Direction.class);
        for (Direction dir : Direction.values()) {
            carsByDirection.put(dir, new ArrayList<>());
        }

        for (Car car : cars) {
            if (!car.hasPassed()) {
                carsByDirection.get(car.getDirection()).add(car);
            }
        }

        for (Direction dir : Direction.values()) {
            List<Car> dirCars = carsByDirection.get(dir);

            sortCarsByPosition(dirCars, dir);

            for (int i = 0; i < dirCars.size(); i++) {
                Car car = dirCars.get(i);
                TrafficLight light = trafficLights.get(car.getOriginDirection());

                double speedFactor = 1.0;

                if (i > 0) {
                    Car carAhead = dirCars.get(i - 1);
                    double distance = car.getDistanceToCarAhead(carAhead);
                    double safeDistance = Car.SAFE_DISTANCE;
                    double criticalDistance = Car.CAR_LENGTH + 5;

                    if (distance < criticalDistance) {
                        speedFactor = 0.0;
                    } else if (distance < safeDistance) {
                        speedFactor = (distance - criticalDistance) / (safeDistance - criticalDistance);
                        speedFactor = Math.max(0.1, speedFactor);
                    }
                }

                boolean isInIntersectionArea = car.isInIntersection() || car.isTurning() || car.hasTurned();
                boolean hasCrossedStopLine = car.isPastStopLine();

                if (!isInIntersectionArea && !hasCrossedStopLine) {
                    double distToStop = car.getDistanceToStopLine();

                    if (distToStop > 0 && distToStop < 50) {
                        if (!light.allowsPass()) {
                            double stopFactor = distToStop / 50.0;
                            speedFactor = Math.min(speedFactor, stopFactor);

                            if (distToStop < 10) {
                                speedFactor = 0.0;
                            }
                        }
                    }
                }

                if (speedFactor <= 0.01) {
                    car.stop();
                } else {
                    car.setSpeedFactor(speedFactor);
                    car.move(deltaTime);
                }

                if (car.hasPassed()) {
                    totalCarsPassed++;
                }
            }
        }

        checkIntersectionCollisions(carsByDirection);
    }

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
