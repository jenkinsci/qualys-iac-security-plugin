package io.jenkins.plugins.validation;

import io.jenkins.plugins.commons.model.ErrorMessage;
import io.jenkins.plugins.commons.model.FieldValidation;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.validator.routines.UrlValidator;

public abstract class UIValidation {

    public static final String ERROR_FILE_EXTENSION = "Scan file does not have valid extension like .tf,.yml,.yaml or .json extension.";
    public static final String ERROR_EMPTY_USER_NAME = "Username should not be empty.";
    public static final String ERROR_EMPTY_CONFIG_NAME = "Name should not be empty.";
    public static final String ERROR_EMPTY_PASSWORD = "Password should not be empty.";
    public static final String ERROR_EMPTY_PLATFORM_URL = "Platform URL should not be empty.";
    public static final String ERROR_SELECT_IAC_ENDPOINT = "Select IaC Service endpoint.";
    public static final String ERROR_EMPTY_SCAN_NAME = "Scan name should not be empty.";
    public static final String ERROR_EMPTY_FOLDER_PATH = "Folder Path should not be empty.";
    public static final String ERROR_VALID_NUMBER = "Enter valid number.";
    public static final String ERROR_USER_NAME_INVALID = "Username is not valid.";
    public static final String ERROR_FILE_SIZE = "File size should be less than 10 MB.";
    public static final String ERROR_USER_NOT_AUTHORIZED = " is not authorized to perform iac scan.";
    public static final String ERROR_QUALYS_HOST_NOT_FOUND = "Platform url is not valid.";
    public static final String ERROR_QUALYS_CONNECTION_PROBLEM = "Unable to connect qualys host.";
    public static final String ERROR_ECONNRESET = "Unable to read file ";
    public static final String ERROR_EMPTY_SCAN_RESULT = "The scan result is empty.";
    public static final String ERROR_PARSING = "file contents are not parsable.";
    public static final String ERROR_INTERNAL_SERVER = "Internal server error occurred.";
    public static final String RESPONSE_NOT_RECEIVED = "No response received from qualys api endpoint ";
    public static final String ERROR_NOT_FOUND = "Qualys platform url is not valid.";
    public static final String ERROR_FILE_IS_NOT_VALID = "Please select valid file for iac scan.";
    public static final String INVALID_SCAN_NAME = " is invalid, only alphanumeric, hyphen, underscore and whitespace characters are allowed.";
    public static final String INVALID_TAG_KEY = " is invalid, only alphanumeric, hyphen and underscore characters are allowed.";
    public static final String INVALID_TAG_VALUE = " is invalid, only alphanumeric, hyphen, underscore and whitespace characters are allowed.";
    public static final String INVALID_USER_NAME = " is invalid, only alphanumeric, hyphen, underscore,exclamation mark and number sign characters are allowed.";
    public static final String EMPTY_STRING = "";
    public static final String COMMA_SEPARATOR = ",";
    public static final String HTTP_PROTOCOL = "http://";
    public static final String HTTPS_PROTOCOL = "https://";
    public static final String FORWARD_SLASH_CHARACTER = "/";
    public static final String BACKWARD_SLASH_CHARACTER = "\\";
    public static final String USER_NAME_REGEX = "^[A-Za-z0-9-_!#]*$";
    public static final String SCAN_NAME_REGEX = "^[A-Za-z0-9-_ ]*$";
    public static final String KEY_IS_ALL_FOLDER_PATH_EXISTS = "IsAllFolderPathExists";
    public static final String KEY_INVALID_FOLDER_PATHS = "InvalidFolderPaths";
    private static final String[] PLATFORM_URLS = new String[]{"https://qualysguard.qualys.com",
        "https://qualysguard.qg2.apps.qualys.com",
        "https://qualysguard.qg3.apps.qualys.com",
        "https://qualysguard.qg4.apps.qualys.com",
        "https://qualysguard.qualys.eu",
        "https://qualysguard.qg2.apps.qualys.eu",
        "https://qualysguard.qg1.apps.qualys.in",
        "https://qualysguard.qg1.apps.qualys.ca",
        "https://qualysguard.qg1.apps.qualys.ae",
        "https://qualysguard.qg1.apps.qualys.co.uk",
        "https://qualysguard.qg1.apps.qualys.com.au"};

    public boolean containsCaseInsensitive(String s, List<String> l) {
        return l.stream().anyMatch(x -> x.trim().equalsIgnoreCase(s.trim()));
    }

    public FieldValidation validatePlatformURL(String platformURL) throws URISyntaxException, IOException {
        platformURL = platformURL.toLowerCase();
        if (platformURL.trim().isEmpty()) {
            return new FieldValidation(false, new ErrorMessage(ERROR_EMPTY_PLATFORM_URL));

        }
        if (platformURL.endsWith(FORWARD_SLASH_CHARACTER) || platformURL.endsWith(BACKWARD_SLASH_CHARACTER)) {
            platformURL = platformURL.substring(0, platformURL.length() - 1);
        }
        if (platformURL.contains(HTTP_PROTOCOL)) {
            platformURL = platformURL.replace(HTTP_PROTOCOL, HTTPS_PROTOCOL);
        } else if (!platformURL.contains(HTTP_PROTOCOL) && !platformURL.contains(HTTPS_PROTOCOL)) {
            platformURL = HTTPS_PROTOCOL + platformURL;
        }

        return new FieldValidation(true, null);
    }

    public FieldValidation validateConfigName(String configName) {
        configName = configName.trim();
        if (configName.isEmpty()) {
            return new FieldValidation(false, new ErrorMessage(ERROR_EMPTY_CONFIG_NAME));

        } else if (!Pattern.matches(USER_NAME_REGEX, configName)) {
            return new FieldValidation(false, new ErrorMessage(MessageFormat.format("Name {0} {1}", configName, INVALID_USER_NAME)));
        }
        return new FieldValidation(true, null);
    }

    public FieldValidation validateUserName(String userName) {
        userName = userName.trim();
        if (userName.isEmpty()) {
            return new FieldValidation(false, new ErrorMessage(ERROR_EMPTY_USER_NAME));

        } else if (!Pattern.matches(USER_NAME_REGEX, userName)) {
            return new FieldValidation(false, new ErrorMessage(MessageFormat.format("Username {0} {1}", userName, INVALID_USER_NAME)));
        }
        return new FieldValidation(true, null);
    }

    public FieldValidation validatePassword(String password) {
        password = password.trim();
        if (password.isEmpty()) {
            return new FieldValidation(false, new ErrorMessage(ERROR_EMPTY_PASSWORD));
        }
        return new FieldValidation(true, null);
    }

    public FieldValidation validateScanName(String scanName) {
        scanName = scanName.trim().toLowerCase();
        if (!Pattern.matches(SCAN_NAME_REGEX, scanName)) {
            return new FieldValidation(false, new ErrorMessage(MessageFormat.format("Scan name {0} {1}", scanName, INVALID_SCAN_NAME)));
        }
        return new FieldValidation(true, null);
    }

    public FieldValidation validateFolderPaths(String folderPaths, String workspacePath) {
        folderPaths = folderPaths.trim();
        if (folderPaths.isEmpty()) {
            return new FieldValidation(false, new ErrorMessage(ERROR_EMPTY_FOLDER_PATH));
        }
        List<String> lstFolderPath = Arrays.asList(folderPaths.split(COMMA_SEPARATOR));
        Map<String, String> mapFoldersInfo = isFolderPathExists(lstFolderPath, workspacePath);
        if (lstFolderPath.isEmpty() || !Boolean.parseBoolean(mapFoldersInfo.get(KEY_IS_ALL_FOLDER_PATH_EXISTS))) {
            return new FieldValidation(false, new ErrorMessage(MessageFormat.format("Folder path not exists : {0}", mapFoldersInfo.get(KEY_INVALID_FOLDER_PATHS))));
        }
        return new FieldValidation(true, null);
    }

    private Map<String, String> isFolderPathExists(List<String> lstFolderPath, String workspacePath) {
        Map<String, String> mapFoldersInfo = new HashMap<>();
        String tmpFolderPath = "";
        for (String folderPath : lstFolderPath) {
            File fileOrDirectory = new File(folderPath);
            if (!fileOrDirectory.exists()) {
                fileOrDirectory = new File(workspacePath + "/" + folderPath);
                if (!fileOrDirectory.exists()) {
                    tmpFolderPath = tmpFolderPath.concat(folderPath).concat(COMMA_SEPARATOR);
                }
            }
        }
        mapFoldersInfo.put(KEY_IS_ALL_FOLDER_PATH_EXISTS, (tmpFolderPath.isEmpty() ? "true" : "false"));
        mapFoldersInfo.put(KEY_INVALID_FOLDER_PATHS, tmpFolderPath);

        return mapFoldersInfo;
    }

    public FieldValidation validateIaCServiceEndpoint(String iacServiceEndpoint, String defaultValue) {
        if (iacServiceEndpoint.equalsIgnoreCase(defaultValue)) {
            return new FieldValidation(false, new ErrorMessage(ERROR_SELECT_IAC_ENDPOINT));
        }
        return new FieldValidation(true, null);
    }

    public FieldValidation validateNumber(String numberStr, int min, int max) {
        try {
            if (Integer.parseInt(numberStr) >= min && Integer.parseInt(numberStr) <= max) {
                return new FieldValidation(true, null);
            } else {
                return new FieldValidation(false, new ErrorMessage(ERROR_VALID_NUMBER));
            }
        } catch (NumberFormatException exception) {
            return new FieldValidation(false, new ErrorMessage(ERROR_VALID_NUMBER));
        }
    }
}
