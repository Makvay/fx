package curves;

import javafx.geometry.Point3D;

// Абстрактный базовый класс для всех 3D кривых
public abstract class Curve3D {
    public abstract Point3D getPoint(double t);
    public abstract Point3D getDerivative(double t);
}

