package io.jenkins.plugins.commons.model;

import lombok.Getter;
import lombok.Setter;

public class Checks {

    @Getter
    @Setter
    private String controlId;
    @Getter
    @Setter
    private String controlName;

    @Getter
    @Setter
    String criticality;

    @Getter
    @Setter
    String resultType;

    @Getter
    @Setter
    private String filePath;
    
    @Getter
    @Setter
    private String resource;

    public Checks(String controlId, String controlName, String level, String resultType, String filePath, String resource) {
        this.controlId = controlId;
        this.controlName = controlName;
        this.criticality = level;
        this.resultType = resultType;
        this.filePath = filePath;
        this.resource = resource;
    }

}
