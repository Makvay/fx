package curves;

import javafx.geometry.Point3D;

public abstract class Curve3D {
    public abstract Point3D getPoint(double t);
    public abstract Point3D getDerivative(double t);
}
