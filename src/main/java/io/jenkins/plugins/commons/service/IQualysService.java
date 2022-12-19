package io.jenkins.plugins.commons.service;

import io.jenkins.plugins.commons.model.QualysBuildConfiguration;
import io.jenkins.plugins.commons.model.ScanResult;
import io.jenkins.plugins.commons.model.FailedStats;

import java.util.Map;

public interface IQualysService {

    public boolean isUserAuthenticated(QualysBuildConfiguration qbc);

    public Map<String, Object> postZip(String workspacePath, QualysBuildConfiguration qbc);

    public ScanResult mapScanResult(String json, FailedStats failedStatsFromBuildConfiguration);

    public String getScanResult(String scanUuid, QualysBuildConfiguration qbc);

    public String getScanStatus(String scanUuid, QualysBuildConfiguration qbc);
    
    public boolean checkBuildFailed(FailedStats failedStatsFromScanResult, FailedStats failedStatsFromBuildConfiguration);
}
