package com.flowapp.GasLine;

import com.flowapp.DateTimeRCryptor.DateTimeRCryptor;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;

public class Launcher extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final var isValid = isValid(primaryStage);
        if (isValid != null && isValid) {
            showMainWindow(primaryStage);
        }
    }

    private Boolean isValid(Stage primaryStage) {
        Boolean isValid = false;
        final var now = LocalDateTime.now();
        final var startTrial = LocalDateTime.of(2021, Month.JUNE, 11, 0,0);
        final var endTrial = LocalDateTime.of(2021, Month.JUNE, 12, 18,0);
        if (startTrial.isBefore(now) && endTrial.isAfter(now)) {
            return true;
        }
        String license = readKey();
        if (validKey(license)) {
            isValid = true;
        }

        while (isValid != null && !isValid) {
            isValid = null;
            license = showLicenseDialog(primaryStage);
            if (license != null) {
                isValid = validKey(license);
            }
        }
        return isValid;
    }

    private String readKey() {
        try {
            return DateTimeRCryptor.readKey();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Boolean validKey(String key) {
        try {
            final var range = DateTimeRCryptor.decryptKey(key);
            final var now = Date.from(ZonedDateTime.now().toInstant());
            if (range != null && range.getStartDate().before(now) && range.getEndDate().after(now)) {
                try {
                    DateTimeRCryptor.writeKey(key);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private String showLicenseDialog(Stage owner) {
        final String[] result = new String[1];
        final Stage dialog = new Stage();

        dialog.setTitle("Enter License Key");
        dialog.initOwner(owner);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.initModality(Modality.WINDOW_MODAL);

        final TextArea textField = new TextArea();
        textField.setMaxHeight(200);
        textField.setMaxWidth(350);
        textField.setWrapText(true);
        final Button submitButton = new Button("OK");
        submitButton.setDefaultButton(true);
        submitButton.setOnAction(t -> {
            result[0] = textField.getText();
            dialog.close();
        });
        textField.setMinHeight(TextField.USE_PREF_SIZE);
        final VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER_RIGHT);
        layout.setStyle("-fx-background-color: azure; -fx-padding: 10;");
        layout.getChildren().setAll(
                textField,
                submitButton
        );
        dialog.setScene(new Scene(layout));
        dialog.centerOnScreen();
        dialog.showAndWait();
        return result[0];
    }

    private void showMainWindow(Stage primaryStage) throws java.io.IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("Views/MainWindow.fxml")));
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
