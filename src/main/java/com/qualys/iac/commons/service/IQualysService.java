package com.qualys.iac.commons.service;

import com.qualys.iac.commons.model.FailedStats;
import com.qualys.iac.commons.model.QualysBuildConfiguration;
import com.qualys.iac.commons.model.ScanResult;
import java.util.Map;

public interface IQualysService {

    public boolean isUserAuthenticated(QualysBuildConfiguration qbc);

    public Map<String, Object> postZip(String workspacePath, QualysBuildConfiguration qbc);

    public ScanResult mapScanResult(String json, FailedStats failedStatsFromBuildConfiguration);

    public String getScanResult(String scanUuid, QualysBuildConfiguration qbc);

    public String getScanStatus(String scanUuid, QualysBuildConfiguration qbc);
    
    public boolean checkBuildFailed(FailedStats failedStatsFromScanResult, FailedStats failedStatsFromBuildConfiguration);
}
