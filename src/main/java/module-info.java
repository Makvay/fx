module org.example.fx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics; // Добавлено для 3D визуализации

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;

    // Экспортируем основные пакеты
    exports curves.visualization;
    exports curves;

    // Открываем пакеты для FXML инъекции
    opens curves.visualization to javafx.fxml;
    opens curves to javafx.fxml;
}