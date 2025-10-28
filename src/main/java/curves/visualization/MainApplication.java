// Обновленный файл: MainApplication.java
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.shape.Cylinder;
import javafx.scene.paint.PhongMaterial;

import java.util.*;


public class MainApplication extends Application {

    private double cameraDistance = 200;
    private List<Curve3D> curves;
    private final List<Curve3D> userCurves = new ArrayList<>(); // Отдельный список для пользовательских кривых
    private Group visualizationRoot;
    private PerspectiveCamera camera;
    private Rotate xRotate;
    private Rotate yRotate;
    private Timeline rotationTimeline;
    private boolean isAnimating = false;
    private TableView<CalculationResult> resultsTable;
    private Label infoLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("3D Curve Analyzer with Rotation");
        System.out.println("=== Curve Analyzer v2.0 ===");

        // Генерируем кривые
        curves = generateFixedCurves();
        System.out.println("Generated " + curves.size() + " curves:");
        for (Curve3D c : curves)
            System.out.println(" - " + c.getClass().getSimpleName());

        // Создаем TabPane (вкладки)
        TabPane tabPane = new TabPane();
        Tab visualizationTab = new Tab("3D Visualization", createVisualizationContent());
        Tab calculationsTab = new Tab("Calculations", createCalculationsContent());
        Tab creationTab = new Tab("Create Curve", createCreationContent());

        visualizationTab.setClosable(false);
        calculationsTab.setClosable(false);
        creationTab.setClosable(false);

        tabPane.getTabs().addAll(visualizationTab, calculationsTab, creationTab);

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

    // Метод для получения всех кривых (исходные + пользовательские)
    private List<Curve3D> getAllCurves() {
        List<Curve3D> allCurves = new ArrayList<>();
        allCurves.addAll(curves);
        allCurves.addAll(userCurves);
        return allCurves;
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

        // Добавляем оси координат
        addCoordinateAxes(visualizationRoot);

        // Настройка управления
        setupMouseControl(group3DContainer, visualizationRoot);
        setupZoomControl(group3DContainer);

        // Выбор фона
        ColorPicker bgPicker = new ColorPicker(Color.WHITESMOKE);
        bgPicker.setOnAction(e -> subScene.setFill(bgPicker.getValue()));
        buttonContainer.getChildren().add(bgPicker);

        // Информация о количестве кривых
        infoLabel = new Label("Curves loaded: " + getAllCurves().size() + " (User: " + userCurves.size() + ")");
        infoLabel.setStyle("-fx-font-size: 12px;");

        VBox bottomBox = new VBox(buttonContainer, infoLabel);
        bottomBox.setPadding(new Insets(5));
        visualizationPane.setBottom(bottomBox);

        visualizationPane.setCenter(group3DContainer);
        return visualizationPane;
    }

    // ОБНОВЛЕННЫЙ МЕТОД: Создание интерфейса для добавления кривых с поворотом
    private BorderPane createCreationContent() {
        BorderPane creationPane = new BorderPane();
        creationPane.setPadding(new Insets(20));

        Label titleLabel = new Label("Create Custom Curve");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Форма для ввода параметров
        GridPane form = new GridPane();
        form.setVgap(15);
        form.setHgap(10);
        form.setPadding(new Insets(20));

        // Выбор типа кривой
        Label typeLabel = new Label("Curve Type:");
        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("Circle", "Ellipse", "Helix");
        typeComboBox.setValue("Circle");

        // Параметры
        Label radiusLabel = new Label("Radius:");
        TextField radiusField = new TextField("2.0");

        Label radiusYLabel = new Label("Radius Y (for Ellipse):");
        TextField radiusYField = new TextField("3.0");
        radiusYLabel.setVisible(false);
        radiusYField.setVisible(false);

        Label stepLabel = new Label("Step (for Helix):");
        TextField stepField = new TextField("1.0");
        stepLabel.setVisible(false);
        stepField.setVisible(false);

        // Смещение (позиция)
        Label offsetLabel = new Label("Position Offset:");
        GridPane offsetPane = new GridPane();
        offsetPane.setHgap(5);

        Label xLabel = new Label("X:");
        TextField xField = new TextField("0");
        xField.setPrefWidth(60);

        Label yLabel = new Label("Y:");
        TextField yField = new TextField("0");
        yField.setPrefWidth(60);

        Label zLabel = new Label("Z:");
        TextField zField = new TextField("0");
        zField.setPrefWidth(60);

        offsetPane.add(xLabel, 0, 0);
        offsetPane.add(xField, 1, 0);
        offsetPane.add(yLabel, 2, 0);
        offsetPane.add(yField, 3, 0);
        offsetPane.add(zLabel, 4, 0);
        offsetPane.add(zField, 5, 0);

        // НОВОЕ: Поворот
        Label rotationLabel = new Label("Rotation:");
        GridPane rotationPane = new GridPane();
        rotationPane.setHgap(5);

        Label axisLabel = new Label("Axis:");
        ComboBox<String> axisComboBox = new ComboBox<>();
        axisComboBox.getItems().addAll("X", "Y", "Z", "Custom");
        axisComboBox.setValue("X");

        Label angleLabel = new Label("Angle (degrees):");
        TextField angleField = new TextField("0");
        angleField.setPrefWidth(80);

        // Поля для пользовательской оси
        Label customAxisLabel = new Label("Custom Axis:");
        GridPane customAxisPane = new GridPane();
        customAxisPane.setHgap(5);

        Label cxLabel = new Label("X:");
        TextField cxField = new TextField("1");
        cxField.setPrefWidth(50);

        Label cyLabel = new Label("Y:");
        TextField cyField = new TextField("0");
        cyField.setPrefWidth(50);

        Label czLabel = new Label("Z:");
        TextField czField = new TextField("0");
        czField.setPrefWidth(50);

        customAxisPane.add(cxLabel, 0, 0);
        customAxisPane.add(cxField, 1, 0);
        customAxisPane.add(cyLabel, 2, 0);
        customAxisPane.add(cyField, 3, 0);
        customAxisPane.add(czLabel, 4, 0);
        customAxisPane.add(czField, 5, 0);
        customAxisPane.setVisible(false);

        rotationPane.add(axisLabel, 0, 0);
        rotationPane.add(axisComboBox, 1, 0);
        rotationPane.add(angleLabel, 2, 0);
        rotationPane.add(angleField, 3, 0);
        rotationPane.add(customAxisLabel, 0, 1);
        rotationPane.add(customAxisPane, 1, 1, 4, 1);

        // Обработчик изменения типа кривой
        typeComboBox.setOnAction(e -> {
            String type = typeComboBox.getValue();
            radiusYLabel.setVisible("Ellipse".equals(type));
            radiusYField.setVisible("Ellipse".equals(type));
            stepLabel.setVisible("Helix".equals(type));
            stepField.setVisible("Helix".equals(type));
        });

        // Обработчик изменения оси вращения
        axisComboBox.setOnAction(e -> {
            boolean isCustom = "Custom".equals(axisComboBox.getValue());
            customAxisPane.setVisible(isCustom);
        });

        // Кнопка создания
        Button createButton = new Button("Create Curve");
        createButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 15;");

        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: green;");

        // Добавление элементов в форму
        int row = 0;
        form.add(typeLabel, 0, row);
        form.add(typeComboBox, 1, row++);
        form.add(radiusLabel, 0, row);
        form.add(radiusField, 1, row++);
        form.add(radiusYLabel, 0, row);
        form.add(radiusYField, 1, row++);
        form.add(stepLabel, 0, row);
        form.add(stepField, 1, row++);
        form.add(offsetLabel, 0, row);
        form.add(offsetPane, 1, row++);
        form.add(rotationLabel, 0, row);
        form.add(rotationPane, 1, row++);
        form.add(createButton, 0, row, 2, 1);
        form.add(statusLabel, 0, ++row, 2, 1);

        // Обработчик кнопки создания
        createButton.setOnAction(e -> {
            try {
                Curve3D newCurve = createCurveFromInput(
                        typeComboBox.getValue(),
                        radiusField.getText(),
                        radiusYField.getText(),
                        stepField.getText(),
                        xField.getText(),
                        yField.getText(),
                        zField.getText(),
                        axisComboBox.getValue(),
                        angleField.getText(),
                        cxField.getText(),
                        cyField.getText(),
                        czField.getText()
                );

                userCurves.add(newCurve);
                statusLabel.setText("✓ Curve created successfully!");
                statusLabel.setStyle("-fx-text-fill: green;");

                // Обновляем интерфейс
                updateUIAfterCurveCreation();

            } catch (Exception ex) {
                statusLabel.setText("✗ Error: " + ex.getMessage());
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        });

        // Инструкция
        Label instruction = new Label(
                """
                        Instructions:
                        • Select curve type and enter parameters
                        • Use position offset to move the curve in 3D space
                        • Use rotation to rotate the curve around specified axis
                        • Created curves will appear in visualization and calculations tabs"""
        );
        instruction.setStyle("-fx-text-fill: gray; -fx-font-size: 12px;");
        instruction.setPadding(new Insets(20, 0, 0, 0));

        VBox content = new VBox(10, titleLabel, form, instruction);
        creationPane.setCenter(content);

        return creationPane;
    }

    // ОБНОВЛЕННЫЙ МЕТОД: Создание кривой на основе ввода пользователя с поворотом
    private Curve3D createCurveFromInput(String type, String radiusStr, String radiusYStr,
                                         String stepStr, String xStr, String yStr, String zStr,
                                         String axisType, String angleStr, String customXStr,
                                         String customYStr, String customZStr) {
        double radius = Double.parseDouble(radiusStr);
        double x = Double.parseDouble(xStr);
        double y = Double.parseDouble(yStr);
        double z = Double.parseDouble(zStr);
        double angleDegrees = Double.parseDouble(angleStr);
        double angleRadians = Math.toRadians(angleDegrees);

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
            Point3D rotationAxis = getRotationAxis(axisType, customXStr, customYStr, customZStr);
            baseCurve = new RotatedCurve(baseCurve, rotationAxis, angleRadians);
        }

        // Применяем смещение если нужно
        if (x != 0 || y != 0 || z != 0) {
            baseCurve = new TranslatedCurve(baseCurve, new Point3D(x, y, z));
        }

        return baseCurve;
    }

    // НОВЫЙ МЕТОД: Получение оси вращения
    private Point3D getRotationAxis(String axisType, String customXStr, String customYStr, String customZStr) {
        return switch (axisType) {
            case "X" -> new Point3D(1, 0, 0);
            case "Y" -> new Point3D(0, 1, 0);
            case "Z" -> new Point3D(0, 0, 1);
            case "Custom" -> {
                double cx = Double.parseDouble(customXStr);
                double cy = Double.parseDouble(customYStr);
                double cz = Double.parseDouble(customZStr);
                yield new Point3D(cx, cy, cz).normalize();
            }
            default -> throw new IllegalArgumentException("Unknown axis type: " + axisType);
        };
    }

    // НОВЫЙ МЕТОД: Обновление UI после создания кривой
    private void updateUIAfterCurveCreation() {
        // Обновляем label с количеством кривых
        if (infoLabel != null) {
            infoLabel.setText("Curves loaded: " + getAllCurves().size() + " (User: " + userCurves.size() + ")");
        }

        // Обновляем таблицу расчетов
        if (resultsTable != null) {
            updateResultsTable();
        }

        // Перерисовываем визуализацию, если сейчас отображаются "All Curves"
        showCurvesByType("All");
    }

    private void toggleSceneAnimation() {
        if (rotationTimeline == null) {
            xRotate = new Rotate(0, Rotate.X_AXIS);
            yRotate = new Rotate(0, Rotate.Y_AXIS);
            visualizationRoot.getTransforms().addAll(xRotate, yRotate);

            rotationTimeline = new Timeline(
                    new KeyFrame(Duration.seconds(0), new KeyValue(yRotate.angleProperty(), 0)),
                    new KeyFrame(Duration.seconds(0.03), new KeyValue(yRotate.angleProperty(), 360))
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
        ToggleButton animationButton = new ToggleButton("Start Animation");

        ToggleGroup group = new ToggleGroup();
        allButton.setToggleGroup(group);
        circleButton.setToggleGroup(group);
        ellipseButton.setToggleGroup(group);
        helixButton.setToggleGroup(group);
        animationButton.setToggleGroup(null); // Убираем из группы, чтобы не мешало

        helixButton.setSelected(true);

        allButton.setOnAction(e -> showCurvesByType("All"));
        circleButton.setOnAction(e -> showCurvesByType("Circle"));
        ellipseButton.setOnAction(e -> showCurvesByType("Ellipse"));
        helixButton.setOnAction(e -> showCurvesByType("Helix"));

        animationButton.setOnAction(e -> {
            toggleSceneAnimation();
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
        List<Curve3D> allCurves = getAllCurves();

        for (Curve3D curve : allCurves) {
            String actualClassName = getActualCurveType(curve);

            if ("All".equals(curveType) || actualClassName.equals(curveType)) {
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

    // НОВЫЙ МЕТОД: Получение реального типа кривой (с учетом декораторов)
    private String getActualCurveType(Curve3D curve) {
        if (curve instanceof TranslatedCurve) {
            return getActualCurveType(((TranslatedCurve) curve).getBaseCurve());
        } else if (curve instanceof RotatedCurve) {
            return getActualCurveType(((RotatedCurve) curve).getBaseCurve());
        } else {
            return curve.getClass().getSimpleName();
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

        resultsTable = createResultsTable();
        VBox circlesInfo = createCirclesInfo();

        calculationsPane.getChildren().addAll(titleLabel, resultsTable, circlesInfo);
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

        updateResultsTable(table);

        table.setPrefHeight(300);
        return table;
    }

    // ОБНОВЛЕННЫЙ МЕТОД: Обновление таблицы результатов
    private void updateResultsTable(TableView<CalculationResult> table) {
        double tCheck = Math.PI / 4;
        ObservableList<CalculationResult> data = FXCollections.observableArrayList();

        for (Curve3D curve : getAllCurves()) {
            String curveName = getCurveDisplayName(curve);

            data.add(new CalculationResult(
                    curveName,
                    curve.getPoint(tCheck).toString(),
                    curve.getDerivative(tCheck).toString()
            ));
        }

        table.setItems(data);
    }

    // НОВЫЙ МЕТОД: Получение отображаемого имени кривой
    private String getCurveDisplayName(Curve3D curve) {
        if (curve instanceof TranslatedCurve) {
            TranslatedCurve tc = (TranslatedCurve) curve;
            Point3D offset = tc.getOffset();
            return String.format("Translated %s (%.1f,%.1f,%.1f)",
                    getCurveDisplayName(tc.getBaseCurve()), offset.getX(), offset.getY(), offset.getZ());
        } else if (curve instanceof RotatedCurve) {
            RotatedCurve rc = (RotatedCurve) curve;
            Point3D axis = rc.getRotationAxis();
            double angle = Math.toDegrees(rc.getRotationAngle());
            return String.format("Rotated %s (%.0f° around %.1f,%.1f,%.1f)",
                    getCurveDisplayName(rc.getBaseCurve()), angle, axis.getX(), axis.getY(), axis.getZ());
        } else {
            return curve.getClass().getSimpleName();
        }
    }

    // Перегруженный метод для обновления существующей таблицы
    private void updateResultsTable() {
        if (resultsTable != null) {
            updateResultsTable(resultsTable);
        }
    }

    private VBox createCirclesInfo() {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10, 0, 0, 0));

        Label circlesTitle = new Label("Circle Information:");
        circlesTitle.setStyle("-fx-font-weight: bold;");

        List<Circle> circles = getAllCurves().stream()
                .filter(c -> getActualCurveType(c).equals("Circle"))
                .map(c -> {
                    // Извлекаем Circle из декораторов
                    Curve3D base = c;
                    while (!(base instanceof Circle)) {
                        if (base instanceof TranslatedCurve) {
                            base = ((TranslatedCurve) base).getBaseCurve();
                        } else if (base instanceof RotatedCurve) {
                            base = ((RotatedCurve) base).getBaseCurve();
                        } else {
                            break;
                        }
                    }
                    return (Circle) base;
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(Circle::getRadius))
                .toList();

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

        // Ось X (красная, 2D)
        javafx.scene.shape.Line xAxis = new javafx.scene.shape.Line(0, 0, axisLength, 0);
        xAxis.setStroke(Color.RED);
        xAxis.setStrokeWidth(2);

        // Ось Y (зеленая, 2D)
        javafx.scene.shape.Line yAxis = new javafx.scene.shape.Line(0, 0, 0, axisLength);
        yAxis.setStroke(Color.GREEN);
        yAxis.setStrokeWidth(2);

        // Ось Z (синяя, 3D)
        Group zAxisGroup = create3DArrow();

        javafx.scene.text.Text xLabel = new javafx.scene.text.Text("X");
        xLabel.setFill(Color.RED);
        xLabel.setTranslateX(axisLength + 5);

        javafx.scene.text.Text yLabel = new javafx.scene.text.Text("Y");
        yLabel.setFill(Color.GREEN);
        yLabel.setTranslateY(axisLength + 5);

        javafx.scene.text.Text zLabel = new javafx.scene.text.Text("Z");
        zLabel.setFill(Color.BLUE);
        zLabel.setTranslateZ(axisLength + 5);

        root.getChildren().addAll(xAxis, yAxis, zAxisGroup, xLabel, yLabel, zLabel);
    }

    // Вспомогательный метод для создания 3D стрелки
    private Group create3DArrow() {
        Group arrowGroup = new Group();

        javafx.scene.shape.Line zLine = new javafx.scene.shape.Line(0, 0, 0, 0);
        zLine.setStroke(Color.BLUE);
        zLine.setStrokeWidth(2);

        Cylinder arrowHead = new Cylinder(2, 100);
        arrowHead.setMaterial(new PhongMaterial(Color.BLUE));

        arrowHead.setTranslateZ(50);
        arrowHead.setRotationAxis(Rotate.X_AXIS);
        arrowHead.setRotate(90);

        arrowGroup.getChildren().addAll(zLine, arrowHead);

        return arrowGroup;
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