package com.qualys.plugins.commons.model;

public class QualysConstants {

    public static final int DEFAULT_LENGTH = 1;
    public static final long CONNECTION_TIMEOUT = 10;
    public final static String KEY_NAME = "name";
    public final static String KEY_FAILED_RESULTS_ONLY = "showOnlyFailedControls";
    public final static String KEY_FILE = "file";
    public final static String KEY_CONTENT_TYPE = "Content-type";
    public final static String KEY_SCAN_UUID = "scanUuid";
    public final static String APPLICATION_URL_ENCODED = "application/x-www-form-urlencoded";
    public final static String FILE_NAME = "QUALYS";
    public static final String KEY_RESULT = "result";
    public static final String KEY_RESULTS = "results";
    public static final String KEY_PASSED_CHECKS = "passedChecks";
    public static final String KEY_PASSED = "passed";
    public static final String KEY_FAILED_CHECKS = "failedChecks";
    public static final String KEY_FAILED_STATS = "failedStats";
    public static final String KEY_FAILED = "failed";
    public static final String KEY_CHECK_ID = "checkId";
    public static final String KEY_CHECK_NAME = "checkName";
    public static final String KEY_CHECK_TYPE = "checkType";
    public static final String KEY_CRITICALITY = "criticality";
    public static final String KEY_CHECK_RESULT = "checkResult";
    public static final String KEY_HIGH = "high";
    public static final String KEY_MEDIUM = "medium";
    public static final String KEY_LOW = "low";
    public static final String KEY_SKIPPED = "skipped";
    public static final String KEY_PARSING_ERRORS = "parsingErrors";
    public static final String KEY_REMEDIATION = "remediation";
    public static final String KEY_SUMMARY = "summary";
    public static final String KEY_FILE_PATH = "filePath";
    public static final String KEY_RESOURCE = "resource";
    public static final String KEY_CONTENT = "content";
    public static final String ZIP_EXTENSION = ".zip";
    public static final String OUTPUT_ZIP_NAME = "Qualys_IaC_Scan";
    public static final String EMPTY_BASE_PATH = "";
    public static final String HTTP_POST_FAILED = "http_post_failed";
    public static final String HTTP_POST_FAILED_REASON = "http_post_failed_reason";
    public static final String ZIP_FILE_MAX_SIZE_MESSAGE = "IaC scan files size exceeding 10 MB.";
    public static final String NO_IAC_FILES_MESSAGE = "There are no valid IaC files(.tf,.yml,.yaml,.json,.template) inside IaC scan directories.";
    public static final float ZIP_FILE_MAX_SIZE = 10.00f;
    public static final int MIN_SCAN_RESULTS_INTERVAL = 10;
    public static final String DEFAULT_SCAN_RESULTS_INTERVAL = "30";
    public static final String DEFAULT_JOB_COMPLETION_TIME = "10";
    public static final int MAX_SCAN_RESULTS_INTERVAL = Integer.MAX_VALUE;
    public static final int MIN_JOB_COMPLETION_TIME = 10;
    public static final int MAX_JOB_COMPLETION_TIME = Integer.MAX_VALUE;

    public static final String EMPTY_STRING = "";

}
