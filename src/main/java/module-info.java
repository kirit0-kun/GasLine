module com.flowapp.GasLine {
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.base;
    requires org.jetbrains.annotations;
    requires DateTimeRCryptor;

    exports com.flowapp.GasLine;
    exports com.flowapp.GasLine.Controllers to javafx.fxml;
    opens com.flowapp.GasLine;
    opens com.flowapp.GasLine.Controllers to javafx.fxml;
}