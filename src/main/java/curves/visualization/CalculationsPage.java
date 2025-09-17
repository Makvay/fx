package curves.visualization;

import curves.Circle;
import curves.Curve3D;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CalculationsPage {
    private Stage stage;
    private List<Curve3D> curves;

    public CalculationsPage() {
        this.stage = new Stage();
        // Генерируем те же кривые, что и в визуализаторе
        this.curves = new CurveVisualizer3D().getCurves();
    }

    public void showCalculations() {
        stage.setTitle("Curve Calculations");

        BorderPane mainPane = new BorderPane();
        mainPane.setPadding(new Insets(10));

        // Заголовок
        Label titleLabel = new Label("Curve Calculations at t=π/4");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Таблица с точками и производными
        TableView<CalculationResult> table = createResultsTable();

        // Информация о кругах
        VBox circlesInfo = createCirclesInfo();

        // Кнопка возврата
        Button backButton = new Button("Back to Main");
        backButton.setOnAction(e -> stage.close());

        VBox content = new VBox(10, titleLabel, table, circlesInfo, backButton);
        mainPane.setCenter(content);

        Scene scene = new Scene(mainPane, 800, 600);
        stage.setScene(scene);
        stage.show();
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

        public String getType() { return type; }
        public String getPoint() { return point; }
        public String getDerivative() { return derivative; }
    }
}