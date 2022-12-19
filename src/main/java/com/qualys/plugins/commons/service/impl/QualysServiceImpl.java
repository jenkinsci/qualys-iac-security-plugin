package com.qualys.plugins.commons.service.impl;

import com.qualys.plugins.commons.model.*;
import com.qualys.plugins.commons.service.IQualysService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipOutputStream;

public class QualysServiceImpl implements IQualysService {

    private final Util util = Util.getInstance();

    @Override
    public boolean isUserAuthenticated(QualysBuildConfiguration qbc) {
        HttpResponse<Void> response;
        try {
            HttpRequest request = util.addCommonConfigurationToHttpRequest(qbc)
                    .uri(new URI(qbc.getAuthenticationURL()))
                    .GET()
                    .build();
            response = util.addCommonConfigurationToHttpClient(QualysConstants.CONNECTION_TIMEOUT)
                    .build()
                    .send(request, HttpResponse.BodyHandlers.discarding());
            return (response.statusCode() != HttpServletResponse.SC_UNAUTHORIZED);
        } catch (IOException | InterruptedException | URISyntaxException ex) {
            Logger.getLogger(QualysServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public static boolean isValidPath(String folderPath) {
        File file = new File(folderPath);
        return file != null && (file.isDirectory() || file.isFile()) && file.exists();
    }

    @Override
    public Map<String, Object> postZip(String workspacePath, QualysBuildConfiguration qbc) {
        HttpResponse<String> response = null;
        Map<String, Object> map = new HashMap<>();
        List<String> lstDirs = qbc.getFormattedDirectories();
        List<Path> lstPaths = new ArrayList<>();
        Set<String> lstEntries = new HashSet<>();
        List<String> lstfiles = new ArrayList<>();
        try {
            for (int i = 0; i < lstDirs.size(); i++) {
                String folderPath = lstDirs.get(i);
                if (isValidPath(folderPath)) {
                    if (folderPath.endsWith(QualysConstants.ZIP_EXTENSION)) {
                        String extractFolderPath = util.getRandomZipPath();
                        util.extractFolder(folderPath, extractFolderPath);
                        lstPaths.add(Paths.get(extractFolderPath));
                    } else {
                        lstPaths.add(Paths.get(new File(folderPath).getCanonicalPath()));
                    }
                    continue;
                }
                folderPath = util.concatPath(workspacePath, folderPath);
                if (isValidPath(folderPath)) {
                    lstPaths.add(Paths.get(new File(folderPath).getCanonicalPath()));
                }
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try ( ZipOutputStream zipOutputStream = new ZipOutputStream(baos)) {
                for (Path path : lstPaths) {
                    if (path.toFile().isDirectory()) {
                        util.addFolderToZip(path.toFile(), zipOutputStream, QualysConstants.EMPTY_BASE_PATH, lstEntries, lstfiles);
                    } else {
                        util.addFileToZip(path.toFile(), zipOutputStream, QualysConstants.EMPTY_BASE_PATH, lstEntries, lstfiles);
                    }
                }
                zipOutputStream.flush();
                zipOutputStream.close();
                baos.close();
                byte bytes[] = baos.toByteArray();
                if (bytes.length == 0 || lstfiles.isEmpty()) {
                    map.put(QualysConstants.HTTP_POST_FAILED, true);
                    map.put(QualysConstants.HTTP_POST_FAILED_REASON, QualysConstants.NO_IAC_FILES_MESSAGE);
                    return map;
                }
                float sizeInMB = bytes.length / (1024f * 1024f);
                if (sizeInMB > QualysConstants.ZIP_FILE_MAX_SIZE) {
                    map.put(QualysConstants.HTTP_POST_FAILED, true);
                    map.put(QualysConstants.HTTP_POST_FAILED_REASON, QualysConstants.ZIP_FILE_MAX_SIZE_MESSAGE);
                    return map;
                }
            }

            HttpEntity httpEntity = MultipartEntityBuilder.create()
                    .addTextBody(QualysConstants.KEY_NAME, qbc.getScanName())
                    .addTextBody(QualysConstants.KEY_FAILED_RESULTS_ONLY, qbc.isFailedResultsOnly() + "")
                    .addBinaryBody(QualysConstants.KEY_FILE, new ByteArrayInputStream(baos.toByteArray()), ContentType.APPLICATION_OCTET_STREAM, util.appendTimestampToFile(QualysConstants.FILE_NAME, QualysConstants.ZIP_EXTENSION))
                    .build();
            Pipe pipe = Pipe.open();

            // Pipeline streams must be used in a multi-threaded environment. Using one
            // thread for simultaneous reads and writes can lead to deadlocks.
            new Thread(() -> {
                try ( OutputStream outputStream = Channels.newOutputStream(pipe.sink())) {
                    // Write the encoded data to the pipeline.
                    httpEntity.writeTo(outputStream);
                } catch (IOException iOException) {
                    Logger.getLogger(QualysServiceImpl.class.getName()).log(Level.SEVERE, null, iOException);
                }

            }).start();
            HttpRequest request = util.addCommonConfigurationToHttpRequest(qbc)
                    .uri(new URI(qbc.getPostScanURL()))
                    .header(QualysConstants.KEY_CONTENT_TYPE, httpEntity.getContentType().getValue())
                    .POST(HttpRequest.BodyPublishers.ofInputStream(() -> Channels.newInputStream(pipe.source())))
                    .build();
            response = util.addCommonConfigurationToHttpClient(QualysConstants.CONNECTION_TIMEOUT)
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            map.put(QualysConstants.HTTP_POST_FAILED, false);
        } catch (IOException | InterruptedException | URISyntaxException ex) {
            map.put(QualysConstants.HTTP_POST_FAILED, true);
            map.put(QualysConstants.HTTP_POST_FAILED_REASON, ex.toString());
            Logger.getLogger(QualysServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (!Boolean.getBoolean(map.get(QualysConstants.HTTP_POST_FAILED).toString())) {
            map.put(QualysConstants.KEY_SCAN_UUID, new JSONObject(response.body()).get(QualysConstants.KEY_SCAN_UUID).toString());
        }
        return map;
    }

    @Override
    public ScanResult mapScanResult(String json, FailedStats failedStatsFromBuildConfiguration) {
        Summary summaryObj = null;
        JSONObject jsonObject = new JSONObject(json.trim());
        JSONArray results = jsonObject.getJSONArray(QualysConstants.KEY_RESULT);
        List<Checks> lstChecks = new ArrayList<>();
        List<Remediation> lstRemediation = new ArrayList<>();
        List<ParsingError> lstParsingErrors = new ArrayList<>();
        List<JSONArray> lstCheckList = new ArrayList<>();
        int totalBuildFailureControlCount = 0, tmpPassed = 0, tmpFailed = 0, tmpHigh = 0, tmpMedium = 0, tmpLow = 0, tmpSkipped = 0, tmpParsingError = 0;
        boolean isHighViolatesCriteria, isMediumViolatesCriteria, isLowViolatesCriteria;
        isHighViolatesCriteria = isMediumViolatesCriteria = isLowViolatesCriteria = false;
        for (int i = 0; i < results.length(); i++) {
            JSONObject tmp = results.getJSONObject(i);
            JSONObject result = tmp.getJSONObject(QualysConstants.KEY_RESULTS);
            JSONArray passedChecks = result.optJSONArray(QualysConstants.KEY_PASSED_CHECKS);
            JSONArray failedChecks = result.optJSONArray(QualysConstants.KEY_FAILED_CHECKS);
            JSONArray parsingErrors = result.optJSONArray(QualysConstants.KEY_PARSING_ERRORS);

            String checkType = tmp.getString(QualysConstants.KEY_CHECK_TYPE);

            if (passedChecks != null) {
                lstCheckList.add(passedChecks);
            }
            if (failedChecks != null) {
                lstCheckList.add(failedChecks);
            }
            if (parsingErrors != null) {
                StringBuilder parsingErrorBuilder = new StringBuilder();
                for (int k = 0; k < parsingErrors.length(); k++) {
                    parsingErrorBuilder.append(parsingErrors.get(k));
                    ParsingError parsingError = new ParsingError(checkType, parsingErrorBuilder.toString());
                    lstParsingErrors.add(parsingError);
                }
            }
            JSONObject summary = tmp.getJSONObject(QualysConstants.KEY_SUMMARY);
            JSONObject failedStats = summary.getJSONObject(QualysConstants.KEY_FAILED_STATS);

            if (failedStats != null) {
                tmpHigh = tmpHigh + failedStats.getInt(QualysConstants.KEY_HIGH);
                tmpMedium = tmpMedium + failedStats.getInt(QualysConstants.KEY_MEDIUM);
                tmpLow = tmpLow + failedStats.getInt(QualysConstants.KEY_LOW);
                if (tmpHigh > failedStatsFromBuildConfiguration.getHigh()) {
                    isHighViolatesCriteria = true;
                    totalBuildFailureControlCount = totalBuildFailureControlCount + tmpHigh;
                }
                if (tmpMedium > failedStatsFromBuildConfiguration.getMedium()) {
                    isMediumViolatesCriteria = true;
                    totalBuildFailureControlCount = totalBuildFailureControlCount + tmpMedium;
                }
                if (tmpLow > failedStatsFromBuildConfiguration.getLow()) {
                    isLowViolatesCriteria = true;
                    totalBuildFailureControlCount = totalBuildFailureControlCount + tmpLow;
                }
            }
            tmpPassed = tmpPassed + summary.getInt(QualysConstants.KEY_PASSED);
            tmpFailed = tmpFailed + summary.getInt(QualysConstants.KEY_FAILED);
            tmpSkipped = tmpSkipped + summary.getInt(QualysConstants.KEY_SKIPPED);
            tmpParsingError = tmpParsingError + summary.getInt(QualysConstants.KEY_PARSING_ERRORS);
        }

        for (int i = 0; i < lstCheckList.size(); i++) {
            for (int j = 0; j < lstCheckList.get(i).length(); j++) {
                JSONObject item = lstCheckList.get(i).getJSONObject(j);
                JSONObject checkResult = item.getJSONObject(QualysConstants.KEY_CHECK_RESULT);
                String controlId = item.getString(QualysConstants.KEY_CHECK_ID);
                String controlName = item.getString(QualysConstants.KEY_CHECK_NAME);
                String criticality = item.getString(QualysConstants.KEY_CRITICALITY);
                String passOrFailResult = checkResult.getString(QualysConstants.KEY_RESULT);
                String filePath = item.getString(QualysConstants.KEY_FILE_PATH);
                String resource = item.getString(QualysConstants.KEY_RESOURCE);
                if (passOrFailResult.equalsIgnoreCase("FAILED")) {
                    Remediation remediation = new Remediation(controlId, item.getString(QualysConstants.KEY_REMEDIATION));
                    lstRemediation.add(remediation);
                }
                lstChecks.add(new Checks(controlId, controlName, criticality, passOrFailResult, filePath, resource));
            }
        }
        lstChecks.sort(Comparator.comparing(Checks::getResource).thenComparing(Checks::getControlId));
        summaryObj = new Summary(tmpPassed, tmpFailed, new FailedStats(tmpHigh, tmpMedium, tmpLow), tmpSkipped, tmpParsingError, isHighViolatesCriteria, isMediumViolatesCriteria, isLowViolatesCriteria, totalBuildFailureControlCount);
        return new ScanResult(summaryObj, lstChecks, lstRemediation, lstParsingErrors);
    }

    @Override
    public String getScanStatus(String scanUuid, QualysBuildConfiguration qbc) {
        HttpResponse<String> response;
        try {
            HttpRequest request = util.addCommonConfigurationToHttpRequest(qbc)
                    .uri(new URI(qbc.getScanStatusURL(scanUuid)))
                    .GET()
                    .build();
            response = util.addCommonConfigurationToHttpClient(QualysConstants.CONNECTION_TIMEOUT)
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return StringUtils.isEmpty(response.body()) ? null : getScanStatus(response.body());

        } catch (IOException | InterruptedException | URISyntaxException ex) {
            Logger.getLogger(QualysServiceImpl.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private String getScanStatus(String body) {
        JSONArray jSONArray = (JSONArray) new JSONObject(body).get("content");
        if (jSONArray.length() > 0) {
            return new JSONObject(jSONArray.get(0).toString()).getString("status");
        }
        return null;
    }

    @Override
    public String getScanResult(String scanUuid, QualysBuildConfiguration qbc) {
        HttpResponse<String> response;
        try {
            HttpRequest request = util.addCommonConfigurationToHttpRequest(qbc)
                    .uri(new URI(qbc.getScanResultURL(scanUuid)))
                    .GET()
                    .build();
            response = util.addCommonConfigurationToHttpClient(QualysConstants.CONNECTION_TIMEOUT)
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return StringUtils.isEmpty(response.body()) ? null : response.body();

        } catch (IOException | InterruptedException | URISyntaxException ex) {
            Logger.getLogger(QualysServiceImpl.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public boolean checkBuildFailed(FailedStats failedStatsFromScanResult, FailedStats failedStatsFromBuildConfiguration) {
        if (failedStatsFromScanResult.getHigh() > failedStatsFromBuildConfiguration.getHigh()) {
            return true;
        } else if (failedStatsFromScanResult.getMedium() > failedStatsFromBuildConfiguration.getMedium()) {
            return true;
        } else if (failedStatsFromScanResult.getLow() > failedStatsFromBuildConfiguration.getLow()) {
            return true;
        }
        return false;
    }
}
