module com.flowaap.GasLine {
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.base;
    requires org.jetbrains.annotations;

    exports com.flowapp.GasLine;
    //exports com.flowapp.GasLine.Controllers to javafx.fxml;
    opens com.flowapp.GasLine;
    //opens com.flowapp.GasLine.Controllers to javafx.fxml;
}