package curves.visualization;

import curves.Circle;
import curves.Curve3D;
import curves.Ellipse;
import curves.Helix;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;

public class CurveVisualizer3D extends Application {

    private double cameraDistance = 150; // Дистанция камеры

    @Override
    public void start(Stage primaryStage) {
        Group root = new Group();
        Scene scene = new Scene(root, 1200, 1200, true);

        // Настройка 3D камеры - ИСПРАВЛЕНО
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-cameraDistance); // Отодвигаем камеру
        camera.setNearClip(0.1);
        camera.setFarClip(10000);
        scene.setCamera(camera);

        Random rand = new Random();
        List<Curve3D> curves = new ArrayList<>();

        // Создание случайных кривых
        for (int i = 0; i < 8; i++) {
            int type = rand.nextInt(3);
            switch (type) {
                case 0 -> curves.add(new Circle(1 + rand.nextDouble() * 3));
                case 1 -> curves.add(new Ellipse(1 + rand.nextDouble() * 2, 1 + rand.nextDouble() * 2));
                case 2 -> curves.add(new Helix(1 + rand.nextDouble() * 2, 0.5 + rand.nextDouble() * 1.5));
            }
        }

        // Визуализация 3D кривых точками - ИСПРАВЛЕНО
        for (Curve3D curve : curves) {
            Color curveColor = Color.color(rand.nextDouble(), rand.nextDouble(), rand.nextDouble());

            for (double t = 0; t <= 4 * Math.PI; t += 0.05) { // Более частые точки
                Point3D point = curve.getPoint(t);

                // Создание 3D точки (сфера)
                Sphere dot = new Sphere(0.8); // Немного больше точки
                dot.setTranslateX(point.getX() * 15); // Масштабирование
                dot.setTranslateY(point.getY() * 15);
                dot.setTranslateZ(point.getZ() * 15);
                dot.setMaterial(new javafx.scene.paint.PhongMaterial(curveColor));

                root.getChildren().add(dot);
            }
        }

        // Добавляем оси координат для ориентира
        addCoordinateAxes(root);

        // Вывод информации в консоль
        double tCheck = Math.PI / 4;
        System.out.println("Points and derivatives at t=PI/4:");
        for (Curve3D curve : curves) {
            System.out.println(curve.getClass().getSimpleName() +
                    " Point: " + curve.getPoint(tCheck) +
                    " Derivative: " + curve.getDerivative(tCheck));
        }

        // Обработка кругов
        List<Circle> circles = curves.stream()
                .filter(c -> c instanceof Circle)
                .map(c -> (Circle)c)
                .sorted(Comparator.comparingDouble(Circle::getRadius))
                .collect(Collectors.toList());

        double sumRadii = circles.stream().mapToDouble(Circle::getRadius).sum();
        System.out.println("Sorted circles by radius:");
        for (Circle c : circles) {
            System.out.println("Circle radius: " + c.getRadius());
        }
        System.out.println("Total sum of radii: " + sumRadii);

        // Добавляем вращение сцены для лучшего обзора
        setupMouseControl(scene, root);

        // Добавляем zoom колесиком мыши
        setupZoomControl(scene, camera);

        primaryStage.setTitle("3D Curve Visualizer - Centered");
        primaryStage.setScene(scene);
        primaryStage.show();
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

    public static void main(String[] args) {
        launch(args);
    }
}



