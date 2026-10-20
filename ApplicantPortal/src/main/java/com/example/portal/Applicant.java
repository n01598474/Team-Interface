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

    // - accessors -
    public String getFullName() { return fullName; }
    public void setFullName(String v) { fullName = nullToEmpty(v); }
    public String getAddress() { return address; }
    public void setAddress(String v) { address = nullToEmpty(v); }
    public String getPhone() { return phone; }
    public void setPhone(String v) { phone = nullToEmpty(v); }
    public String getEmail() { return email; }
    public void setEmail(String v) { email = nullToEmpty(v); }
    public boolean isEligibleToWorkInUS() { return eligibleToWorkInUS; }
    public void setEligibleToWorkInUS(boolean v) { eligibleToWorkInUS = v; }
    public boolean isNeedsSponsorship() { return needsSponsorship; }
    public void setNeedsSponsorship(boolean v) { needsSponsorship = v; }
    public String getShiftPreference() { return shiftPreference; }
    public void setShiftPreference(String v) { shiftPreference = nullToEmpty(v); }
    public String getWeeklyHours() { return weeklyHours; }
    public void setWeeklyHours(String v) { weeklyHours = nullToEmpty(v); }
    public List<String> getAvailableDays() { return availableDays; }
    public void setAvailableDays(List<String> v) { availableDays = new ArrayList<>(v); }
    public ObservableList<Employment> getEmploymentHistory() { return employmentHistory; }
    }

    // - validation -
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (fullName.isBlank()) {
            errors.add(new ValidationError(F_NAME, "Enter your full name."));
        }
        if (address.isBlank()) {
            errors.add(new ValidationError(F_ADDRESS, "Enter your mailing address."));
        }
        String digits = phone.replaceAll("\\D", "");
        if (!PHONE_CHARS.matcher(phone).matches() || digits.length() < 10 || digits.length() > 15) {
            errors.add(new ValidationError(F_PHONE, "Enter a phone number with area code, like (555) 010-0142."));
        }
        if (!EMAIL.matcher(email.trim()).matches()) {
            errors.add(new ValidationError(F_EMAIL, "Enter an email address like name@example.com."));
        }
        if (!eligibleToWorkInUS) {
            errors.add(new ValidationError(F_ELIGIBLE, "Confirm that you are legally eligible to work in the United States."));
        }
        if (shiftPreference.isBlank()) {
            errors.add(new ValidationError(F_SHIFT, "Choose a preferred shift."));
        }

        List<Integer> incompleteRows = new ArrayList<>();
        for (int i = 0; i < employmentHistory.size(); i++) {
            Employment e = employmentHistory.get(i);
            if (e.getEmployer().isBlank() || e.getPosition().isBlank()) {
                incompleteRows.add(i + 1);
            }
        }
        if (!incompleteRows.isEmpty()) {
            errors.add(new ValidationError(F_HISTORY,
                    "Work history row " + incompleteRows.toString().replaceAll("[\\[\\]]", "")
                            + " needs an employer and a position."));
        }
        return errors;
    }
