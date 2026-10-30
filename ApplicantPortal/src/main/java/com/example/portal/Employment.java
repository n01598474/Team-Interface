package com.example.portal;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/** One row of the Work History TableView. Properties make the cells editable and live. */
public class Employment {
    private final StringProperty employer = new SimpleStringProperty("");
    private final StringProperty position = new SimpleStringProperty("");
    private final StringProperty startDate = new SimpleStringProperty("");
    private final StringProperty endDate = new SimpleStringProperty("");
    private final StringProperty reasonForLeaving = new SimpleStringProperty("");

    public StringProperty employerProperty() { return employer; }
    public StringProperty positionProperty() { return position; }
    public StringProperty startDateProperty() { return startDate; }
    public StringProperty endDateProperty() { return endDate; }
    public StringProperty reasonForLeavingProperty() { return reasonForLeaving; }

    public String getEmployer() { return employer.get(); }
    public String getPosition() { return position.get(); }
    public String getStartDate() { return startDate.get(); }
    public String getEndDate() { return endDate.get(); }
    public String getReasonForLeaving() { return reasonForLeaving.get(); }
}
