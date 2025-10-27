package curves;

import javafx.geometry.Point3D;

public class TranslatedCurve extends Curve3D {
    private final Curve3D baseCurve;
    private final Point3D offset;

    public TranslatedCurve(Curve3D baseCurve, Point3D offset) {
        this.baseCurve = baseCurve;
        this.offset = offset;
    }

    public Curve3D getBaseCurve() {
        return baseCurve;
    }

    public Point3D getOffset() {
        return offset;
    }

    @Override
    public Point3D getPoint(double t) {
        Point3D basePoint = baseCurve.getPoint(t);
        return new Point3D(
                basePoint.getX() + offset.getX(),
                basePoint.getY() + offset.getY(),
                basePoint.getZ() + offset.getZ()
        );
    }

    @Override
    public Point3D getDerivative(double t) {
        // Производная не меняется при смещении
        return baseCurve.getDerivative(t);
    }
}