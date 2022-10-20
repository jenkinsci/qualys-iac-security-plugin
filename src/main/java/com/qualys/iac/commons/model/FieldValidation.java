package com.qualys.iac.commons.model;

public class FieldValidation {

    private boolean isValid;
    private ErrorMessage errorMessage;

    public FieldValidation(boolean isValid, ErrorMessage errorMessage) {
        this.isValid = isValid;
        this.errorMessage = errorMessage;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setIsValid(boolean isValid) {
        this.isValid = isValid;
    }

    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(ErrorMessage errorMessage) {
        this.errorMessage = errorMessage;
    }

}
