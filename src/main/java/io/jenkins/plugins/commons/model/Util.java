package io.jenkins.plugins.commons.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;


import javax.servlet.http.HttpServletResponse;



/**
 * This is class provides functions related to extract zip and zip folder. It
 * contains common methods regarding adding headers to http request
 */
public final class Util {

    private static Util util = null;
    public static final String EMPTY_BASE_PATH = "";

    //Making constructor private so only one instance of class get created
    private Util() {
    }

    //This method instantiates class object
    public static Util getInstance() {
        if (util == null) {
            util = new Util();
        }
        return util;
    }

    public String getBasicAuthToken(String userName, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((userName + ":" + password).getBytes(Charset.forName("UTF-8")));
    }

    public String getAuthorizationHeader(QualysBuildConfiguration qbc) {
        if (String.valueOf(qbc.getAuthType()).equalsIgnoreCase("OAUTH"))
            return "Bearer " + this.generateJwtTokenUsingClientIdAndClientSecret(qbc);
        return this.getBasicAuthToken(qbc.getUserName(), qbc.getPassword());
    }

    public HttpRequest.Builder addCommonConfigurationToHttpRequest(QualysBuildConfiguration qbc) {
        return HttpRequest.newBuilder()
                .setHeader("Authorization", this.getAuthorizationHeader(qbc))
                .setHeader("Accept", "application/json");
    }

    public String generateJwtTokenUsingClientIdAndClientSecret(QualysBuildConfiguration qbc) {
        String apiUrl = qbc.getGatewayUrl()+qbc.getOauthEndPointUrl();
        System.out.println("Requesting new auth token using clientId and clientSecret from API Gateway Server:" + apiUrl);
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(apiUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("clientId", qbc.getUserName())
                    .header("clientSecret", qbc.getPassword())
                    .header("X-Requested-With", "Qualys")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody())   // empty body
                    .build();
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == HttpServletResponse.SC_OK) {
                System.out.println("Successfully received auth token from API Gateway Server.");
                return response.body().toString();
            } else
                System.out.println("Error while generating JWT token.");
        } catch (IOException e) {
            System.out.println("Error while generating JWT token " + e.getMessage());
        } catch (InterruptedException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    public HttpClient.Builder addCommonConfigurationToHttpClient(long connectionTimeout) {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMinutes(connectionTimeout));
    }

    public String concatPath(String firstPath, String secondPath) {
        return firstPath.concat(File.separator).concat(secondPath);
    }

    public String getTempDirectory() {
        return System.getProperty("java.io.tmpdir");
    }

    public String appendTimestampToFile(String fileName, String extension) {
        return fileName.concat("_").concat(Instant.now().getEpochSecond() + "").concat(extension);
    }

    public String getRandomZipPath() {
        String uuid = UUID.randomUUID().toString();
        return util.concatPath(getTempDirectory(), uuid);
    }

    public void extractFolder(String zipFile, String extractFolderPath) {
        //String extractFolderPath = util.getRandomZipPath()
        //extractFolder have unique file path e.g %temp%/{GUID}/ in windows and in linux like /tmp/{GUID}/ so zip slip will not occur
        try {
            int BUFFER = 2048;
            File file = new File(zipFile);

            ZipFile zip = new ZipFile(file);
            File extractFolderDir = new File(extractFolderPath);
            boolean isDirectoryCreated = false;
            if (!extractFolderDir.exists()) {
                isDirectoryCreated = extractFolderDir.mkdirs();
            }
            if (isDirectoryCreated) {
                Enumeration zipFileEntries = zip.entries();

                // Process each entry
                while (zipFileEntries.hasMoreElements()) {
                    // grab a zip file entry
                    ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                    String currentEntry = entry.getName();
                    String canonicalDestinationDirPath = extractFolderDir.getCanonicalPath();
                    File destFile = new File(extractFolderPath, currentEntry);
                    String canonicalDestinationFile = destFile.getCanonicalPath();
                    if (!canonicalDestinationFile.startsWith(canonicalDestinationDirPath + File.separator)) {
                        throw new ArchiveException("Entry is outside of the target dir: " + entry.getName());
                    }
                    //destFile = new File(newPath, destFile.getName());
                    File destinationParent = destFile.getParentFile();

                    // create the parent directory structure if needed
                    boolean isDestinationParentCreated = destinationParent.mkdirs();
                    if (isDestinationParentCreated && !entry.isDirectory()) {
                        try (BufferedInputStream is = new BufferedInputStream(zip
                                .getInputStream(entry))) {
                            int currentByte;
                            // establish buffer for writing file
                            byte data[] = new byte[BUFFER];

                            // write the current file to disk
                            FileOutputStream fos = new FileOutputStream(destFile);
                            BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

                            // read and write until last byte is encountered
                            while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                                dest.write(data, 0, currentByte);
                            }
                            dest.flush();
                            dest.close();
                            fos.close();
                            is.close();
                        }
                    }
                }
            }
            zip.close();
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ArchiveException e) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public String getRenamedPath(String path, String basePath, File file, Set<String> lstEntries) {
        if (lstEntries.contains(path)) {
            return util.concatPath(basePath.concat(appendTimestampToFile(file.getName(),
                    FilenameUtils.getExtension(file.getName()))), EMPTY_BASE_PATH);
        }
        return path;
    }

    public void addFileToZip(File file, ZipOutputStream zip, String basePath, Set<String> lstEntries, List<String> lstfiles) {
        try {
            if (isIaCScanFile(file.getName())) {
                String path = basePath + file.getName();
                path = getRenamedPath(path, basePath, file, lstEntries);
                lstEntries.add(path);
                ZipEntry zipEntry = new ZipEntry(path);
                zip.putNextEntry(zipEntry);
                IOUtils.copy(new FileInputStream(file), zip);
                zip.closeEntry();
                lstfiles.add(file.getName());
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addFolderToZip(File folder, ZipOutputStream zout, String basePath, Set<String> lstEntries, List<String> lstfiles) throws IOException {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String path = util.concatPath(basePath.concat(file.getName()), EMPTY_BASE_PATH);
                    path = getRenamedPath(path, basePath, file, lstEntries);
                    lstEntries.add(path);
                    zout.putNextEntry(new ZipEntry(path));
                    addFolderToZip(file, zout, path, lstEntries, lstfiles);
                    zout.closeEntry();
                } else {
                    addFileToZip(file, zout, basePath, lstEntries, lstfiles);
                }
            }
        }
    }

    // Function to validate image file extension .
    public static boolean isIaCScanFile(String str) {
        // Regex to check valid image file extension.
        String regex
                = "([^\\s]+(\\.(?i)(tf|yml|yaml|json|template|TF|YML|YAML|JSON|TEMPLATE))$)";

        // Compile the ReGex
        Pattern p = Pattern.compile(regex);

        // If the string is empty
        // return false
        if (str == null) {
            return false;
        }

        // Pattern class contains matcher() method
        // to find matching between given string
        // and regular expression.
        Matcher m = p.matcher(str);

        // Return if the string
        // matched the ReGex
        return m.matches();
    }
}
