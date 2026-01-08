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

    // Şerit merkez pozisyonları (yolun ortasından geçmesi için)
    // Kuzey-Güney şeritleri (dikey yol x=350-450 arası, orta x=400)
    public static final double LANE_NORTH_X = 375;  // Kuzeye giden şerit (sol şerit)
    public static final double LANE_SOUTH_X = 425;  // Güneye giden şerit (sağ şerit)
    // Doğu-Batı şeritleri (yatay yol y=350-450 arası, orta y=400)
    public static final double LANE_EAST_Y = 375;   // Doğuya giden şerit (üst şerit)
    public static final double LANE_WEST_Y = 425;   // Batıya giden şerit (alt şerit)

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
    private boolean isTurning;  // Dönüş işlemi devam ediyor mu
    private double turnProgress; // Dönüş ilerlemesi (0-1 arası)
    private double turnStartX, turnStartY; // Dönüş başlangıç noktası
    private double turnEndX, turnEndY;     // Dönüş bitiş noktası
    private double turnControlX, turnControlY;   // Cubic Bezier kontrol noktası 1 (P1)
    private double turnControl2X, turnControl2Y; // Cubic Bezier kontrol noktası 2 (P2)
    private double angle; // Araç açısı (derece)
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
        this.isTurning = false;
        this.turnProgress = 0;
        this.angle = getInitialAngle(direction);
        this.color = generateRandomColor();
    }

    /**
     * Yöne göre başlangıç açısını döndürür.
     */
    private double getInitialAngle(Direction dir) {
        switch (dir) {
            case NORTH: return 0;    // Yukarı
            case SOUTH: return 180;  // Aşağı
            case EAST: return 270;   // Sola doğru (ekrandan dışarı)
            case WEST: return 90;    // Sağa doğru (ekrana doğru)
            default: return 0;
        }
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
     * Kavşak merkezine geldiğinde kavisli dönüş yapar.
     */
    public void move(double deltaTime) {
        // Dönüş işlemi devam ediyorsa
        if (isTurning) {
            performCurvedTurn(deltaTime);
            return;
        }

        // Dönüş noktasına gelip gelmediğini kontrol et
        if (!hasTurned && shouldStartTurn()) {
            startTurn();
            if (isTurning) {
                performCurvedTurn(deltaTime);
                return;
            }
        }

        // Normal düz hareket
        moveStraight(deltaTime);
    }

    /**
     * Düz hareket yapar.
     */
    private void moveStraight(double deltaTime) {
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
     * Dönüş başlatılmalı mı kontrol eder.
     */
    private boolean shouldStartTurn() {
        if (turnDirection == TurnDirection.STRAIGHT) {
            return false;
        }

        // Kavşağa giriş noktaları (dönüş başlangıç noktaları)
        switch (originDirection) {
            case NORTH:
                return y >= 350 && y <= 360;
            case SOUTH:
                return y <= 450 && y >= 440;
            case EAST:
                return x <= 450 && x >= 440;
            case WEST:
                return x >= 350 && x <= 360;
        }
        return false;
    }

    /**
     * Dönüş işlemini başlatır ve Bezier eğrisi kontrol noktalarını hesaplar.
     */
    private void startTurn() {
        Direction targetDir = originDirection.getTargetDirection(turnDirection);

        turnStartX = x;
        turnStartY = y;
        isTurning = true;
        turnProgress = 0;

        // Dönüş bitiş noktası ve kontrol noktası hesapla
        calculateTurnPath(targetDir);
    }

    /**
     * Dönüş yolunu hesaplar (Cubic Bezier eğrisi kontrol noktaları).
     * Gerçekçi dönüş yolu için her yön kombinasyonu özel olarak hesaplanır.
     */
    private void calculateTurnPath(Direction targetDir) {

        if (turnDirection == TurnDirection.RIGHT) {
            // SAĞA DÖNÜŞ - Kısa yarıçap, kavşağın yakın köşesinden
            calculateRightTurnPath();
        } else {
            // SOLA DÖNÜŞ - Geniş yarıçap, kavşak merkezinden geçerek
            calculateLeftTurnPath();
        }
    }

    /**
     * Sağa dönüş yolunu hesaplar.
     * Sağa dönüş dar bir çeyrek daire çizer.
     * NORTH → WEST, SOUTH → EAST, EAST → NORTH, WEST → SOUTH
     *
     * Çeyrek daire için Cubic Bezier kontrol noktası katsayısı: k ≈ 0.5523
     * Bu, P0'dan P1'e ve P3'ten P2'ye olan mesafenin yarıçap * k olmasını sağlar.
     */
    private void calculateRightTurnPath() {
        // Çeyrek daire için Bezier katsayısı (4/3 * tan(π/8))
        final double K = 0.5522847498;

        switch (originDirection) {
            case NORTH: // Yukarı gidiyor, BATIYA dönecek
                // Başlangıç: (LANE_NORTH_X, turnStartY) - kuzey şeridinde
                // Bitiş: hedef LANE_WEST_Y şeridine, kavşağın sol kenarına
                turnStartX = LANE_NORTH_X - CAR_WIDTH / 2.0;
                turnEndX = STOP_WEST - 10;
                turnEndY = LANE_WEST_Y - CAR_WIDTH / 2.0;

                // Yarıçap: başlangıç ile bitiş arasındaki mesafe
                double radiusNR = Math.abs(turnEndY - turnStartY);

                // P1: Başlangıçtan hareket yönünde (yukarı = +y)
                turnControlX = turnStartX;
                turnControlY = turnStartY + radiusNR * K;
                // P2: Bitişten hareket yönünün tersine (sola = +x)
                turnControl2X = turnEndX + radiusNR * K;
                turnControl2Y = turnEndY;
                break;

            case SOUTH: // Aşağı gidiyor, DOĞUYA dönecek
                turnStartX = LANE_SOUTH_X - CAR_WIDTH / 2.0;
                turnEndX = STOP_EAST + 10;
                turnEndY = LANE_EAST_Y - CAR_WIDTH / 2.0;

                double radiusSR = Math.abs(turnStartY - turnEndY);

                turnControlX = turnStartX;
                turnControlY = turnStartY - radiusSR * K;
                turnControl2X = turnEndX - radiusSR * K;
                turnControl2Y = turnEndY;
                break;

            case EAST: // Sola gidiyor, KUZEYE dönecek
                turnStartY = LANE_EAST_Y - CAR_WIDTH / 2.0;
                turnEndX = LANE_NORTH_X - CAR_WIDTH / 2.0;
                turnEndY = STOP_NORTH - 10;

                double radiusER = Math.abs(turnStartX - turnEndX);

                turnControlX = turnStartX - radiusER * K;
                turnControlY = turnStartY;
                turnControl2X = turnEndX;
                turnControl2Y = turnEndY + radiusER * K;
                break;

            case WEST: // Sağa gidiyor, GÜNEYE dönecek
                turnStartY = LANE_WEST_Y - CAR_WIDTH / 2.0;
                turnEndX = LANE_SOUTH_X - CAR_WIDTH / 2.0;
                turnEndY = STOP_SOUTH + 10;

                double radiusWR = Math.abs(turnEndX - turnStartX);

                turnControlX = turnStartX + radiusWR * K;
                turnControlY = turnStartY;
                turnControl2X = turnEndX;
                turnControl2Y = turnEndY - radiusWR * K;
                break;
        }
    }

    /**
     * Sola dönüş yolunu hesaplar.
     * Sola dönüş geniş bir çeyrek daire çizer (kavşak merkezinden geçer).
     * NORTH → EAST, SOUTH → WEST, EAST → SOUTH, WEST → NORTH
     *
     * Çeyrek daire için Cubic Bezier kontrol noktası katsayısı: k ≈ 0.5523
     */
    private void calculateLeftTurnPath() {
        // Çeyrek daire için Bezier katsayısı
        final double K = 0.5522847498;

        switch (originDirection) {
            case NORTH: // Yukarı gidiyor, DOĞUYA dönecek
                // Başlangıç: kuzey şeridinde
                // Bitiş: doğu şeridinde (LANE_EAST_Y), kavşağın sağ kenarına
                turnStartX = LANE_NORTH_X - CAR_WIDTH / 2.0;
                turnEndX = STOP_EAST + 10;
                turnEndY = LANE_EAST_Y - CAR_WIDTH / 2.0;

                // Yarıçap: başlangıç ile bitiş arasındaki dikey mesafe
                double radiusNL = Math.abs(turnEndY - turnStartY);

                // P1: Başlangıçtan yukarı yönde
                turnControlX = turnStartX;
                turnControlY = turnStartY + radiusNL * K;
                // P2: Bitişten sola yönde (doğuya gidecek, yani -x yönünde gelecek)
                turnControl2X = turnEndX - radiusNL * K;
                turnControl2Y = turnEndY;
                break;

            case SOUTH: // Aşağı gidiyor, BATIYA dönecek
                turnStartX = LANE_SOUTH_X - CAR_WIDTH / 2.0;
                turnEndX = STOP_WEST - 10;
                turnEndY = LANE_WEST_Y - CAR_WIDTH / 2.0;

                double radiusSL = Math.abs(turnStartY - turnEndY);

                turnControlX = turnStartX;
                turnControlY = turnStartY - radiusSL * K;
                turnControl2X = turnEndX + radiusSL * K;
                turnControl2Y = turnEndY;
                break;

            case EAST: // Sola gidiyor, GÜNEYE dönecek
                turnStartY = LANE_EAST_Y - CAR_WIDTH / 2.0;
                turnEndX = LANE_SOUTH_X - CAR_WIDTH / 2.0;
                turnEndY = STOP_SOUTH + 10;

                double radiusEL = Math.abs(turnEndY - turnStartY);

                turnControlX = turnStartX - radiusEL * K;
                turnControlY = turnStartY;
                turnControl2X = turnEndX;
                turnControl2Y = turnEndY - radiusEL * K;
                break;

            case WEST: // Sağa gidiyor, KUZEYE dönecek
                turnStartY = LANE_WEST_Y - CAR_WIDTH / 2.0;
                turnEndX = LANE_NORTH_X - CAR_WIDTH / 2.0;
                turnEndY = STOP_NORTH - 10;

                double radiusWL = Math.abs(turnStartY - turnEndY);

                turnControlX = turnStartX + radiusWL * K;
                turnControlY = turnStartY;
                turnControl2X = turnEndX;
                turnControl2Y = turnEndY + radiusWL * K;
                break;
        }
    }

    /**
     * Kavisli dönüş gerçekleştirir (Cubic Bezier eğrisi).
     */
    private void performCurvedTurn(double deltaTime) {
        // Dönüş hızını hesapla (dönüşte biraz yavaşla)
        double turnSpeedFactor = turnDirection == TurnDirection.LEFT ? 0.5 : 0.7;
        double turnSpeed = speed * turnSpeedFactor;

        // Eğri uzunluğunu tahmin et ve ilerleme miktarını hesapla
        double curveLength = estimateCurveLength();
        double progressIncrement = (turnSpeed * deltaTime) / curveLength;
        turnProgress += progressIncrement;

        if (turnProgress >= 1.0) {
            // Dönüş tamamlandı
            turnProgress = 1.0;
            finishTurn();
            return;
        }

        // Cubic Bezier eğrisi: B(t) = (1-t)³P0 + 3(1-t)²tP1 + 3(1-t)t²P2 + t³P3
        double t = turnProgress;
        double oneMinusT = 1 - t;
        double oneMinusT2 = oneMinusT * oneMinusT;
        double oneMinusT3 = oneMinusT2 * oneMinusT;
        double t2 = t * t;
        double t3 = t2 * t;

        x = oneMinusT3 * turnStartX
            + 3 * oneMinusT2 * t * turnControlX
            + 3 * oneMinusT * t2 * turnControl2X
            + t3 * turnEndX;

        y = oneMinusT3 * turnStartY
            + 3 * oneMinusT2 * t * turnControlY
            + 3 * oneMinusT * t2 * turnControl2Y
            + t3 * turnEndY;

        // Araç açısını hesapla (eğrinin teğet yönü)
        calculateAngleCubic(t);
    }

    /**
     * Cubic Bezier eğrisinin teğetine göre araç açısını hesaplar.
     */
    private void calculateAngleCubic(double t) {
        double oneMinusT = 1 - t;
        double oneMinusT2 = oneMinusT * oneMinusT;
        double t2 = t * t;

        // Cubic Bezier türevi: B'(t) = 3(1-t)²(P1-P0) + 6(1-t)t(P2-P1) + 3t²(P3-P2)
        double dx = 3 * oneMinusT2 * (turnControlX - turnStartX)
                  + 6 * oneMinusT * t * (turnControl2X - turnControlX)
                  + 3 * t2 * (turnEndX - turnControl2X);

        double dy = 3 * oneMinusT2 * (turnControlY - turnStartY)
                  + 6 * oneMinusT * t * (turnControl2Y - turnControlY)
                  + 3 * t2 * (turnEndY - turnControl2Y);

        angle = Math.toDegrees(Math.atan2(dx, dy));
    }

    /**
     * Cubic Bezier eğrisinin yaklaşık uzunluğunu tahmin eder.
     */
    private double estimateCurveLength() {
        double d1 = Math.sqrt(Math.pow(turnControlX - turnStartX, 2) + Math.pow(turnControlY - turnStartY, 2));
        double d2 = Math.sqrt(Math.pow(turnControl2X - turnControlX, 2) + Math.pow(turnControl2Y - turnControlY, 2));
        double d3 = Math.sqrt(Math.pow(turnEndX - turnControl2X, 2) + Math.pow(turnEndY - turnControl2Y, 2));
        return (d1 + d2 + d3) * 0.85; // Eğri düz çizgiden biraz kısa
    }


    /**
     * Dönüşü tamamlar.
     */
    private void finishTurn() {
        Direction targetDir = originDirection.getTargetDirection(turnDirection);
        direction = targetDir;
        hasTurned = true;
        isTurning = false;
        angle = getInitialAngle(targetDir);

        // Pozisyonu şerit merkezine sabitle (CAR_WIDTH offset'i ile)
        switch (targetDir) {
            case NORTH:
                x = LANE_NORTH_X - CAR_WIDTH / 2.0;
                break;
            case SOUTH:
                x = LANE_SOUTH_X - CAR_WIDTH / 2.0;
                break;
            case EAST:
                y = LANE_EAST_Y - CAR_WIDTH / 2.0;
                break;
            case WEST:
                y = LANE_WEST_Y - CAR_WIDTH / 2.0;
                break;
        }
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
     * Hız faktörü ile aracın hızını ayarlar (0.0 = dur, 1.0 = tam hız).
     */
    public void setSpeedFactor(double factor) {
        factor = Math.max(0, Math.min(1.0, factor));
        this.speed = maxSpeed * factor;
    }

    /**
     * Aracı belirli bir oranla yavaşlatır.
     */
    public void slowDown(double factor) {
        this.speed *= factor;
        if (this.speed < 5) {
            this.speed = 0;
        }
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
    public boolean isTurning() { return isTurning; }
    public double getAngle() { return angle; }
    public Color getColor() { return color; }
    public int getId() { return id; }

    public static void resetIdCounter() {
        idCounter = 0;
    }
}
