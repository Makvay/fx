package curves;

import javafx.geometry.Point3D;

public class Helix extends Curve3D {
    private double radius, step;

    public Helix(double radius, double step) {
        if (radius <= 0) throw new IllegalArgumentException("Radius must be positive");
        this.radius = radius;
        this.step = step;
    }

    @Override
    public Point3D getPoint(double t) {
        return new Point3D(radius * Math.cos(t), radius * Math.sin(t), step * t / (2 * Math.PI));
    }

    @Override
    public Point3D getDerivative(double t) {
        return new Point3D(-radius * Math.sin(t), radius * Math.cos(t), step / (2 * Math.PI));
    }
}