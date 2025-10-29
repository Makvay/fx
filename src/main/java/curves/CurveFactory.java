package curves;

import curves.figures.Circle;
import curves.figures.Ellipse;
import curves.figures.Helix;
import curves.properties.RotatedCurve;
import curves.properties.TranslatedCurve;
import javafx.geometry.Point3D;


public class CurveFactory {

    public static Curve3D createCurve(String type, String radiusStr, String radiusYStr, String stepStr,
                                      String offsetXStr, String offsetYStr, String offsetZStr,
                                      String rotationAxis, String angleStr) {

        double radius = Double.parseDouble(radiusStr);
        double x = Double.parseDouble(offsetXStr);
        double y = Double.parseDouble(offsetYStr);
        double z = Double.parseDouble(offsetZStr);
        double angleDegrees = Double.parseDouble(angleStr);

        Curve3D baseCurve = switch (type) {
            case "Circle" -> new Circle(radius);
            case "Ellipse" -> {
                double radiusY = Double.parseDouble(radiusYStr);
                yield new Ellipse(radius, radiusY);
            }
            case "Helix" -> {
                double step = Double.parseDouble(stepStr);
                yield new Helix(radius, step);
            }
            default -> throw new IllegalArgumentException("Unknown curve type: " + type);
        };

        // Применяем поворот если угол не нулевой
        if (angleDegrees != 0) {
            Point3D rotationAxisPoint = getRotationAxis(rotationAxis);
            baseCurve = new RotatedCurve(baseCurve, rotationAxisPoint, Math.toRadians(angleDegrees));
        }

        // Применяем смещение если нужно
        if (x != 0 || y != 0 || z != 0) {
            baseCurve = new TranslatedCurve(baseCurve, new Point3D(x, y, z));
        }

        return baseCurve;
    }

    private static Point3D getRotationAxis(String axisType) {
        return switch (axisType) {
            case "X" -> new Point3D(1, 0, 0);
            case "Y" -> new Point3D(0, 1, 0);
            case "Z" -> new Point3D(0, 0, 1);
            default -> throw new IllegalArgumentException("Unknown axis type: " + axisType);
        };
    }

    public static Curve3D[] getDefaultCurves() {
        return new Curve3D[] {
                new Circle(2.0),
                new Ellipse(2.0, 3.0),
                new Helix(2.0, 1.0)
        };
    }
}