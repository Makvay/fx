package curves.visualization;

import curves.CurveFactory;
import curves.Curve3D;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.function.Consumer;


class CreationForm {
    private final GridPane form;
    private ComboBox<String> typeComboBox;
    private ComboBox<String> axisComboBox;
    private TextField radiusField;
    private TextField radiusYField;
    private TextField stepField;
    private TextField xField;
    private TextField yField;
    private TextField zField;
    private TextField angleField;
    private Label radiusYLabel;
    private Label stepLabel;
    private Label statusLabel;

    public CreationForm(Consumer<Curve3D> onCreate) {
        this.form = createForm(onCreate);
    }

    private GridPane createForm(Consumer<Curve3D> onCreate) {
        GridPane form = new GridPane();
        form.setVgap(15);
        form.setHgap(10);
        form.setPadding(new Insets(20));

        // Выбор типа кривой
        Label typeLabel = new Label("Curve Type:");
        typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("Circle", "Ellipse", "Helix");
        typeComboBox.setValue("Circle");

        // Параметры
        Label radiusLabel = new Label("Radius:");
        radiusField = new TextField("2.0");

        radiusYLabel = new Label("Radius Y (for Ellipse):");
        radiusYField = new TextField("3.0");
        radiusYLabel.setVisible(false);
        radiusYField.setVisible(false);

        stepLabel = new Label("Step (for Helix):");
        stepField = new TextField("1.0");
        stepLabel.setVisible(false);
        stepField.setVisible(false);

        // Смещение (позиция)
        Label offsetLabel = new Label("Position Offset:");
        GridPane offsetPane = new GridPane();
        offsetPane.setHgap(5);

        Label xLabel = new Label("X:");
        xField = new TextField("0");
        xField.setPrefWidth(60);

        Label yLabel = new Label("Y:");
        yField = new TextField("0");
        yField.setPrefWidth(60);

        Label zLabel = new Label("Z:");
        zField = new TextField("0");
        zField.setPrefWidth(60);

        offsetPane.add(xLabel, 0, 0);
        offsetPane.add(xField, 1, 0);
        offsetPane.add(yLabel, 2, 0);
        offsetPane.add(yField, 3, 0);
        offsetPane.add(zLabel, 4, 0);
        offsetPane.add(zField, 5, 0);

        // Поворот
        Label rotationLabel = new Label("Rotation:");
        GridPane rotationPane = new GridPane();
        rotationPane.setHgap(5);

        Label axisLabel = new Label("Axis:");
        axisComboBox = new ComboBox<>();
        axisComboBox.getItems().addAll("X", "Y", "Z"); // Убрал "Custom"
        axisComboBox.setValue("X");

        Label angleLabel = new Label("Angle (degrees):");
        angleField = new TextField("0");
        angleField.setPrefWidth(80);

        rotationPane.add(axisLabel, 0, 0);
        rotationPane.add(axisComboBox, 1, 0);
        rotationPane.add(angleLabel, 2, 0);
        rotationPane.add(angleField, 3, 0);

        // Обработчик изменения типа кривой
        typeComboBox.setOnAction(e -> {
            String type = typeComboBox.getValue();
            radiusYLabel.setVisible("Ellipse".equals(type));
            radiusYField.setVisible("Ellipse".equals(type));
            stepLabel.setVisible("Helix".equals(type));
            stepField.setVisible("Helix".equals(type));
        });

        // Кнопка создания
        Button createButton = new Button("Create Curve");
        createButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 15;");

        statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: green;");

        // Обработчик кнопки создания
        createButton.setOnAction(e -> {
            try {
                Curve3D newCurve = CurveFactory.createCurve(
                        typeComboBox.getValue(),
                        radiusField.getText(),
                        radiusYField.getText(),
                        stepField.getText(),
                        xField.getText(),
                        yField.getText(),
                        zField.getText(),
                        axisComboBox.getValue(),
                        angleField.getText()
                );

                onCreate.accept(newCurve);
                statusLabel.setText("✓ Curve created successfully!");
                statusLabel.setStyle("-fx-text-fill: green;");

            } catch (Exception ex) {
                statusLabel.setText("✗ Error: " + ex.getMessage());
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        });

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

        return form;
    }

    public GridPane getForm() {
        return form;
    }

    public Label getInstructions() {
        Label instruction = new Label(
                """
                        Instructions:
                        • Select curve type and enter parameters
                        • Use position offset to move the curve in 3D space
                        • Use rotation to rotate the curve around specified axis (X, Y, or Z)
                        • Created curves will appear in visualization and calculations tabs"""
        );
        instruction.setStyle("-fx-text-fill: gray; -fx-font-size: 12px;");
        instruction.setPadding(new Insets(20, 0, 0, 0));
        return instruction;
    }
}