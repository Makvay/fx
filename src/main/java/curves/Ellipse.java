package curves;

import javafx.geometry.Point3D;

public class Ellipse extends Curve3D {
    private double radiusX, radiusY;

    public Ellipse(double radiusX, double radiusY) {
        if (radiusX <= 0 || radiusY <= 0) throw new IllegalArgumentException("Radii must be positive");
        this.radiusX = radiusX;
        this.radiusY = radiusY;
    }

    @Override
    public Point3D getPoint(double t) {
        return new Point3D(radiusX * Math.cos(t), radiusY * Math.sin(t), 0);
    }

    @Override
    public Point3D getDerivative(double t) {
        return new Point3D(-radiusX * Math.sin(t), radiusY * Math.cos(t), 0);
    }
}