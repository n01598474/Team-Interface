package com.example.portal;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.beans.property.StringProperty;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MainController {

    // Applicant data
    private final Applicant applicant = new Applicant();

    // Toolbar controls
    @FXML private Button themeButton;
    @FXML private CheckBox dyslexiaFontCheckBox;

    // Main layout
    @FXML private ScrollPane mainScrollPane;

    // Applicant information
    @FXML private TextField fullNameField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;

    // Work eligibility
    @FXML private CheckBox eligibleCheckBox;
    @FXML private CheckBox sponsorshipCheckBox;

    // Availability
    @FXML private ComboBox<String> shiftComboBox;
    @FXML private TextField weeklyHoursField;

    @FXML private CheckBox mondayCheckBox;
    @FXML private CheckBox tuesdayCheckBox;
    @FXML private CheckBox wednesdayCheckBox;
    @FXML private CheckBox thursdayCheckBox;
    @FXML private CheckBox fridayCheckBox;
    @FXML private CheckBox saturdayCheckBox;
    @FXML private CheckBox sundayCheckBox;

    // Employment history
    @FXML private TableView<Employment> employmentTable;
    @FXML private TableColumn<Employment, String> employerColumn;
    @FXML private TableColumn<Employment, String> positionColumn;
    @FXML private TableColumn<Employment, String> startDateColumn;
    @FXML private TableColumn<Employment, String> endDateColumn;
    @FXML private TableColumn<Employment, String> reasonColumn;

    // Error display
    @FXML private Label errorSummaryLabel;

    // Appearance settings
    // 0 = Light, 1 = Dark, 2 = High Contrast
    private int themeIndex = 0;
    private double fontSize = 14;

    private final Map<String, Control> errorControls = new HashMap<>();

    @FXML
    private void initialize() {

        // Configure employment table
        employmentTable.setEditable(true);

        configureEditableColumn(
                employerColumn,
                Employment::employerProperty
        );

        configureEditableColumn(
                positionColumn,
                Employment::positionProperty
        );

        configureEditableColumn(
                startDateColumn,
                Employment::startDateProperty
        );

        configureEditableColumn(
                endDateColumn,
                Employment::endDateProperty
        );

        configureEditableColumn(
                reasonColumn,
                Employment::reasonForLeavingProperty
        );

        employmentTable.setItems(applicant.getEmploymentHistory());

        // Available shifts
        shiftComboBox.getItems().addAll(
                "Morning",
                "Afternoon",
                "Evening",
                "Overnight",
                "Flexible"
        );

        // Map validation errors to their corresponding controls
        errorControls.put(Applicant.F_NAME, fullNameField);
        errorControls.put(Applicant.F_ADDRESS, addressField);
        errorControls.put(Applicant.F_PHONE, phoneField);
        errorControls.put(Applicant.F_EMAIL, emailField);
        errorControls.put(Applicant.F_ELIGIBLE, eligibleCheckBox);
        errorControls.put(Applicant.F_SHIFT, shiftComboBox);
        errorControls.put(Applicant.F_HISTORY, employmentTable);

        errorSummaryLabel.setVisible(false);
        errorSummaryLabel.setManaged(false);

        // Apply the initial theme after the scene has been created
        Platform.runLater(this::applyAppearance);
    }

    // --------------------------------------------------
    // EDITABLE EMPLOYMENT TABLE
    // --------------------------------------------------

    private void configureEditableColumn(
            TableColumn<Employment, String> column,
            Function<Employment, StringProperty> propertyAccessor) {

        column.setCellValueFactory(
                cellData -> propertyAccessor.apply(cellData.getValue())
        );

        column.setCellFactory(col -> new TextFieldTableCell<>() {

            private TextField editor;
            private boolean focusListenerInstalled = false;

            @Override
            public void startEdit() {
                super.startEdit();

                if (isEditing() && getGraphic() instanceof TextField textField) {

                    editor = textField;

                    if (!focusListenerInstalled) {

                        editor.focusedProperty().addListener(
                                (observable, wasFocused, isFocused) -> {

                                    if (!isFocused && isEditing() && editor != null) {
                                        commitEdit(editor.getText());
                                    }
                                }
                        );

                        focusListenerInstalled = true;
                    }
                }
            }
        });

        column.setOnEditCommit(event ->
                propertyAccessor.apply(event.getRowValue())
                        .set(event.getNewValue())
        );
    }

    // --------------------------------------------------
    // THEME AND ACCESSIBILITY
    // --------------------------------------------------

    @FXML
    private void handleThemeToggle() {
        themeIndex = (themeIndex + 1) % 3;
        applyAppearance();
    }

    @FXML
    private void handleFontIncrease() {
        fontSize = Math.min(fontSize + 2, 24);
        applyAppearance();
    }

    @FXML
    private void handleFontDecrease() {
        fontSize = Math.max(fontSize - 2, 10);
        applyAppearance();
    }

    @FXML
    private void handleDyslexiaFontToggle() {
        applyAppearance();
    }

    private void applyAppearance() {

        if (mainScrollPane == null || mainScrollPane.getScene() == null) {
            return;
        }

        var scene = mainScrollPane.getScene();

        String themeFile;

        switch (themeIndex) {
            case 1 -> themeFile = "dark.css";
            case 2 -> themeFile = "high-contrast.css";
            default -> themeFile = "light.css";
        }

        String baseCss = MainController.class
                .getResource("base.css").toExternalForm();

        String themeCss = MainController.class
                .getResource(themeFile).toExternalForm();

        // Load the base stylesheet and selected theme
        scene.getStylesheets().setAll(baseCss, themeCss);

        Node root = scene.getRoot();

        // Apply font preferences
        String fontFamily = dyslexiaFontCheckBox.isSelected()
                ? "Verdana"
                : "System";

        root.setStyle(
                "-fx-font-size: " + fontSize + "px;" +
                        "-fx-font-family: '" + fontFamily + "';"
        );

        // Update button text to show the next theme
        String nextTheme = switch (themeIndex) {
            case 0 -> "Dark";
            case 1 -> "High Contrast";
            default -> "Light";
        };

        themeButton.setText("Switch to " + nextTheme);
    }

    // --------------------------------------------------
    // EMPLOYMENT HISTORY
    // --------------------------------------------------

    @FXML
    private void handleAddEmployment() {

        Employment employment = new Employment();

        applicant.getEmploymentHistory().add(employment);

        employmentTable.getSelectionModel().select(employment);
        employmentTable.scrollTo(employment);
        employmentTable.requestFocus();
    }

    @FXML
    private void handleRemoveEmployment() {

        Employment selected = employmentTable
                .getSelectionModel()
                .getSelectedItem();

        if (selected == null) {
            showAlert(
                    Alert.AlertType.INFORMATION,
                    "No Employment Selected",
                    "Select an employment record before removing it."
            );
            return;
        }

        applicant.getEmploymentHistory().remove(selected);
    }

    @FXML
    private void handleClearForm() {

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);

        confirmation.setTitle("Clear Application");
        confirmation.setHeaderText("Clear the entire application?");
        confirmation.setContentText(
                "This will erase all applicant information and employment history."
        );

        var result = confirmation.showAndWait();

        if (result.isEmpty() ||
                result.get() != ButtonType.OK) {
            return;
        }

        // Clear applicant information
        fullNameField.clear();
        addressField.clear();
        phoneField.clear();
        emailField.clear();

        // Clear work eligibility
        eligibleCheckBox.setSelected(false);
        sponsorshipCheckBox.setSelected(false);

        // Clear availability
        shiftComboBox.getSelectionModel().clearSelection();
        weeklyHoursField.clear();

        mondayCheckBox.setSelected(false);
        tuesdayCheckBox.setSelected(false);
        wednesdayCheckBox.setSelected(false);
        thursdayCheckBox.setSelected(false);
        fridayCheckBox.setSelected(false);
        saturdayCheckBox.setSelected(false);
        sundayCheckBox.setSelected(false);

        // Clear employment history
        employmentTable.edit(-1, null);
        applicant.getEmploymentHistory().clear();

        // Reset stored applicant information
        applicant.setFullName("");
        applicant.setAddress("");
        applicant.setPhone("");
        applicant.setEmail("");
        applicant.setEligibleToWorkInUS(false);
        applicant.setNeedsSponsorship(false);
        applicant.setShiftPreference("");
        applicant.setWeeklyHours("");
        applicant.setAvailableDays(new ArrayList<>());

        // Clear validation errors
        clearErrorHighlights();

        errorSummaryLabel.setText("");
        errorSummaryLabel.setVisible(false);
        errorSummaryLabel.setManaged(false);

        // Return focus to the first field
        fullNameField.requestFocus();
    }

    // --------------------------------------------------
    // FORM SUBMISSION AND VALIDATION
    // --------------------------------------------------

    @FXML
    private void handleSubmit() {

        collectApplicantInformation();

        clearErrorHighlights();

        List<Applicant.ValidationError> errors = applicant.validate();

        if (!errors.isEmpty()) {
            displayErrors(errors);
            return;
        }

        errorSummaryLabel.setText("");
        errorSummaryLabel.setVisible(false);
        errorSummaryLabel.setManaged(false);

        showExportDialog();
    }

    private void collectApplicantInformation() {

        applicant.setFullName(fullNameField.getText());
        applicant.setAddress(addressField.getText());
        applicant.setPhone(phoneField.getText());
        applicant.setEmail(emailField.getText());

        applicant.setEligibleToWorkInUS(
                eligibleCheckBox.isSelected()
        );

        applicant.setNeedsSponsorship(
                sponsorshipCheckBox.isSelected()
        );

        applicant.setShiftPreference(
                shiftComboBox.getValue() == null
                        ? ""
                        : shiftComboBox.getValue()
        );

        applicant.setWeeklyHours(weeklyHoursField.getText());

        List<String> availableDays = new ArrayList<>();

        if (mondayCheckBox.isSelected()) availableDays.add("Monday");
        if (tuesdayCheckBox.isSelected()) availableDays.add("Tuesday");
        if (wednesdayCheckBox.isSelected()) availableDays.add("Wednesday");
        if (thursdayCheckBox.isSelected()) availableDays.add("Thursday");
        if (fridayCheckBox.isSelected()) availableDays.add("Friday");
        if (saturdayCheckBox.isSelected()) availableDays.add("Saturday");
        if (sundayCheckBox.isSelected()) availableDays.add("Sunday");

        applicant.setAvailableDays(availableDays);
    }

    private void displayErrors(
            List<Applicant.ValidationError> errors) {

        StringBuilder summary = new StringBuilder(
                "Please correct the following issues:\n\n"
        );

        for (Applicant.ValidationError error : errors) {

            summary.append("• ")
                    .append(error.message())
                    .append("\n");

            Control control = errorControls.get(error.fieldId());

            if (control != null) {
                control.setStyle(
                        "-fx-border-color: #D32F2F;" +
                                "-fx-border-width: 2px;"
                );
            }
        }

        errorSummaryLabel.setText(summary.toString());
        errorSummaryLabel.setVisible(true);
        errorSummaryLabel.setManaged(true);

        // Move focus and scroll to the first invalid control
        Control firstInvalid = errorControls.get(
                errors.get(0).fieldId()
        );

        if (firstInvalid != null) {
            scrollIntoView(firstInvalid);
        }
    }

    private void clearErrorHighlights() {

        for (Control control : errorControls.values()) {
            control.setStyle("");
        }
    }

    // --------------------------------------------------
    // SCROLL TO INVALID CONTROL
    // --------------------------------------------------

    private void scrollIntoView(Control control) {

        Node content = mainScrollPane.getContent();

        if (content == null) {
            return;
        }

        Bounds sceneBounds = control.localToScene(
                control.getBoundsInLocal()
        );

        Bounds contentBounds = content.sceneToLocal(sceneBounds);

        double contentHeight = content.getBoundsInLocal().getHeight();
        double viewportHeight = mainScrollPane.getViewportBounds().getHeight();

        double maximumScroll = contentHeight - viewportHeight;

        if (maximumScroll > 0) {

            double targetPosition = contentBounds.getMinY();

            double scrollPosition = targetPosition / maximumScroll;

            mainScrollPane.setVvalue(
                    Math.max(0, Math.min(1, scrollPosition))
            );
        }

        control.requestFocus();
    }

    // --------------------------------------------------
    // JSON AND CSV EXPORT
    // --------------------------------------------------

    private void showExportDialog() {

        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Save Employment Application");

        fileChooser.setInitialFileName("employment-application.json");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(
                        "JSON Files", "*.json"
                ),
                new FileChooser.ExtensionFilter(
                        "CSV Files", "*.csv"
                )
        );

        Window window = mainScrollPane.getScene().getWindow();

        File file = fileChooser.showSaveDialog(window);

        if (file == null) {
            return;
        }

        try {

            Path exportPath = file.toPath();

            applicant.exportTo(exportPath);

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Application Exported",
                    "Your application was successfully saved to:\n"
                            + exportPath.toAbsolutePath()
            );

        } catch (IOException exception) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Export Failed",
                    "The application could not be saved:\n"
                            + exception.getMessage()
            );
        }
    }

    // --------------------------------------------------
    // ALERT HELPER
    // --------------------------------------------------

    private void showAlert(
            Alert.AlertType type,
            String title,
            String message) {

        Alert alert = new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}
