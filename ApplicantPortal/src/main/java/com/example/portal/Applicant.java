package com.example.portal;

package com.example.portal;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Data model: stores form input/validates/exports it.
 *
 * The class intentionally has NO fields for age, date of birth, marital or
 *  family status, homeownership, or arrest history.
 */

public class Applicant {

    /** Field ids shared with the controller so each error can be tied to a control. */
    public static final String F_NAME = "fullName";
    public static final String F_ADDRESS = "address";
    public static final String F_PHONE = "phone";
    public static final String F_EMAIL = "email";
    public static final String F_ELIGIBLE = "eligible";
    public static final String F_SHIFT = "shift";
    public static final String F_HISTORY = "history";

    public record ValidationError(String fieldId, String message) { }

    private static final Pattern PHONE_CHARS = Pattern.compile("^[0-9()+\\-.\\s]+$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private String fullName = "";
    private String address = "";
    private String phone = "";
    private String email = "";
    private boolean eligibleToWorkInUS;
    private boolean needsSponsorship;
    private String shiftPreference = "";
    private String weeklyHours = "";
    private List<String> availableDays = new ArrayList<>();
    private final ObservableList<Employment> employmentHistory = FXCollections.observableArrayList();
}
