package curves.visualization;

import curves.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
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
import javafx.util.Duration;

import java.util.*;
import java.util.stream.Collectors;


public class MainApplication extends Application {

    private double cameraDistance = 200;
    private List<Curve3D> curves;
    private Group visualizationRoot;
    private PerspectiveCamera camera;
    private Rotate xRotate;
    private Rotate yRotate;
    private Timeline rotationTimeline;
    private boolean isAnimating = false;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("3D Curve Analyzer");
        System.out.println("=== Curve Analyzer v1.0 ===");

        // Генерируем кривые
        curves = generateFixedCurves();
        System.out.println("Generated " + curves.size() + " curves:");
        for (Curve3D c : curves)
            System.out.println(" - " + c.getClass().getSimpleName());

        // Создаем TabPane (вкладки)
        TabPane tabPane = new TabPane();
        Tab visualizationTab = new Tab("3D Visualization", createVisualizationContent());
        Tab calculationsTab = new Tab("Calculations", createCalculationsContent());
        visualizationTab.setClosable(false);
        calculationsTab.setClosable(false);

        tabPane.getTabs().addAll(visualizationTab, calculationsTab);

        BorderPane mainLayout = new BorderPane(tabPane);
        Scene scene = new Scene(mainLayout, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Метод для фиксированной генерации кривых при запуске
    private List<Curve3D> generateFixedCurves() {
        List<Curve3D> fixedCurves = new ArrayList<>();

        // Добавляем по одному экземпляру каждого типа
        fixedCurves.add(new Circle(2.0));           // Круг с радиусом 2.0
        fixedCurves.add(new Ellipse(2.0, 3.0));     // Эллипс с радиусами 2.0 и 3.0
        fixedCurves.add(new Helix(2.0, 1.0));       // Спираль с радиусом 2.0 и шагом 1.0

        return fixedCurves;
    }

    // Метод для рандомной генерации кривых при запуске
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

        visualizationRoot = new Group();

        // Настройка камеры
        camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-cameraDistance);
        camera.setNearClip(0.1);
        camera.setFarClip(10000);

        SubScene subScene = new SubScene(visualizationRoot, 1200, 700);
        subScene.setCamera(camera);
        subScene.setFill(Color.WHITESMOKE);

        Group3DContainer group3DContainer = new Group3DContainer(subScene);

        HBox buttonContainer = createCurveSelectionButtons();

        // Подсказка управления
        Label hint = new Label("💡 Hold left mouse button to rotate, scroll to zoom");
        hint.setStyle("-fx-text-fill: gray; -fx-font-size: 12px; -fx-alignment: center;");
        visualizationPane.setTop(hint);
        BorderPane.setMargin(hint, new Insets(5, 0, 0, 0));

        // По умолчанию показываем спирали
        showCurvesByType("Helix");

        // Добавляем оси координат (опционально)
        addCoordinateAxes(visualizationRoot);

        // Настройка управления
        setupMouseControl(group3DContainer, visualizationRoot);
        setupZoomControl(group3DContainer);

        //  Выбор фона
        ColorPicker bgPicker = new ColorPicker(Color.WHITESMOKE);
        bgPicker.setOnAction(e -> subScene.setFill(bgPicker.getValue()));
        buttonContainer.getChildren().add(bgPicker);

        // Информация о количестве кривых
        Label infoLabel = new Label("Curves loaded: " + curves.size());
        infoLabel.setStyle("-fx-font-size: 12px;");

        VBox bottomBox = new VBox(buttonContainer, infoLabel);
        bottomBox.setPadding(new Insets(5));
        visualizationPane.setBottom(bottomBox);


        visualizationPane.setCenter(group3DContainer);
        return visualizationPane;
    }

    private void toggleSceneAnimation() {
        if (rotationTimeline == null) {
            xRotate = new Rotate(0, Rotate.X_AXIS);
            yRotate = new Rotate(0, Rotate.Y_AXIS);
            visualizationRoot.getTransforms().addAll(xRotate, yRotate);

            rotationTimeline = new Timeline(
                    new KeyFrame(Duration.seconds(0), new KeyValue(yRotate.angleProperty(), 0)),
                    new KeyFrame(Duration.seconds(30), new KeyValue(yRotate.angleProperty(), 360))
            );
            rotationTimeline.setCycleCount(Animation.INDEFINITE);
        }

        if (isAnimating) {
            // Останавливаем анимацию
            rotationTimeline.pause();
            isAnimating = false;
        } else {
            // Запускаем анимацию
            rotationTimeline.play();
            isAnimating = true;
        }
    }

    private HBox createCurveSelectionButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setStyle("-fx-alignment: center;");

        ToggleButton allButton = new ToggleButton("All Curves");
        ToggleButton circleButton = new ToggleButton("Circles");
        ToggleButton ellipseButton = new ToggleButton("Ellipses");
        ToggleButton helixButton = new ToggleButton("Helix's");
        ToggleButton animationButton = new ToggleButton("Start Animation"); // Изменено название

        ToggleGroup group = new ToggleGroup();
        allButton.setToggleGroup(group);
        circleButton.setToggleGroup(group);
        ellipseButton.setToggleGroup(group);
        helixButton.setToggleGroup(group);
        animationButton.setToggleGroup(group);

        helixButton.setSelected(true);

        allButton.setOnAction(e -> showCurvesByType("All"));
        circleButton.setOnAction(e -> showCurvesByType("Circle"));
        ellipseButton.setOnAction(e -> showCurvesByType("Ellipse"));
        helixButton.setOnAction(e -> showCurvesByType("Helix"));

        // Исправленный обработчик для кнопки анимации:
        animationButton.setOnAction(e -> {
            toggleSceneAnimation();
            // Обновляем текст кнопки в зависимости от состояния
            if (isAnimating) {
                animationButton.setText("Stop Animation");
            } else {
                animationButton.setText("Start Animation");
            }
        });

        buttonBox.getChildren().addAll(allButton, circleButton, ellipseButton, helixButton, animationButton);
        return buttonBox;
    }

    private void showCurvesByType(String curveType) {
        visualizationRoot.getChildren().clear();
        addCoordinateAxes(visualizationRoot);

        Random rand = new Random();
        for (Curve3D curve : curves) {
            String className = curve.getClass().getSimpleName();
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

    private void setupMouseControl(Group3DContainer container, Group root) {
        if (xRotate == null || yRotate == null) {
            xRotate = new Rotate(0, Rotate.X_AXIS);
            yRotate = new Rotate(0, Rotate.Y_AXIS);
            root.getTransforms().addAll(xRotate, yRotate);
        }

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

    private void setupZoomControl(Group3DContainer container) {
        container.setOnScroll((ScrollEvent event) -> {
            double zoomFactor = 1.05;
            double delta = event.getDeltaY();

            if (delta < 0) cameraDistance *= zoomFactor;
            else cameraDistance /= zoomFactor;

            cameraDistance = Math.max(10, Math.min(500, cameraDistance));
            camera.setTranslateZ(-cameraDistance);
            event.consume();
        });
    }

    private VBox createCalculationsContent() {
        VBox calculationsPane = new VBox(10);
        calculationsPane.setPadding(new Insets(10));

        Label titleLabel = new Label("Curve Calculations at t=π/4");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TableView<CalculationResult> table = createResultsTable();
        VBox circlesInfo = createCirclesInfo();

        calculationsPane.getChildren().addAll(titleLabel, table, circlesInfo);
        return calculationsPane;
    }

    private TableView<CalculationResult> createResultsTable() {
        TableView<CalculationResult> table = new TableView<>();
        double tCheck = Math.PI / 4;

        TableColumn<CalculationResult, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<CalculationResult, String> pointCol = new TableColumn<>("Point");
        pointCol.setCellValueFactory(new PropertyValueFactory<>("point"));

        TableColumn<CalculationResult, String> derivativeCol = new TableColumn<>("Derivative");
        derivativeCol.setCellValueFactory(new PropertyValueFactory<>("derivative"));

        table.getColumns().addAll(typeCol, pointCol, derivativeCol);

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

        List<Circle> circles = curves.stream()
                .filter(c -> c instanceof Circle)
                .map(c -> (Circle) c)
                .sorted(Comparator.comparingDouble(Circle::getRadius))
                .collect(Collectors.toList());

        double sumRadii = circles.stream().mapToDouble(Circle::getRadius).sum();

        StringBuilder circlesText = new StringBuilder("Sorted circles by radius:\n");
        for (Circle c : circles)
            circlesText.append(String.format("Circle radius: %.2f\n", c.getRadius()));
        circlesText.append(String.format("\nTotal sum of radii: %.2f", sumRadii));

        Label circlesLabel = new Label(circlesText.toString());
        circlesLabel.setWrapText(true);

        box.getChildren().addAll(circlesTitle, circlesLabel);
        return box;
    }

    private void addCoordinateAxes(Group root) {
        final double axisLength = 100;


        javafx.scene.shape.Line xAxis = new javafx.scene.shape.Line();
        xAxis.setStroke(Color.RED);
        xAxis.setStrokeWidth(0);


        javafx.scene.shape.Line yAxis = new javafx.scene.shape.Line();
        yAxis.setStroke(Color.GREEN);
        yAxis.setStrokeWidth(0);


        javafx.scene.shape.Line zAxis = new javafx.scene.shape.Line();
        zAxis.setStroke(Color.BLUE);
        zAxis.setStrokeWidth(0);


        javafx.scene.text.Text xLabel = new javafx.scene.text.Text("X");
        xLabel.setFill(Color.RED);
        xLabel.setTranslateX(axisLength);

        javafx.scene.text.Text yLabel = new javafx.scene.text.Text("Y");
        yLabel.setFill(Color.GREEN);
        yLabel.setTranslateY(axisLength);

        javafx.scene.text.Text zLabel = new javafx.scene.text.Text("Z");
        zLabel.setFill(Color.BLUE);
        zLabel.setTranslateZ(axisLength );

        root.getChildren().addAll(xAxis, yAxis, zAxis, xLabel, yLabel, zLabel);
    }

    public static class CalculationResult {
        private final String type;
        private final String point;
        private final String derivative;

        public CalculationResult(String type, String point, String derivative) {
            this.type = type;
            this.point = point;
            this.derivative = derivative;
        }
        public String getType() { return type; }
        public String getPoint() { return point; }
        public String getDerivative() { return derivative; }
    }

    private static class Group3DContainer extends BorderPane {
        private final SubScene subScene;

        public Group3DContainer(SubScene subScene) {
            this.subScene = subScene;
            setCenter(subScene);
            setStyle("-fx-background-color: transparent;");
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
