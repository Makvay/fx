package curves.visualization;

import curves.*;
import curves.properties.RotatedCurve;
import curves.properties.TranslatedCurve;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import java.util.List;
import java.util.Random;

public class CurveVisualizer {
    private final Group root;
    private final SubScene subScene;
    private final PerspectiveCamera camera;
    private Rotate xRotate, yRotate;
    private double cameraDistance = 200;
    private final Label infoLabel;

    public CurveVisualizer(SubScene subScene, Label infoLabel) {
        this.root = new Group();
        this.subScene = subScene;
        this.infoLabel = infoLabel;

        // Настройка камеры
        this.camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-cameraDistance);
        camera.setNearClip(0.1);
        camera.setFarClip(10000);

        subScene.setCamera(camera);
        subScene.setRoot(root);

        setupCoordinateAxes();
    }

    public void displayCurves(List<Curve3D> curves, String filterType) {
        root.getChildren().clear();
        setupCoordinateAxes();

        Random rand = new Random();

        for (Curve3D curve : curves) {
            String actualType = getActualCurveType(curve);

            if ("All".equals(filterType) || actualType.equals(filterType)) {
                Color curveColor = Color.color(rand.nextDouble(), rand.nextDouble(), rand.nextDouble());
                createCurvePoints(curve, curveColor, actualType);
            }
        }
    }

    private void createCurvePoints(Curve3D curve, Color color, String curveType) {
        for (double t = 0; t <= 4 * Math.PI; t += 0.07) {
            Point3D point = curve.getPoint(t);
            Point3D derivative = curve.getDerivative(t);

            Sphere dot = new Sphere(2.0);
            dot.setTranslateX(point.getX() * 15);
            dot.setTranslateY(point.getY() * 15);
            dot.setTranslateZ(point.getZ() * 15);
            dot.setMaterial(new PhongMaterial(color));
            dot.setPickOnBounds(true);

            // Подсказки при наведении
            double finalT = t;
            dot.setOnMouseEntered(event -> infoLabel.setText(String.format(
                    "%s | t = %.2f | Point: (%.2f, %.2f, %.2f) | Derivative: (%.2f, %.2f, %.2f)",
                    curveType, finalT, point.getX(), point.getY(), point.getZ(),
                    derivative.getX(), derivative.getY(), derivative.getZ()
            )));

            dot.setOnMouseExited(event -> infoLabel.setText("Hover over any point to see coordinates"));

            root.getChildren().add(dot);
        }
    }

    private String getActualCurveType(Curve3D curve) {
        if (curve instanceof TranslatedCurve) {
            return getActualCurveType(((TranslatedCurve) curve).getBaseCurve());
        } else if (curve instanceof RotatedCurve) {
            return getActualCurveType(((RotatedCurve) curve).getBaseCurve());
        } else {
            return curve.getClass().getSimpleName();
        }
    }


    private void setupCoordinateAxes() {
        final double axisLength = 100;

        // Ось X (красная, 3D)
        Group xAxisGroup = create3DLineX();

        // Ось Y (зеленая, 3D)
        Group yAxisGroup = create3DLineY();

        // Ось Z (синяя, 3D)
        Group zAxisGroup = create3DLineZ();

        // Метки осей
        javafx.scene.text.Text xLabel = new javafx.scene.text.Text("X");
        xLabel.setFill(Color.RED);
        xLabel.setTranslateX(axisLength + 5);

        javafx.scene.text.Text yLabel = new javafx.scene.text.Text("Y");
        yLabel.setFill(Color.GREEN);
        yLabel.setTranslateY(axisLength + 5);

        javafx.scene.text.Text zLabel = new javafx.scene.text.Text("Z");
        zLabel.setFill(Color.BLUE);
        zLabel.setTranslateZ(axisLength + 5);

        root.getChildren().addAll(xAxisGroup, yAxisGroup, zAxisGroup, xLabel, yLabel, zLabel);
    }

    // метод для создания 3D линии оси X
    private Group create3DLineX() {
        Group arrowGroup = new Group();

        // линия оси X
        Cylinder xLine = new Cylinder(0.5, 100); // радиус 1, длина 100
        xLine.setMaterial(new PhongMaterial(Color.RED));
        xLine.setRotationAxis(Rotate.Z_AXIS);
        xLine.setRotate(90);
        xLine.setTranslateX(50);

        arrowGroup.getChildren().addAll(xLine);
        return arrowGroup;
    }

    // метод для создания 3D линии оси Y
    private Group create3DLineY() {
        Group arrowGroup = new Group();

        // линия оси Y
        Cylinder yLine = new Cylinder(0.5, 100); // радиус 1, длина 100
        yLine.setMaterial(new PhongMaterial(Color.GREEN));
        yLine.setTranslateY(50);

        arrowGroup.getChildren().addAll(yLine);
        return arrowGroup;
    }

    // метод для создания 3D линии оси Z
    private Group create3DLineZ() {
        Group arrowGroup = new Group();

        // линия оси Z
        Cylinder zLine = new Cylinder(0.5, 100); // радиус 1, длина 100
        zLine.setMaterial(new PhongMaterial(Color.BLUE));
        zLine.setRotationAxis(Rotate.X_AXIS);
        zLine.setRotate(90);
        zLine.setTranslateZ(50);

        arrowGroup.getChildren().addAll(zLine);
        return arrowGroup;
    }


    // Методы управления камерой
    public void setupMouseControl(SubScene scene) {
        if (xRotate == null || yRotate == null) {
            xRotate = new Rotate(0, Rotate.X_AXIS);
            yRotate = new Rotate(0, Rotate.Y_AXIS);
            root.getTransforms().addAll(xRotate, yRotate);
        }

        final double[] anchor = new double[4]; // x, y, angleX, angleY

        scene.setOnMousePressed(event -> {
            anchor[0] = event.getX();
            anchor[1] = event.getY();
            anchor[2] = xRotate.getAngle();
            anchor[3] = yRotate.getAngle();
        });

        scene.setOnMouseDragged(event -> {
            xRotate.setAngle(anchor[2] - (anchor[1] - event.getY()) * 0.3);
            yRotate.setAngle(anchor[3] + (anchor[0] - event.getX()) * 0.3);
        });
    }

    public void setupZoomControl(SubScene scene) {
        scene.setOnScroll((ScrollEvent event) -> {
            double zoomFactor = 1.05;
            double delta = event.getDeltaY();

            if (delta < 0) cameraDistance *= zoomFactor;
            else cameraDistance /= zoomFactor;

            cameraDistance = Math.max(10, Math.min(500, cameraDistance));
            camera.setTranslateZ(-cameraDistance);
        });
    }

    // Геттеры
    public Rotate getXRotate() { return xRotate; }
    public Rotate getYRotate() { return yRotate; }
    public SubScene getSubScene() { return subScene; }
}