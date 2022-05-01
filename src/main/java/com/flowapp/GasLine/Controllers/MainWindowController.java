package com.flowapp.GasLine.Controllers;

import com.flowapp.GasLine.GasLine;
import com.flowapp.GasLine.Models.GasPipe;
import com.flowapp.GasLine.Models.GasPipeResult;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.*;
import java.util.regex.Pattern;

public class MainWindowController implements Initializable {


    @FXML
    private TextField iDTextField;

    @FXML
    private TextField loopIDTextField;

    @FXML
    private TextField totalLengthTextField;

    @FXML
    private TextField flowRateTextField;

    @FXML
    private TextField flowRateIncreaseTextField;

    @FXML
    private TextField consumerFlowRateTextField;

    @FXML
    private TextField p1TextField;

    @FXML
    private TextField p2TextField;

    @FXML
    private TextField tAvgTextField;

    @FXML
    private TextField maxPressureTextField;

    @FXML
    private TextField roughnessTextField;

    @FXML
    private TextField c1YTextField;

    @FXML
    private TextField c2YTextField;

    @FXML
    private TextField c3YTextField;

    @FXML
    private TextField nitrogenYTextField;

    @FXML
    private TextField h2sYTextField1;

    @FXML
    private TextField c4YTextBox;

    @FXML
    private TextArea answerArea;

    @FXML
    private Button calculateBtn;

    @FXML
    private ImageView facebookIcon;

    @FXML
    private ImageView linkedInIcon;

    @FXML
    private ImageView emailIcon;

    private Stage chartsWindow;

    private final Application application;

    public MainWindowController(Application application) {
        this.application = application;
    }

    Stage getStage() {
        return (Stage) answerArea.getScene().getWindow();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        final TextField[] textFields = {
         iDTextField,
         loopIDTextField,
         totalLengthTextField,
                flowRateTextField,
         flowRateIncreaseTextField,
         consumerFlowRateTextField,
         p1TextField,
         p2TextField,
         tAvgTextField,
         maxPressureTextField,
         roughnessTextField,
         c1YTextField,
         c2YTextField,
         c3YTextField,
         c4YTextBox,
                h2sYTextField1,
                nitrogenYTextField
        };
        for (var field: textFields) {
            field.setTextFormatter(createDecimalFormatter());
        }
        var packagePath = getClass().getPackageName().split("\\.");
        packagePath[packagePath.length-1] = "Fonts";
        String fontPath = Arrays.stream(packagePath).reduce("", (s, s2) -> s + "/" + s2);
        Font font = Font.loadFont(
                getClass().getResourceAsStream(fontPath + "/FiraCode-Retina.ttf"),
                15
        );
        answerArea.setFont(font);
        calculateBtn.setOnAction(e -> {
            try {
                calculate();
            } catch (Exception ex) {
                ex.printStackTrace();
                final var errorDialog = createErrorDialog(getStage(), ex);
                errorDialog.show();
            }
        });
        setUpIcons();
    }

    private void setUpIcons() {
        var packagePath = getClass().getPackageName().split("\\.");
        packagePath[packagePath.length-1] = "Images";
        String fontPath = Arrays.stream(packagePath).reduce("", (s, s2) -> s + "/" + s2);
        final var facebookImage = getClass().getResource(fontPath + "/facebook.png");
        final var linkedInImage = getClass().getResource(fontPath + "/linkedin.png");
        final var emailImage = getClass().getResource(fontPath + "/email.png");
        facebookIcon.setImage(new Image(Objects.requireNonNull(facebookImage).toString()));
        linkedInIcon.setImage(new Image(Objects.requireNonNull(linkedInImage).toString()));
        emailIcon.setImage(new Image(Objects.requireNonNull(emailImage).toString()));
        facebookIcon.setPickOnBounds(true);
        linkedInIcon.setPickOnBounds(true);
        emailIcon.setPickOnBounds(true);
        facebookIcon.setOnMouseClicked(e -> {
            openBrowser("https://www.facebook.com/Moustafa.essam.hpp");
        });
        linkedInIcon.setOnMouseClicked(e -> {
            openBrowser("https://www.linkedin.com/in/moustafa-essam-726262174");
        });
        emailIcon.setOnMouseClicked(e -> {
            final var email = "mailto:essam.moustafa15@gmail.com";
            openBrowser(email);
            copyToClipboard(email);
        });
    }

    void openBrowser(String url) {
        application.getHostServices().showDocument(url);
    }

    private void copyToClipboard(String answer) {
        Clipboard.getSystemClipboard().setContent(Map.of(DataFormat.PLAIN_TEXT, answer));
    }

    private final Pattern numbersExpr = Pattern.compile("[-]?[\\d]*[.]?[\\d]*");
    TextFormatter<?> createDecimalFormatter() {
        final var pattern = numbersExpr.pattern();
        return new TextFormatter<>(c -> {
            if (c.getControlNewText().isEmpty()) { return c; }
            final var isGood = c.getControlNewText().matches(pattern);
            if (isGood) { return c; }
            else { return null; }
        });
    }

    void calculate() {
        final float iDmm = getFloat(iDTextField.getText());
        final float loopIDmm = getFloat(loopIDTextField.getText());
        final float totalLength = getFloat(totalLengthTextField.getText());
        final float flowRate = getFloat(flowRateTextField.getText());
        final float flowRateIncrease = getFloat(flowRateIncreaseTextField.getText());
        final float increasedFlowRate = (1 + flowRateIncrease/100.0f) * flowRate;
        final float consumerFlowRate = getFloat(consumerFlowRateTextField.getText());
        final Float p1 = getFloat(p1TextField.getText());
        final Float p2 = getFloat(p2TextField.getText());
        final float tAvg = getFloat(tAvgTextField.getText());
        final float maxPressure = getFloat(maxPressureTextField.getText());
        final float roughness = getFloat(roughnessTextField.getText());
        final Float c1y = getFloat(c1YTextField.getText());
        final Float c2y = getFloat(c2YTextField.getText());
        final Float c3y = getFloat(c3YTextField.getText());
        final Float c4y = getFloat(c4YTextBox.getText());
        final Float nitrogenY = getFloat(nitrogenYTextField.getText());
        final Float h2sY = getFloat(h2sYTextField1.getText());

        final var task = new Service<GasPipeResult>() {
            @Override
            protected Task<GasPipeResult> createTask() {
                return new Task<>() {
                    @Override
                    protected GasPipeResult call() {
                        final var gasLine = new GasLine();
                        return gasLine.gasLine(
                                iDmm,
                                loopIDmm,
                                totalLength,
                                flowRate,
                                increasedFlowRate,
                                consumerFlowRate,
                                p2,
                                p1,
                                tAvg,
                                maxPressure,
                                roughness,
                                c1y,
                                c2y,
                                c3y,
                                c4y,
                                nitrogenY, h2sY);
                    }
                };
            }
        };
        final var loadingDialog = createProgressAlert(getStage(), task);
        task.setOnRunning(e -> {
            loadingDialog.show();
            System.out.println("STARTED");
        });
        task.setOnSucceeded(e -> {
            final var result = task.getValue();
            drawLines(result.getBeforeLines(), result.getLoops(), result.getAfterLines(), result.getComplementaryLines());
            setAnswer(result.getSteps());
            loadingDialog.close();
        });
        task.setOnFailed(e -> {
            loadingDialog.close();
            final var error = e.getSource().getException();
            final var errorDialog = createErrorDialog(getStage(), error);
            errorDialog.show();
            setAnswer(error.getMessage());
        });
        task.setOnCancelled(e -> {
            loadingDialog.close();
        });
        task.restart();
    }

    Float getFloat(String value) {
        try {
            return Float.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    Integer getInteger(String value) {
        try {
            return Integer.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    void setAnswer(String answer) {
        answerArea.setText(answer);
    }

    Alert createErrorDialog(Stage owner, Throwable e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(owner);
        alert.setTitle("Error");
        alert.setContentText(e.getMessage());
        return alert;
    }

    Alert createProgressAlert(Stage owner, Service<?> task) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.initOwner(owner);
        alert.titleProperty().bind(task.titleProperty());
        alert.contentTextProperty().bind(task.messageProperty());

        ProgressIndicator pIndicator = new ProgressIndicator();
        pIndicator.progressProperty().bind(task.progressProperty());
        alert.setGraphic(pIndicator);
        alert.setHeaderText("Loading...");

        alert.getDialogPane().getButtonTypes().add(ButtonType.OK);
        alert.getDialogPane().lookupButton(ButtonType.OK)
                .disableProperty().bind(task.runningProperty());

        alert.getDialogPane().cursorProperty().bind(
                Bindings.when(task.runningProperty())
                        .then(Cursor.WAIT)
                        .otherwise(Cursor.DEFAULT)
        );
        return alert;
    }

    private void drawLines(List<GasPipe> beforeLines, List<GasPipe> loops, List<GasPipe> afterLines, List<GasPipe> complementaryLines) {
        XYChart.Series<Number, Number> beforeSeries = new XYChart.Series();
        beforeSeries.setName("Before");
        for (var l: beforeLines) {
            for (var p: l.generateHG()) {
                beforeSeries.getData().add(new XYChart.Data(p.getX(), p.getY()));
            }
        }

        final List<XYChart.Series<Number, Number>> loopsSeries = new ArrayList<>();
        for (var l: loops) {
            XYChart.Series<Number, Number> loopSeries = new XYChart.Series();
            loopSeries.setName("Loop");
            for (var p: l.generateHG()) {
                loopSeries.getData().add(new XYChart.Data(p.getX(), p.getY()));
            }
            loopsSeries.add(loopSeries);
        }

        XYChart.Series<Number, Number> afterSeries = new XYChart.Series();
        afterSeries.setName("after");
        for (var l: afterLines) {
            for (var p: l.generateHG()) {
                afterSeries.getData().add(new XYChart.Data(p.getX(), p.getY()));
            }
        }

        final List<XYChart.Series<Number, Number>> complementarySeries = new ArrayList<>();
        for (var l: complementaryLines) {
            XYChart.Series<Number, Number> loopSeries = new XYChart.Series();
            loopSeries.setName("Complementary");
            for (var p: l.generateHG()) {
                loopSeries.getData().add(new XYChart.Data(p.getX(), p.getY()));
            }
            complementarySeries.add(loopSeries);
        }

        //Defining the x an y axes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();

        //Setting labels for the axes
        xAxis.setLabel("L(mile)");
        yAxis.setLabel("P(psi)");

        LineChart<Number, Number> hydraulicGradient = new LineChart<Number, Number>(xAxis, yAxis);
        hydraulicGradient.getData().addAll(complementarySeries);
        hydraulicGradient.getData().addAll(beforeSeries, afterSeries);
        hydraulicGradient.getData().addAll(loopsSeries);

        for (var item: hydraulicGradient.getData()) {
            for (XYChart.Data<Number, Number> entry : item.getData()) {
                Tooltip t = new Tooltip("(" + String.format("%.2f", Math.abs((float) entry.getXValue())) + " , " + entry.getYValue().toString() + ")");
                t.setShowDelay(new Duration(50));
                Tooltip.install(entry.getNode(), t);
            }
        }

        //Creating a stack pane to hold the chart
        StackPane pane = new StackPane(hydraulicGradient);
        pane.setPadding(new Insets(15, 15, 15, 15));
        pane.setStyle("-fx-background-color: BEIGE");
        //Setting the Scene
        Scene scene = new Scene(pane, 595, 350);
        if (chartsWindow != null) {
            chartsWindow.close();
        }
        chartsWindow = new Stage();
        chartsWindow.initOwner(getStage());
        chartsWindow.setTitle("HG");
        chartsWindow.setScene(scene);
        chartsWindow.show();
    }
}
