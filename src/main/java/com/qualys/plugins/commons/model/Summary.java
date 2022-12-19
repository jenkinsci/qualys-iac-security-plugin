package com.qualys.plugins.commons.model;

import lombok.Getter;
import lombok.Setter;

public class Summary {

    @Getter
    @Setter
    private int passed;
    @Getter
    @Setter
    private int failed;
    @Getter
    @Setter
    private FailedStats failedStats;

    @Getter
    @Setter
    private int skipped;

    @Getter
    @Setter
    private int parsingErrors;

    @Getter
    @Setter
    private int totalBuildFailureControlCount;

    @Getter
    @Setter
    private boolean isHighViolatesCriteria;

    @Getter
    @Setter
    private boolean isMediumViolatesCriteria;

    @Getter
    @Setter
    private boolean isLowViolatesCriteria;

    public Summary(int passed, int failed, FailedStats failedStats, int skipped, int parsingErrors, boolean isHighViolatesCriteria, boolean isMediumViolatesCriteria, boolean isLowViolatesCriteria, int totalBuildFailureControlCount) {
        this.passed = passed;
        this.failed = failed;
        this.failedStats = failedStats;
        this.skipped = skipped;
        this.parsingErrors = parsingErrors;
        this.isHighViolatesCriteria = isHighViolatesCriteria;
        this.isMediumViolatesCriteria = isMediumViolatesCriteria;
        this.isLowViolatesCriteria = isLowViolatesCriteria;
        this.totalBuildFailureControlCount = totalBuildFailureControlCount;
    }
}
