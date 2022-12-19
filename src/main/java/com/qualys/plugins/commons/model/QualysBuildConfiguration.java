package com.qualys.plugins.commons.model;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

public class QualysBuildConfiguration {

    @Setter
    private String platformURL;
    @Setter
    @Getter
    private String userName;
    @Setter
    @Getter
    private String password;
    @Setter
    @Getter
    private boolean isFailedResultsOnly;
    @Setter
    @Getter
    private String scanName;
    @Setter
    @Getter
    private String scanDirectories;

    public static final String COMMA_SEPARATOR = ",";

    private static final String QUALYS_SCAN_SUB_URL = "/cloudview-api/rest/v1/iac/scan";
    private static final String QUALYS_RESULT_SUB_URL = "/cloudview-api/rest/v1/iac/scanResult?scanUuid=";
    private static final String QUALYS_SCAN_LIST_WITH_SCAN_UID_SUB_URL = "/cloudview-api/rest/v1/iac/getScanList?filter=scanUuid:";
    private static final String QUALYS_SCAN_LIST_SUB_URL = "/cloudview-api/rest/v1/iac/getScanList";

    public QualysBuildConfiguration() {
    }

    public QualysBuildConfiguration(String platformURL, String userName, String password) {
        this.platformURL = platformURL;
        this.userName = userName;
        this.password = password;
    }

    public QualysBuildConfiguration(String platformURL, String userName, String password, boolean isFailedResultsOnly, String scanName, String scanDirectories) {
        this.platformURL = platformURL;
        this.userName = userName;
        this.password = password;
        this.isFailedResultsOnly = isFailedResultsOnly;
        this.scanName = scanName;
        this.scanDirectories = scanDirectories;
    }

    public String getPlatformURL() {
        return platformURL.replaceAll("/$", "");
    }

    public List<String> getFormattedDirectories() {
        return Arrays.asList(getScanDirectories().split(COMMA_SEPARATOR));
    }

    public String getBasicAuthToken() {
        return "Basic " + Base64.getEncoder().encodeToString((this.userName + ":" + this.password).getBytes(Charset.forName("UTF-8")));
    }

    public String getPostScanURL() {
        return this.getPlatformURL() + QUALYS_SCAN_SUB_URL;
    }

    public String getScanResultURL(String scanUuid) {
        return this.getPlatformURL() + QUALYS_RESULT_SUB_URL + scanUuid;
    }

    public String getScanStatusURL(String scanUuid) {
        return this.getPlatformURL() + QUALYS_SCAN_LIST_WITH_SCAN_UID_SUB_URL + scanUuid;
    }

    public String getAuthenticationURL() {
        return this.getPlatformURL() + QUALYS_SCAN_LIST_SUB_URL;
    }

    public String correctURL(String url) {
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        return url.replaceAll("(?<!(http:|https:))/+", "/");
    }

    public boolean isCredentialsBlank() {
        return StringUtils.isEmpty(this.getPlatformURL()) || StringUtils.isEmpty(this.getUserName()) || StringUtils.isEmpty(this.getPassword());
    }
}
