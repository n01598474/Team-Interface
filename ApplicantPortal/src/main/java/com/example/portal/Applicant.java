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
 * The class intentionally has NO fields for age, date of birth, marital or
 * family status, homeownership, or arrest history.
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

    // - export -
    /** Writes CSV if filename ends w/ .csv, else JSON. */
    public void exportTo(Path file) throws IOException {
        boolean csv = file.getFileName().toString().toLowerCase().endsWith(".csv");
        Files.writeString(file, csv ? toCsv() : toJson(), StandardCharsets.UTF_8);
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder("{\n");
        sb.append("  \"fullName\": ").append(q(fullName)).append(",\n");
        sb.append("  \"address\": ").append(q(address)).append(",\n");
        sb.append("  \"phone\": ").append(q(phone)).append(",\n");
        sb.append("  \"email\": ").append(q(email)).append(",\n");
        sb.append("  \"eligibleToWorkInUS\": ").append(eligibleToWorkInUS).append(",\n");
        sb.append("  \"needsSponsorship\": ").append(needsSponsorship).append(",\n");
        sb.append("  \"shiftPreference\": ").append(q(shiftPreference)).append(",\n");
        sb.append("  \"weeklyHours\": ").append(q(weeklyHours)).append(",\n");

        sb.append("  \"availableDays\": [");
        for (int i = 0; i < availableDays.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(q(availableDays.get(i)));
        }
        sb.append("],\n");

        sb.append("  \"workHistory\": [");
        for (int i = 0; i < employmentHistory.size(); i++) {
            Employment e = employmentHistory.get(i);
            sb.append(i == 0 ? "\n" : ",\n");
            sb.append("    {")
                    .append("\"employer\": ").append(q(e.getEmployer())).append(", ")
                    .append("\"position\": ").append(q(e.getPosition())).append(", ")
                    .append("\"startDate\": ").append(q(e.getStartDate())).append(", ")
                    .append("\"endDate\": ").append(q(e.getEndDate())).append(", ")
                    .append("\"reasonForLeaving\": ").append(q(e.getReasonForLeaving()))
                    .append("}");
        }
        sb.append(employmentHistory.isEmpty() ? "" : "\n  ").append("]\n}\n");
        return sb.toString();
    }

    /** Flat CSV: one line per work-history record, applicant columns repeated on each line. */
    public String toCsv() {
        List<String> lines = new ArrayList<>();
        lines.add("fullName,address,phone,email,eligibleToWorkInUS,needsSponsorship,shiftPreference,"
                + "weeklyHours,availableDays,employer,position,startDate,endDate,reasonForLeaving");

        String common = String.join(",",
                csv(fullName), csv(address), csv(phone), csv(email),
                String.valueOf(eligibleToWorkInUS), String.valueOf(needsSponsorship),
                csv(shiftPreference), csv(weeklyHours), csv(String.join(" ", availableDays)));

        if (employmentHistory.isEmpty()) {
            lines.add(common + ",,,,,");
        }
        for (Employment e : employmentHistory) {
            lines.add(common + "," + String.join(",",
                    csv(e.getEmployer()), csv(e.getPosition()), csv(e.getStartDate()),
                    csv(e.getEndDate()), csv(e.getReasonForLeaving())));
        }
        return String.join("\n", lines) + "\n";
    }

    // - helpers -
    private static String nullToEmpty(String s) { return s == null ? "" : s.trim(); }

    private static String q(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.append('"').toString();
    }

    private static String csv(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
