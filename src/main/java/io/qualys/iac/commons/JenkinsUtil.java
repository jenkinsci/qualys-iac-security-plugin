package io.qualys.iac.commons;

import io.qualys.iac.commons.model.FieldValidation;
import hudson.util.FormValidation;

public final class JenkinsUtil {

    private static JenkinsUtil jenkinsUtil = null;

    private JenkinsUtil() {
    }

    public static JenkinsUtil getInstance() {
        if (jenkinsUtil == null) {
            jenkinsUtil = new JenkinsUtil();
        }
        return jenkinsUtil;
    }

    public static FormValidation showErrorIfExists(FieldValidation field, String isPageLoad) {
        if (!field.isValid() && !Boolean.parseBoolean(isPageLoad)) {
            return FormValidation.error(field.getErrorMessage().getMessage());
        }
        return FormValidation.ok();
    }
}
