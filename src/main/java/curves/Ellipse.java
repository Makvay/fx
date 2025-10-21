package curves;

import javafx.geometry.Point3D;

import java.util.ArrayList;
import java.util.List;

public class Ellipse extends Curve3D {
    private final double radiusX, radiusY;
    private final double rotation; // Угол поворота по оси X в радианах

    public Ellipse(double radiusX, double radiusY) {
        this(radiusX, radiusY, 0.0); // По умолчанию без поворота
    }

    // Конструктор с поворотом
    private Ellipse(double radiusX, double radiusY, double rotation) {
        if (radiusX <= 0 || radiusY <= 0) throw new IllegalArgumentException("Radii must be positive");
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.rotation = rotation;
    }

    @Override
    public Point3D getPoint(double t) {
        // Базовая точка эллипса в плоскости XY
        double x = radiusX * Math.cos(t);
        double y = radiusY * Math.sin(t);
        double z = 0;

        // Применяем поворот по оси X
        if (rotation != 0) {
            // Матрица поворота вокруг оси X:
            // x' = x
            // y' = y * cos(angle) - z * sin(angle)
            // z' = y * sin(angle) + z * cos(angle)
            double cosX = Math.cos(rotation);
            double sinX = Math.sin(rotation);
            double newY = y * cosX - z * sinX;
            double newZ = y * sinX + z * cosX;
            y = newY;
            z = newZ;
        }

        return new Point3D(x, y, z);
    }

    @Override
    public Point3D getDerivative(double t) {
        // Производная базового эллипса
        double dx = -radiusX * Math.sin(t);
        double dy = radiusY * Math.cos(t);
        double dz = 0;

        if (rotation != 0) {
            double cosX = Math.cos(rotation);
            double sinX = Math.sin(rotation);
            double newDy = dy * cosX - dz * sinX;
            double newDz = dy * sinX + dz * cosX;
            dy = newDy;
            dz = newDz;
        }

        return new Point3D(dx, dy, dz);
    }

    public static List<Ellipse> createEllipse(double radiusX, double radiusY) {
        List<Ellipse> cross = new ArrayList<>();
        cross.add(new Ellipse(radiusX, radiusY, 0.0));           // Обычный эллипс
        cross.add(new Ellipse(radiusX, radiusY, Math.PI / 2));   // Повёрнутый на 90
        return cross;
    }
    public static List<Ellipse> createSphere(double radius, int segments) {
        List<Ellipse> sphere = new ArrayList<>();
        for (int i = 0; i < segments; i++) {
            double angle = (Math.PI * i) / segments;
            sphere.add(new Ellipse(radius, radius, angle));
        }
        return sphere;
    }
}