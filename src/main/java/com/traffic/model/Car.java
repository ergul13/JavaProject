package com.traffic.model;

import javafx.scene.paint.Color;
import java.util.Random;

public class Car {
    private static final Random random = new Random();

    // === SABİTLER ===
    public static final int STOP_NORTH = 340;
    public static final int STOP_SOUTH = 460;
    public static final int STOP_EAST = 460;
    public static final int STOP_WEST = 340;

    public static final double LANE_NORTH_X = 375;
    public static final double LANE_SOUTH_X = 425;
    public static final double LANE_EAST_Y = 375;
    public static final double LANE_WEST_Y = 425;

    public static final int CAR_WIDTH = 25;
    public static final int CAR_LENGTH = 45;
    public static final double SAFE_DISTANCE = 55; // Orijinal güvenli mesafe

    // === DEĞİŞKENLER ===
    private Direction direction;
    private final Direction originDirection;
    private final TurnDirection turnDirection;

    private double x, y;
    private double speed;
    private final double maxSpeed;
    private boolean hasPassed;
    private boolean hasTurned;
    private boolean isTurning;

    // Pivot Dönüş Değişkenleri
    private double pivotX, pivotY;
    private double turnRadius;
    private double currentTurnAngle;
    private double targetTurnAngle;
    private double turnSpeedDir;

    private double angle;
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
        this.maxSpeed = 80 + random.nextDouble() * 40;
        this.speed = maxSpeed;
        this.hasPassed = false;
        this.hasTurned = false;
        this.isTurning = false;
        this.angle = getInitialAngle(direction);
        this.color = generateRandomColor();
    }

    private double getInitialAngle(Direction dir) {
        switch (dir) {
            case NORTH: return 0;
            case SOUTH: return 180;
            case EAST: return 270;
            case WEST: return 90;
            default: return 0;
        }
    }

    private TurnDirection randomTurnDirection() {
        int rand = random.nextInt(100);
        if (rand < 50) return TurnDirection.STRAIGHT;
        if (rand < 75) return TurnDirection.LEFT;
        return TurnDirection.RIGHT;
    }

    private Color generateRandomColor() {
        Color[] colors = {
                Color.BLUE, Color.RED, Color.ORANGE, Color.PURPLE,
                Color.CYAN, Color.MAGENTA, Color.DEEPPINK, Color.DARKBLUE
        };
        return colors[random.nextInt(colors.length)];
    }

    // === HAREKET MANTIĞI ===
    public void move(double deltaTime) {
        if (isTurning) {
            updateTurnMovement(deltaTime);
            return;
        }

        if (!hasTurned && shouldStartTurn()) {
            setupTurnGeometry();
            isTurning = true;
            updateTurnMovement(deltaTime);
            return;
        }

        moveStraight(deltaTime);
    }

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

    private boolean shouldStartTurn() {
        if (turnDirection == TurnDirection.STRAIGHT) return false;

        double cx = x + CAR_WIDTH / 2.0;
        double cy = y + CAR_LENGTH / 2.0;

        switch (originDirection) {
            case NORTH: return cy >= 350;
            case SOUTH: return cy <= 450;
            case EAST:  return cx <= 450;
            case WEST:  return cx >= 350;
        }
        return false;
    }

    private void setupTurnGeometry() {
        double currentCx = x + CAR_WIDTH / 2.0;
        double currentCy = y + CAR_LENGTH / 2.0;
        double overshoot = 0;

        if (turnDirection == TurnDirection.RIGHT) {
            turnRadius = 25;
            turnSpeedDir = 1;

            switch (originDirection) {
                case NORTH:
                    pivotX = 350; pivotY = 350;
                    overshoot = currentCy - 350;
                    currentTurnAngle = 0;
                    targetTurnAngle = Math.PI / 2;
                    break;
                case SOUTH:
                    pivotX = 450; pivotY = 450;
                    overshoot = 450 - currentCy;
                    currentTurnAngle = Math.PI;
                    targetTurnAngle = 3 * Math.PI / 2;
                    break;
                case EAST:
                    pivotX = 425; pivotY = 375;
                    turnRadius = 25;
                    overshoot = 450 - currentCx;
                    currentTurnAngle = 0;
                    targetTurnAngle = -Math.PI / 2;
                    turnSpeedDir = -1;
                    break;
                case WEST:
                    pivotX = 375; pivotY = 425;
                    overshoot = currentCx - 350;
                    currentTurnAngle = Math.PI;
                    targetTurnAngle = Math.PI / 2;
                    turnSpeedDir = -1;
                    break;
            }
        } else {
            turnRadius = 75;
            turnSpeedDir = -1;

            switch (originDirection) {
                case NORTH:
                    pivotX = 450; pivotY = 350;
                    overshoot = currentCy - 350;
                    currentTurnAngle = Math.PI;
                    targetTurnAngle = Math.PI / 2;
                    break;
                case SOUTH:
                    pivotX = 350; pivotY = 450;
                    overshoot = 450 - currentCy;
                    currentTurnAngle = 2 * Math.PI;
                    targetTurnAngle = 3 * Math.PI / 2;
                    break;
                case EAST:
                    pivotX = 450; pivotY = 450;
                    overshoot = 450 - currentCx;
                    currentTurnAngle = 3 * Math.PI / 2;
                    targetTurnAngle = Math.PI;
                    break;
                case WEST:
                    pivotX = 350; pivotY = 350;
                    overshoot = currentCx - 350;
                    currentTurnAngle = Math.PI / 2;
                    targetTurnAngle = 0;
                    break;
            }
        }

        double angleCorrection = overshoot / turnRadius;
        if (turnSpeedDir > 0) currentTurnAngle += angleCorrection;
        else currentTurnAngle -= angleCorrection;
    }

    private void updateTurnMovement(double deltaTime) {
        // --- DEĞİŞİKLİK BURADA ---
        // Eskiden Right 0.6, Left 0.5 idi. Şimdi neredeyse tam hız yaptım.
        // Right: 0.95 (Çok hafif yavaşlama)
        // Left: 0.85 (Biraz daha kontrollü ama hızlı)
        double turnFactor = (turnDirection == TurnDirection.RIGHT) ? 0.95 : 0.85;
        double turnSpeed = speed * turnFactor;

        double angularSpeed = (turnSpeed / turnRadius) * deltaTime;

        if (turnSpeedDir > 0) { // CW
            currentTurnAngle += angularSpeed;
            if (currentTurnAngle >= targetTurnAngle) finishTurn();
        } else { // CCW
            currentTurnAngle -= angularSpeed;
            if (currentTurnAngle <= targetTurnAngle && targetTurnAngle >= -2 * Math.PI) finishTurn();
            if (targetTurnAngle < 0 && currentTurnAngle <= targetTurnAngle) finishTurn();
        }

        double cx = pivotX + turnRadius * Math.cos(currentTurnAngle);
        double cy = pivotY + turnRadius * Math.sin(currentTurnAngle);

        x = cx - CAR_WIDTH / 2.0;
        y = cy - CAR_LENGTH / 2.0;

        double tangentDegrees = Math.toDegrees(currentTurnAngle);
        if (turnSpeedDir > 0) tangentDegrees += 90;
        else tangentDegrees -= 90;

        angle = 90 - tangentDegrees;
    }

    private void finishTurn() {
        isTurning = false;
        hasTurned = true;

        Direction targetDir = originDirection.getTargetDirection(turnDirection);
        direction = targetDir;
        angle = getInitialAngle(targetDir);

        switch (targetDir) {
            case NORTH: x = LANE_NORTH_X - CAR_WIDTH/2.0; break;
            case SOUTH: x = LANE_SOUTH_X - CAR_WIDTH/2.0; break;
            case EAST:  y = LANE_EAST_Y - CAR_WIDTH/2.0; break;
            case WEST:  y = LANE_WEST_Y - CAR_WIDTH/2.0; break;
        }
    }

    // === YARDIMCI METODLAR ===

    public double getDistanceToStopLine() {
        switch (direction) {
            case NORTH: return STOP_NORTH - y - CAR_LENGTH;
            case SOUTH: return y - STOP_SOUTH;
            case EAST: return x - STOP_EAST;
            case WEST: return STOP_WEST - x - CAR_LENGTH;
        }
        return 0;
    }

    public double getDistanceTo(Car other) {
        double cx1 = x + CAR_WIDTH/2.0; double cy1 = y + CAR_LENGTH/2.0;
        double cx2 = other.x + CAR_WIDTH/2.0; double cy2 = other.y + CAR_LENGTH/2.0;
        return Math.sqrt(Math.pow(cx1 - cx2, 2) + Math.pow(cy1 - cy2, 2));
    }

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

    public boolean isPastStopLine() {
        switch (direction) {
            case NORTH: return y > STOP_NORTH;
            case SOUTH: return y < STOP_SOUTH;
            case EAST: return x < STOP_EAST;
            case WEST: return x > STOP_WEST;
        }
        return false;
    }

    public boolean isInIntersection() {
        return x > 340 && x < 460 && y > 340 && y < 460;
    }

    public void setSpeed(double speed) { this.speed = Math.max(0, Math.min(maxSpeed, speed)); }
    public void setSpeedFactor(double factor) { this.speed = maxSpeed * factor; }
    public void slowDown(double factor) { this.speed *= factor; if (this.speed < 5) this.speed = 0; }
    public void stop() { this.speed = 0; }
    public void accelerate() { this.speed = maxSpeed; }

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
    public static void resetIdCounter() { idCounter = 0; }
}