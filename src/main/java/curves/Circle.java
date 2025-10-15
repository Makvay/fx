package curves;

import javafx.geometry.Point3D;

public class Circle extends Curve3D {
    private final double radius;

    public Circle(double radius) {
        if (radius <= 0) throw new IllegalArgumentException("Радиус должен быть положительным");
        this.radius = radius;
    }

    public double getRadius() { return radius; }

    @Override
    public Point3D getPoint(double t) {
        return new Point3D(radius * Math.cos(t), radius * Math.sin(t), 0);
    }

    @Override
    public Point3D getDerivative(double t) {
        return new Point3D(-radius * Math.sin(t), radius * Math.cos(t), 0);
    }
}
