package com.qualys.iac.commons.model;

import lombok.Getter;
import lombok.Setter;

public class Remediation {

    @Getter
    @Setter
    private String controlId;
    @Getter
    @Setter
    private String remediation;

    public Remediation(String controlId, String remediation) {
        this.controlId = controlId;
        this.remediation = remediation;
    }

}
