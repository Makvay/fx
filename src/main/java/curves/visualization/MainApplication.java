package curves.visualization;

import curves.*;
import curves.figures.Circle;
import curves.properties.RotatedCurve;
import curves.properties.TranslatedCurve;
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
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.*;

public class MainApplication extends Application {
    private final List<Curve3D> userCurves = new ArrayList<>();
    private CurveVisualizer visualizer;
    private Timeline rotationTimeline;
    private boolean isAnimating = false;
    private Label infoLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("3D Curve Analyzer with Rotation");

        TabPane tabPane = new TabPane();
        Tab visualizationTab = new Tab("3D Visualization", createVisualizationContent());
        Tab calculationsTab = new Tab("Calculations", createCalculationsContent());
        Tab creationTab = new Tab("Create Curve", createCreationContent());

        Arrays.asList(visualizationTab, calculationsTab, creationTab)
                .forEach(tab -> tab.setClosable(false));

        tabPane.getTabs().addAll(visualizationTab, calculationsTab, creationTab);

        Scene scene = new Scene(new BorderPane(tabPane), 1200, 840);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private BorderPane createVisualizationContent() {
        BorderPane pane = new BorderPane();

        // Создаем SubScene для 3D
        SubScene subScene = new SubScene(new Group(), 1200, 700);
        subScene.setFill(Color.WHITESMOKE);

        infoLabel = new Label("Hover over any point to see coordinates");
        infoLabel.setStyle("-fx-text-fill: black; -fx-font-size: 14px; -fx-font-weight: bold;");

        // Инициализируем визуализатор
        visualizer = new CurveVisualizer(subScene, infoLabel);
        visualizer.setupMouseControl(subScene);
        visualizer.setupZoomControl(subScene);

        // Кнопки управления
        HBox controls = createControlButtons();

        // ColorPicker с исправленной ссылкой
        ColorPicker bgPicker = new ColorPicker(Color.WHITESMOKE);
        bgPicker.setOnAction(e -> visualizer.getSubScene().setFill(bgPicker.getValue()));
        controls.getChildren().add(bgPicker);

        // Показываем спирали по умолчанию
        visualizer.displayCurves(getAllCurves(), "Helix");

        VBox bottomBox = new VBox(controls, createCurvesCountLabel());
        bottomBox.setPadding(new Insets(5));

        pane.setTop(infoLabel);
        pane.setCenter(subScene);
        pane.setBottom(bottomBox);

        return pane;
    }

    private HBox createControlButtons() {
        HBox box = new HBox(10);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-alignment: center;");

        ToggleButton allBtn = createToggleButton("All Curves", "All");
        ToggleButton circleBtn = createToggleButton("Circles", "Circle");
        ToggleButton ellipseBtn = createToggleButton("Ellipses", "Ellipse");
        ToggleButton helixBtn = createToggleButton("Helix's", "Helix");

        ToggleGroup group = new ToggleGroup();
        Arrays.asList(allBtn, circleBtn, ellipseBtn, helixBtn)
                .forEach(btn -> btn.setToggleGroup(group));

        helixBtn.setSelected(true);


        Label volumeLabel = new Label("Volume:");
        Slider volumeSlider = new Slider(0, 1, 0.7);
        volumeSlider.setPrefWidth(100);

        volumeLabel.setVisible(false);
        volumeSlider.setVisible(false);

        // начальная громкость
        SoundManager.getInstance().setVolume(0.7);

        // Обработчик изменения громкости
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            SoundManager.getInstance().setVolume(newValue.doubleValue());
        });


        Button animationBtn = new Button("Start Animation");
        animationBtn.setOnAction(e -> {
            toggleAnimation(animationBtn);
            boolean showVolume = isAnimating; // видимость элементов громкости
            volumeLabel.setVisible(showVolume);
            volumeSlider.setVisible(showVolume);
        });


        box.getChildren().addAll(allBtn, circleBtn, ellipseBtn, helixBtn, animationBtn,
                volumeLabel, volumeSlider);
        return box;
    }

    private ToggleButton createToggleButton(String text, String curveType) {
        ToggleButton btn = new ToggleButton(text);
        btn.setOnAction(e -> visualizer.displayCurves(getAllCurves(), curveType));
        return btn;
    }

    private void toggleAnimation(Button btn) {
        SoundManager soundManager = SoundManager.getInstance();

        if (rotationTimeline == null) {
            Rotate xRotate = visualizer.getXRotate();
            Rotate yRotate = visualizer.getYRotate();

            if (xRotate == null || yRotate == null) return;

            rotationTimeline = new Timeline(
                    new KeyFrame(Duration.seconds(0), new KeyValue(yRotate.angleProperty(), 0)),
                    new KeyFrame(Duration.seconds(0.03), new KeyValue(yRotate.angleProperty(), 360))
            );
            rotationTimeline.setCycleCount(Animation.INDEFINITE);
        }
        if (isAnimating) {
            // Останавливаем анимацию и звук
            rotationTimeline.pause();
            soundManager.pauseSound();
            btn.setText("Start Animation");
        } else {
            // Запускаем анимацию и звук
            rotationTimeline.play();
            soundManager.playSound();
            btn.setText("Stop Animation");
        }
        isAnimating = !isAnimating;
    }

    private VBox createCalculationsContent() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(10));

        Label title = new Label("Curve Calculations at t=π/4");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TableView<CalculationResult> table = createResultsTable();
        VBox circlesInfo = createCirclesInfo();

        pane.getChildren().addAll(title, table, circlesInfo);
        return pane;
    }

    private TableView<CalculationResult> createResultsTable() {
        TableView<CalculationResult> table = new TableView<>();

        TableColumn<CalculationResult, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<CalculationResult, String> pointCol = new TableColumn<>("Point");
        pointCol.setCellValueFactory(new PropertyValueFactory<>("point"));

        TableColumn<CalculationResult, String> derivativeCol = new TableColumn<>("Derivative");
        derivativeCol.setCellValueFactory(new PropertyValueFactory<>("derivative"));

        table.getColumns().addAll(typeCol, pointCol, derivativeCol);
        updateResultsTable(table);
        table.setPrefHeight(300);

        return table;
    }

    private void updateResultsTable(TableView<CalculationResult> table) {
        double t = Math.PI / 4;
        ObservableList<CalculationResult> data = FXCollections.observableArrayList();

        for (Curve3D curve : getAllCurves()) {
            data.add(new CalculationResult(
                    getCurveDisplayName(curve),
                    curve.getPoint(t).toString(),
                    curve.getDerivative(t).toString()
            ));
        }

        table.setItems(data);
    }

    private VBox createCirclesInfo() {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10, 0, 0, 0));

        Label title = new Label("Circle Information:");
        title.setStyle("-fx-font-weight: bold;");

        List<Circle> circles = getAllCurves().stream()
                .filter(c -> getActualCurveType(c).equals("Circle"))
                .map(this::extractCircle)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(Circle::getRadius))
                .toList();

        double sumRadii = circles.stream().mapToDouble(Circle::getRadius).sum();

        StringBuilder info = new StringBuilder("Sorted circles by radius:\n");
        circles.forEach(c -> info.append(String.format("Circle radius: %.2f\n", c.getRadius())));
        info.append(String.format("\nTotal sum of radii: %.2f", sumRadii));

        Label circlesLabel = new Label(info.toString());
        circlesLabel.setWrapText(true);

        box.getChildren().addAll(title, circlesLabel);
        return box;
    }

    private Circle extractCircle(Curve3D curve) {
        Curve3D current = curve;
        while (current != null && !(current instanceof Circle)) {
            if (current instanceof TranslatedCurve) {
                current = ((TranslatedCurve) current).getBaseCurve();
            } else if (current instanceof RotatedCurve) {
                current = ((RotatedCurve) current).getBaseCurve();
            } else {
                break;
            }
        }
        return (current instanceof Circle) ? (Circle) current : null;
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

    private String getCurveDisplayName(Curve3D curve) {
        if (curve instanceof TranslatedCurve tc) {
            Point3D offset = tc.getOffset();
            return String.format("Translated %s (%.1f,%.1f,%.1f)",
                    getCurveDisplayName(tc.getBaseCurve()), offset.getX(), offset.getY(), offset.getZ());
        } else if (curve instanceof RotatedCurve rc) {
            Point3D axis = rc.getRotationAxis();
            double angle = Math.toDegrees(rc.getRotationAngle());
            return String.format("Rotated %s (%.0f° around %.1f,%.1f,%.1f)",
                    getCurveDisplayName(rc.getBaseCurve()), angle, axis.getX(), axis.getY(), axis.getZ());
        } else {
            return curve.getClass().getSimpleName();
        }
    }

    private BorderPane createCreationContent() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(20));

        Label title = new Label("Create Custom Curve");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        CreationForm form = new CreationForm(this::onCurveCreated);

        VBox content = new VBox(10, title, form.getForm(), form.getInstructions());
        pane.setCenter(content);

        return pane;
    }

    private void onCurveCreated(Curve3D curve) {
        userCurves.add(curve);
        if (infoLabel != null) {
            infoLabel.setText("Hover over any point to see coordinates");
        }
    }

    private List<Curve3D> getAllCurves() {
        List<Curve3D> all = new ArrayList<>();
        all.addAll(Arrays.asList(CurveFactory.getDefaultCurves()));
        all.addAll(userCurves);
        return all;
    }

    private Label createCurvesCountLabel() {
        return new Label("Curves loaded: " + getAllCurves().size());
    }

    // Внутренний класс для результатов вычислений
    public static class CalculationResult {
        private final String type, point, derivative;

        public CalculationResult(String type, String point, String derivative) {
            this.type = type;
            this.point = point;
            this.derivative = derivative;
        }

        public String getType() { return type; }
        public String getPoint() { return point; }
        public String getDerivative() { return derivative; }
    }

    public static void main(String[] args) {
        launch(args);
    }
}