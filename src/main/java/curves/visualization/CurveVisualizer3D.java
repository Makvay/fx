package curves.visualization;

import curves.Circle;
import curves.Curve3D;
import curves.Ellipse;
import curves.Helix;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;

public class CurveVisualizer3D {
    private Stage stage;
    private double cameraDistance = 250;
    private Group root;
    private List<Curve3D> curves;

    public CurveVisualizer3D() {
        this.stage = new Stage();
        this.curves = generateRandomCurves();
    }

    public void showVisualization() {
        stage.setTitle("3D Curve Visualizer");

        root = new Group();
        Scene scene = new Scene(root, 1200, 800, true);

        // Настройка 3D камеры
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-cameraDistance);
        camera.setNearClip(0.1);
        camera.setFarClip(10000);
        scene.setCamera(camera);

        // Визуализация 3D кривых точками
        visualizeCurves();

        // Добавляем оси координат
        addCoordinateAxes(root);

        // Настройка управления
        setupMouseControl(scene, root);
        setupZoomControl(scene, camera);

        // Добавляем кнопку возврата
        Button backButton = new Button("Back to Main");
        backButton.setOnAction(e -> stage.close());

        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(root);
        mainPane.setTop(backButton);

        Scene finalScene = new Scene(mainPane, 1200, 800, true);
        finalScene.setCamera(camera);

        stage.setScene(finalScene);
        stage.show();
    }

    private List<Curve3D> generateRandomCurves() {
        Random rand = new Random();
        List<Curve3D> curves = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            int type = rand.nextInt(3);
            switch (type) {
                case 0 -> curves.add(new Circle(1 + rand.nextDouble() * 3));
                case 1 -> curves.add(new Ellipse(1 + rand.nextDouble() * 2, 1 + rand.nextDouble() * 2));
                case 2 -> curves.add(new Helix(1 + rand.nextDouble() * 2, 0.5 + rand.nextDouble() * 1.5));
            }
        }
        return curves;
    }

    private void visualizeCurves() {
        Random rand = new Random();
        for (Curve3D curve : curves) {
            Color curveColor = Color.color(rand.nextDouble(), rand.nextDouble(), rand.nextDouble());

            for (double t = 0; t <= 4 * Math.PI; t += 0.05) {
                Point3D point = curve.getPoint(t);

                Sphere dot = new Sphere(0.8);
                dot.setTranslateX(point.getX() * 15);
                dot.setTranslateY(point.getY() * 15);
                dot.setTranslateZ(point.getZ() * 15);
                dot.setMaterial(new javafx.scene.paint.PhongMaterial(curveColor));

                root.getChildren().add(dot);
            }
        }
    }

    public List<Curve3D> getCurves() {
        return curves;
    }

    // Добавляем оси координат
    private void addCoordinateAxes(Group root) {
        // Можно добавить простые линии для осей X, Y, Z
        // Это поможет ориентироваться в пространстве
    }

    // Метод для вращения сцены мышью
    private void setupMouseControl(Scene scene, Group root) {
        Rotate xRotate = new Rotate(0, Rotate.X_AXIS);
        Rotate yRotate = new Rotate(0, Rotate.Y_AXIS);
        root.getTransforms().addAll(xRotate, yRotate);

        final double[] anchorX = new double[1];
        final double[] anchorY = new double[1];
        final double[] anchorAngleX = new double[1];
        final double[] anchorAngleY = new double[1];

        scene.setOnMousePressed(event -> {
            anchorX[0] = event.getSceneX();
            anchorY[0] = event.getSceneY();
            anchorAngleX[0] = xRotate.getAngle();
            anchorAngleY[0] = yRotate.getAngle();
        });

        scene.setOnMouseDragged(event -> {
            xRotate.setAngle(anchorAngleX[0] - (anchorY[0] - event.getSceneY()) * 0.3);
            yRotate.setAngle(anchorAngleY[0] + (anchorX[0] - event.getSceneX()) * 0.3);
        });
    }

    // Добавляем zoom колесиком мыши
    private void setupZoomControl(Scene scene, PerspectiveCamera camera) {
        scene.setOnScroll(event -> {
            double zoomFactor = 1.05;
            double delta = event.getDeltaY();

            if (delta < 0) {
                cameraDistance *= zoomFactor;
            } else {
                cameraDistance /= zoomFactor;
            }
            camera.setTranslateZ(-cameraDistance);
        });
    }
}