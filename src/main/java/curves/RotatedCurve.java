package curves;

import javafx.geometry.Point3D;

public class RotatedCurve extends Curve3D {
    private final Curve3D baseCurve;
    private final Point3D rotationAxis;
    private final double rotationAngle; // в радианах

    public RotatedCurve(Curve3D baseCurve, Point3D rotationAxis, double rotationAngle) {
        this.baseCurve = baseCurve;
        this.rotationAxis = rotationAxis.normalize();
        this.rotationAngle = rotationAngle;
    }

    public Curve3D getBaseCurve() {
        return baseCurve;
    }

    public Point3D getRotationAxis() {
        return rotationAxis;
    }

    public double getRotationAngle() {
        return rotationAngle;
    }

    @Override
    public Point3D getPoint(double t) {
        Point3D point = baseCurve.getPoint(t);
        return rotatePoint(point, rotationAxis, rotationAngle);
    }

    @Override
    public Point3D getDerivative(double t) {
        Point3D derivative = baseCurve.getDerivative(t);
        return rotatePoint(derivative, rotationAxis, rotationAngle);
    }

    private Point3D rotatePoint(Point3D point, Point3D axis, double angle) {
        // Формула поворота Родригеса
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double dot = point.dotProduct(axis);

        Point3D cross = axis.crossProduct(point);

        double x = point.getX() * cos + cross.getX() * sin + axis.getX() * dot * (1 - cos);
        double y = point.getY() * cos + cross.getY() * sin + axis.getY() * dot * (1 - cos);
        double z = point.getZ() * cos + cross.getZ() * sin + axis.getZ() * dot * (1 - cos);

        return new Point3D(x, y, z);
    }
}