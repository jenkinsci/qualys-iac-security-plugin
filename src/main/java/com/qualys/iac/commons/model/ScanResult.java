package com.qualys.iac.commons.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class ScanResult {

    @Getter
    @Setter
    private Summary summary;
    @Getter
    @Setter
    private List<Checks> lstterraFormChecks;
    @Getter
    @Setter
    private List<Remediation> lstremediation;
    @Getter
    @Setter
    private List<ParsingError> lstParsingErrors;

    @Getter
    @Setter
    private String scanId;

    @Getter
    @Setter
    private String scanName;


    @Getter
    @Setter
    private boolean isAppliedBuildSetting;

    @Getter
    @Setter
    private String qualysJsonResponse;

    @Getter
    @Setter
    private String scanStatus;

    public ScanResult(Summary summary, List<Checks> lstterraFormChecks, List<Remediation> lstremediation, List<ParsingError> lstParsingErrors) {
        this.summary = summary;
        this.lstterraFormChecks = lstterraFormChecks;
        this.lstremediation = lstremediation;
        this.lstParsingErrors = lstParsingErrors;
    }
}
