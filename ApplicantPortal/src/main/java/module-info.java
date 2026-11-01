module com.example.portal {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.portal to javafx.fxml;
    exports com.example.portal;
}
