package curves.properties;

import curves.Curve3D;
import javafx.geometry.Point3D;


public class RotatedCurve extends Curve3D {
    private final Curve3D baseCurve;
    private final Point3D rotationAxis;
    private final double rotationAngle;

    public RotatedCurve(Curve3D baseCurve, Point3D rotationAxis, double rotationAngle) {
        this.baseCurve = baseCurve;
        this.rotationAxis = rotationAxis.normalize();
        this.rotationAngle = rotationAngle;
    }

    public Curve3D getBaseCurve() { return baseCurve; }
    public Point3D getRotationAxis() { return rotationAxis; }
    public double getRotationAngle() { return rotationAngle; }

    @Override
    public Point3D getPoint(double t) {
        return rotatePoint(baseCurve.getPoint(t));
    }

    @Override
    public Point3D getDerivative(double t) {
        return rotatePoint(baseCurve.getDerivative(t));
    }

    private Point3D rotatePoint(Point3D point) {
        double cos = Math.cos(rotationAngle);
        double sin = Math.sin(rotationAngle);
        double dot = point.dotProduct(rotationAxis);
        Point3D cross = rotationAxis.crossProduct(point);

        double x = point.getX() * cos + cross.getX() * sin + rotationAxis.getX() * dot * (1 - cos);
        double y = point.getY() * cos + cross.getY() * sin + rotationAxis.getY() * dot * (1 - cos);
        double z = point.getZ() * cos + cross.getZ() * sin + rotationAxis.getZ() * dot * (1 - cos);

        return new Point3D(x, y, z);
    }
}
