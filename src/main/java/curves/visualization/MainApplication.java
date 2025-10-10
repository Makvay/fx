package curves.visualization;

import curves.*;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;

public class MainApplication extends Application {

    private double cameraDistance = 200;
    private List<Curve3D> curves;
    private Group visualizationRoot;
    private PerspectiveCamera camera;
    private Rotate xRotate;
    private Rotate yRotate;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("3D Curve Analyzer");

        // Генерируем кривые
        curves = generateRandomCurves();

        // Создаем TabPane для переключения между страницами
        TabPane tabPane = new TabPane();

        // Создаем вкладки
        Tab visualizationTab = new Tab("3D Visualization", createVisualizationContent());
        Tab calculationsTab = new Tab("Calculations", createCalculationsContent());

        // Делаем вкладки не закрываемыми
        visualizationTab.setClosable(false);
        calculationsTab.setClosable(false);

        tabPane.getTabs().addAll(visualizationTab, calculationsTab);

        // Основной layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setCenter(tabPane);

        Scene scene = new Scene(mainLayout, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
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

    private BorderPane createVisualizationContent() {
        BorderPane visualizationPane = new BorderPane();

        // Создаем корневую группу для 3D сцены
        visualizationRoot = new Group();

        // Настройка 3D камеры
        camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-cameraDistance);
        camera.setNearClip(0.1);
        camera.setFarClip(10000);

        // Создаем SubScene для 3D контента
        SubScene subScene = new SubScene(visualizationRoot, 1200, 700);
        subScene.setCamera(camera);
        subScene.setFill(Color.WHITESMOKE);

        // Создаем специальную панель для 3D контента с обработкой событий
        Group3DContainer group3DContainer = new Group3DContainer(subScene);

        // Создаем кнопки для выбора фигур
        HBox buttonContainer = createCurveSelectionButtons();

        // Визуализация - по умолчанию показываем только спирали
        showCurvesByType("Helix");

        // Добавляем оси координат
        addCoordinateAxes(visualizationRoot);

        // Настройка управления
        setupMouseControl(group3DContainer, visualizationRoot);
        setupZoomControl(group3DContainer);

        visualizationPane.setCenter(group3DContainer);
        visualizationPane.setBottom(buttonContainer);

        return visualizationPane;
    }

    private HBox createCurveSelectionButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setStyle("-fx-alignment: center;");

        ToggleButton allButton = new ToggleButton("All Curves");
        ToggleButton circleButton = new ToggleButton("Circles");
        ToggleButton ellipseButton = new ToggleButton("Ellipses");
        ToggleButton helixButton = new ToggleButton("Helixes");

        // По умолчанию выбираем спирали
        helixButton.setSelected(true);

        // Обработчики событий для кнопок
        allButton.setOnAction(e -> showCurvesByType("All"));
        circleButton.setOnAction(e -> showCurvesByType("Circle"));
        ellipseButton.setOnAction(e -> showCurvesByType("Ellipse"));
        helixButton.setOnAction(e -> showCurvesByType("Helix"));

        buttonBox.getChildren().addAll(allButton, circleButton, ellipseButton, helixButton);
        return buttonBox;
    }

    private void showCurvesByType(String curveType) {
        // Очищаем сцену
        visualizationRoot.getChildren().clear();

        // Добавляем оси координат обратно
        addCoordinateAxes(visualizationRoot);

        Random rand = new Random();

        for (Curve3D curve : curves) {
            String className = curve.getClass().getSimpleName();

            // Фильтруем кривые по типу
            if ("All".equals(curveType) || className.equals(curveType)) {
                Color curveColor = Color.color(rand.nextDouble(), rand.nextDouble(), rand.nextDouble());

                for (double t = 0; t <= 4 * Math.PI; t += 0.05) {
                    Point3D point = curve.getPoint(t);

                    Sphere dot = new Sphere(0.8);
                    dot.setTranslateX(point.getX() * 15);
                    dot.setTranslateY(point.getY() * 15);
                    dot.setTranslateZ(point.getZ() * 15);
                    dot.setMaterial(new javafx.scene.paint.PhongMaterial(curveColor));

                    visualizationRoot.getChildren().add(dot);
                }
            }
        }
    }

    // Метод для вращения сцены мышью
    private void setupMouseControl(Group3DContainer container, Group root) {
        xRotate = new Rotate(0, Rotate.X_AXIS);
        yRotate = new Rotate(0, Rotate.Y_AXIS);
        root.getTransforms().addAll(xRotate, yRotate);

        final double[] anchorX = new double[1];
        final double[] anchorY = new double[1];
        final double[] anchorAngleX = new double[1];
        final double[] anchorAngleY = new double[1];

        container.setOnMousePressed(event -> {
            anchorX[0] = event.getX();
            anchorY[0] = event.getY();
            anchorAngleX[0] = xRotate.getAngle();
            anchorAngleY[0] = yRotate.getAngle();
            event.consume();
        });

        container.setOnMouseDragged(event -> {
            xRotate.setAngle(anchorAngleX[0] - (anchorY[0] - event.getY()) * 0.3);
            yRotate.setAngle(anchorAngleY[0] + (anchorX[0] - event.getX()) * 0.3);
            event.consume();
        });
    }

    // Метод для zoom колесиком мыши
    private void setupZoomControl(Group3DContainer container) {
        container.setOnScroll((ScrollEvent event) -> {
            double zoomFactor = 1.05;
            double delta = event.getDeltaY();

            if (delta < 0) {
                // Отдаление
                cameraDistance *= zoomFactor;
            } else {
                // Приближение
                cameraDistance /= zoomFactor;
            }

            // Ограничиваем минимальное и максимальное расстояние камеры
            cameraDistance = Math.max(10, Math.min(500, cameraDistance));

            camera.setTranslateZ(-cameraDistance);
            event.consume();
        });
    }

    private VBox createCalculationsContent() {
        VBox calculationsPane = new VBox(10);
        calculationsPane.setPadding(new Insets(10));

        // Заголовок
        Label titleLabel = new Label("Curve Calculations at t=π/4");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Таблица с точками и производными
        TableView<CalculationResult> table = createResultsTable();

        // Информация о кругах
        VBox circlesInfo = createCirclesInfo();

        calculationsPane.getChildren().addAll(titleLabel, table, circlesInfo);

        return calculationsPane;
    }

    private TableView<CalculationResult> createResultsTable() {
        TableView<CalculationResult> table = new TableView<>();
        double tCheck = Math.PI / 4;

        // Создаем колонки
        TableColumn<CalculationResult, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<CalculationResult, String> pointCol = new TableColumn<>("Point");
        pointCol.setCellValueFactory(new PropertyValueFactory<>("point"));

        TableColumn<CalculationResult, String> derivativeCol = new TableColumn<>("Derivative");
        derivativeCol.setCellValueFactory(new PropertyValueFactory<>("derivative"));

        table.getColumns().addAll(typeCol, pointCol, derivativeCol);

        // Заполняем таблицу данными
        ObservableList<CalculationResult> data = FXCollections.observableArrayList();
        for (Curve3D curve : curves) {
            data.add(new CalculationResult(
                    curve.getClass().getSimpleName(),
                    curve.getPoint(tCheck).toString(),
                    curve.getDerivative(tCheck).toString()
            ));
        }

        table.setItems(data);
        table.setPrefHeight(300);
        return table;
    }

    private VBox createCirclesInfo() {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10, 0, 0, 0));

        Label circlesTitle = new Label("Circle Information:");
        circlesTitle.setStyle("-fx-font-weight: bold;");

        // Фильтруем круги и сортируем по радиусу
        List<Circle> circles = curves.stream()
                .filter(c -> c instanceof Circle)
                .map(c -> (Circle) c)
                .sorted(Comparator.comparingDouble(Circle::getRadius))
                .collect(Collectors.toList());

        double sumRadii = circles.stream().mapToDouble(Circle::getRadius).sum();

        StringBuilder circlesText = new StringBuilder("Sorted circles by radius:\n");
        for (Circle c : circles) {
            circlesText.append(String.format("Circle radius: %.2f\n", c.getRadius()));
        }
        circlesText.append(String.format("\nTotal sum of radii: %.2f", sumRadii));

        Label circlesLabel = new Label(circlesText.toString());
        circlesLabel.setWrapText(true);

        box.getChildren().addAll(circlesTitle, circlesLabel);
        return box;
    }

    private void addCoordinateAxes(Group root) {
        // Реализация осей координат (можно оставить пустым или добавить позже)
    }

    // Вспомогательный класс для отображения данных в таблице
    public static class CalculationResult {
        private final String type;
        private final String point;
        private final String derivative;

        public CalculationResult(String type, String point, String derivative) {
            this.type = type;
            this.point = point;
            this.derivative = derivative;
        }

        public String getType() {
            return type;
        }

        public String getPoint() {
            return point;
        }

        public String getDerivative() {
            return derivative;
        }
    }

    // Специальный контейнер для 3D контента с обработкой событий
    private static class Group3DContainer extends BorderPane {
        private final SubScene subScene;

        public Group3DContainer(SubScene subScene) {
            this.subScene = subScene;
            setCenter(subScene);
            setStyle("-fx-background-color: transparent;");

            // Включаем события мыши
            setPickOnBounds(true);
        }

        public SubScene getSubScene() {
            return subScene;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}