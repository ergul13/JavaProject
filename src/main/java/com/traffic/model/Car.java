package com.traffic.model;

import javafx.scene.paint.Color;
import java.util.Random;


public class Car {
    private static final Random random = new Random();

    // Durak çizgisi konumları (kavşak sınırları)
    public static final int STOP_NORTH = 340;
    public static final int STOP_SOUTH = 460;
    public static final int STOP_EAST = 460;
    public static final int STOP_WEST = 340;


    // Araç boyutları
    public static final int CAR_WIDTH = 25;
    public static final int CAR_LENGTH = 45;
    public static final double SAFE_DISTANCE = 55; // Araçlar arası güvenli mesafe

    private Direction direction;       // Mevcut hareket yönü
    private final Direction originDirection;  // Başlangıç yönü
    private final TurnDirection turnDirection; // Dönüş yönü (düz, sol, sağ)
    private double x, y;
    private double speed;
    private final double maxSpeed;
    private boolean hasPassed;
    private boolean hasTurned;  // Dönüş yapıldı mı
    private final Color color;
    private final int id;
    private static int idCounter = 0;

    public Car(Direction direction, double x, double y) {
        this.id = idCounter++;
        this.direction = direction;
        this.originDirection = direction;
        this.turnDirection = randomTurnDirection();
        this.x = x;
        this.y = y;
        this.maxSpeed = 80 + random.nextDouble() * 40; // 80-120 arası rastgele hız
        this.speed = maxSpeed;
        this.hasPassed = false;
        this.hasTurned = false;
        this.color = generateRandomColor();
    }

    /**
     * Rastgele dönüş yönü belirler.
     * %50 düz, %25 sol, %25 sağ
     */
    private TurnDirection randomTurnDirection() {
        int rand = random.nextInt(100);
        if (rand < 50) {
            return TurnDirection.STRAIGHT;
        } else if (rand < 75) {
            return TurnDirection.LEFT;
        } else {
            return TurnDirection.RIGHT;
        }
    }

    /**
     * Araç için rastgele renk oluşturur.
     */
    private Color generateRandomColor() {
        Color[] colors = {
            Color.BLUE, Color.RED, Color.ORANGE, Color.PURPLE,
            Color.CYAN, Color.MAGENTA, Color.DEEPPINK, Color.DARKBLUE,
            Color.DARKCYAN, Color.DARKMAGENTA, Color.CRIMSON, Color.CORAL
        };
        return colors[random.nextInt(colors.length)];
    }

    /**
     * Aracı delta süre kadar hareket ettirir.
     * Kavşak merkezine geldiğinde dönüş yapar.
     */
    public void move(double deltaTime) {
        // Kavşak içindeyken ve henüz dönmemişse dönüş kontrolü yap
        if (!hasTurned && isAtTurnPoint()) {
            performTurn();
        }

        // Mevcut yöne göre hareket et
        switch (direction) {
            case NORTH:
                y += speed * deltaTime;
                if (y > 850) hasPassed = true;
                break;
            case SOUTH:
                y -= speed * deltaTime;
                if (y < -50) hasPassed = true;
                break;
            case EAST:
                x -= speed * deltaTime;
                if (x < -50) hasPassed = true;
                break;
            case WEST:
                x += speed * deltaTime;
                if (x > 850) hasPassed = true;
                break;
        }
    }

    /**
     * Dönüş noktasına gelip gelmediğini kontrol eder.
     */
    private boolean isAtTurnPoint() {
        if (turnDirection == TurnDirection.STRAIGHT) {
            return false; // Düz gidenlerin dönmesine gerek yok
        }

        switch (originDirection) {
            case NORTH:
                // Kuzeyden gelen, kavşak merkezine yaklaşınca döner
                if (turnDirection == TurnDirection.RIGHT) {
                    return y >= 380 && y <= 420;
                } else { // LEFT
                    return y >= 400 && y <= 440;
                }
            case SOUTH:
                if (turnDirection == TurnDirection.RIGHT) {
                    return y <= 420 && y >= 380;
                } else {
                    return y <= 400 && y >= 360;
                }
            case EAST:
                if (turnDirection == TurnDirection.RIGHT) {
                    return x <= 420 && x >= 380;
                } else {
                    return x <= 400 && x >= 360;
                }
            case WEST:
                if (turnDirection == TurnDirection.RIGHT) {
                    return x >= 380 && x <= 420;
                } else {
                    return x >= 400 && x <= 440;
                }
        }
        return false;
    }

    /**
     * Dönüş gerçekleştirir - yönü ve pozisyonu günceller.
     */
    private void performTurn() {
        Direction newDirection = originDirection.getTargetDirection(turnDirection);

        // Yeni yöne göre şerit pozisyonuna yerleştir
        switch (newDirection) {
            case NORTH:
                x = 387; // Kuzey yönü sol şerit
                break;
            case SOUTH:
                x = 412; // Güney yönü sağ şerit
                break;
            case EAST:
                y = 387; // Doğu yönü üst şerit
                break;
            case WEST:
                y = 412; // Batı yönü alt şerit
                break;
        }

        this.direction = newDirection;
        this.hasTurned = true;
    }

    /**
     * Durak çizgisine olan mesafeyi hesaplar.
     */
    public double getDistanceToStopLine() {
        switch (direction) {
            case NORTH: return STOP_NORTH - y - CAR_LENGTH;
            case SOUTH: return y - STOP_SOUTH;
            case EAST: return x - STOP_EAST;
            case WEST: return STOP_WEST - x - CAR_LENGTH;
        }
        return 0;
    }

    /**
     * Başka bir araca olan mesafeyi hesaplar.
     */
    public double getDistanceTo(Car other) {
        return Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2));
    }

    /**
     * Aynı şeritteki öndeki araca olan mesafeyi hesaplar.
     */
    public double getDistanceToCarAhead(Car other) {
        if (other.direction != this.direction) return Double.MAX_VALUE;

        switch (direction) {
            case NORTH: return other.y - (this.y + CAR_LENGTH);
            case SOUTH: return this.y - (other.y + CAR_LENGTH);
            case EAST: return this.x - (other.x + CAR_LENGTH);
            case WEST: return other.x - (this.x + CAR_LENGTH);
        }
        return Double.MAX_VALUE;
    }

    /**
     * Aracın durak çizgisini geçip geçmediğini kontrol eder.
     */
    public boolean isPastStopLine() {
        switch (direction) {
            case NORTH: return y > STOP_NORTH;
            case SOUTH: return y < STOP_SOUTH;
            case EAST: return x < STOP_EAST;
            case WEST: return x > STOP_WEST;
        }
        return false;
    }

    /**
     * Aracın kavşak içinde olup olmadığını kontrol eder.
     */
    public boolean isInIntersection() {
        return x > 340 && x < 460 && y > 340 && y < 460;
    }

    /**
     * Aracın hızını ayarlar, 0 ile maxSpeed arasında sınırlar.
     */
    public void setSpeed(double speed) {
        this.speed = Math.max(0, Math.min(maxSpeed, speed));
    }

    /**
     * Aracı durdurur.
     */
    public void stop() {
        this.speed = 0;
    }

    /**
     * Aracı maksimum hıza getirir.
     */
    public void accelerate() {
        this.speed = maxSpeed;
    }

    // Getter metodları
    public Direction getDirection() { return direction; }
    public Direction getOriginDirection() { return originDirection; }
    public TurnDirection getTurnDirection() { return turnDirection; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getSpeed() { return speed; }
    public double getMaxSpeed() { return maxSpeed; }
    public boolean hasPassed() { return hasPassed; }
    public boolean hasTurned() { return hasTurned; }
    public Color getColor() { return color; }
    public int getId() { return id; }

    public static void resetIdCounter() {
        idCounter = 0;
    }
}
