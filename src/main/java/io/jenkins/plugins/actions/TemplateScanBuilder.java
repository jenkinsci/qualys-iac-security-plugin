package io.jenkins.plugins.actions;

import io.jenkins.plugins.commons.JenkinsUtil;
import io.jenkins.plugins.commons.model.*;
import io.jenkins.plugins.commons.model.Util;
import io.jenkins.plugins.commons.service.IQualysService;
import io.jenkins.plugins.validation.UIJenkinsValidation;
import io.jenkins.plugins.validation.UIValidation;
import io.jenkins.plugins.commons.service.impl.QualysServiceImpl;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.*;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import io.jenkins.plugins.jenkins.dto.QualysApiConfiguration;
import java.io.File;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.kohsuke.stapler.AncestorInPath;

@Extension
public class TemplateScanBuilder extends Builder implements SimpleBuildStep {

    @Setter
    @Getter
    private String IaCServiceEndpoint;

    @Setter
    @Getter
    private String scanName;

    @Setter
    @Getter
    private String scanDirectories;

    @DataBoundSetter
    @Setter
    private boolean isFailedResultsOnly;

    @DataBoundSetter
    @Setter
    @Getter
    private boolean buildFailureSettings;

    @Setter
    @Getter
    private String high;

    @Setter
    @Getter
    private String medium;

    @Setter
    @Getter
    private String low;

    @DataBoundSetter
    @Setter
    @Getter
    private boolean timeoutSettings;

    @Setter
    @Getter
    private String scanResultInterval;

    @Setter
    @Getter
    private String totalJobCompletionTime;

    @Setter
    @Getter
    private String jobName;

    private static final String FINAL_STATUS = "FINISHED";
    private static final String ERROR_STATUS = "ERROR";
    private final io.jenkins.plugins.commons.model.Util util = io.jenkins.plugins.commons.model.Util.getInstance();
    private final UIValidation uIValidation = new UIJenkinsValidation();

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @DataBoundConstructor
    public TemplateScanBuilder(String IaCServiceEndpoint, String scanName, String scanDirectories, boolean isFailedResultsOnly, boolean buildFailureSettings, String high, String medium, String low, boolean timeoutSettings, String scanResultInterval, String totalJobCompletionTime, String jobName) {
        this.IaCServiceEndpoint = IaCServiceEndpoint;
        if (uIValidation.validateScanName(scanName).isValid()) {
            this.scanName = scanName;
        } else {
            this.scanName = "";
        }
        if (uIValidation.validateFolderPaths(scanDirectories, Jenkins.get().getRootDir().getPath() + File.separator + "workspace" + File.separator + jobName).isValid()) {
            this.scanDirectories = scanDirectories.replaceAll("\\.{2,}", "").replaceAll("[\\/]+", "/").replaceAll("[\\\\]+", "\\\\");
        } else {
            this.scanDirectories = "";
        }
        this.isFailedResultsOnly = isFailedResultsOnly;
        this.buildFailureSettings = buildFailureSettings;
        if (uIValidation.validateNumber(high, 0, Integer.MAX_VALUE).isValid()) {
            this.high = high;
        } else {
            this.high = "0";
        }
        if (uIValidation.validateNumber(medium, 0, Integer.MAX_VALUE).isValid()) {
            this.medium = medium;
        } else {
            this.medium = "0";
        }
        if (uIValidation.validateNumber(low, 0, Integer.MAX_VALUE).isValid()) {
            this.low = low;
        } else {
            this.low = "0";
        }
        this.timeoutSettings = timeoutSettings;
        if (uIValidation.validateNumber(scanResultInterval, QualysConstants.MIN_SCAN_RESULTS_INTERVAL, Integer.MAX_VALUE).isValid()) {
            this.scanResultInterval = scanResultInterval;
        } else {
            this.scanResultInterval = QualysConstants.DEFAULT_SCAN_RESULTS_INTERVAL;
        }
        if (uIValidation.validateNumber(totalJobCompletionTime, QualysConstants.MIN_JOB_COMPLETION_TIME, Integer.MAX_VALUE).isValid()) {
            this.totalJobCompletionTime = totalJobCompletionTime;
        } else {
            this.totalJobCompletionTime = QualysConstants.DEFAULT_JOB_COMPLETION_TIME;
        }
    }

    public TemplateScanBuilder() {
    }

    public boolean getFailedResultsOnly() {
        return isFailedResultsOnly;
    }

    public String getFormattedScanName() {
        if (StringUtils.isEmpty(scanName.trim())) {
            return "jenkins_" + getSelectedIaCServiceEndpoint().getQualysUserName() + "_" + Instant.now().getEpochSecond();
        }
        return scanName;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    public QualysApiConfiguration getSelectedIaCServiceEndpoint() {
        for (QualysApiConfiguration qac : getDescriptor().getIaCServiceEndpoints()) {
            if (getIaCServiceEndpoint() != null && getIaCServiceEndpoint().equals(qac.getName())) {
                return qac;
            }
        }
        // If no installation match then take the first one
        if (getDescriptor().getIaCServiceEndpoints().length > 0) {
            return getDescriptor().getIaCServiceEndpoints()[0];
        }
        return null;
    }

    @SneakyThrows
    @SuppressFBWarnings({"DM_EXIT", "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE", "DLS_DEAD_LOCAL_STORE", "DM_STRING_VOID_CTOR"})
    public void perform(@NonNull Run<?, ?> run, @NonNull FilePath workspace, @NonNull EnvVars env, @NonNull Launcher launcher,
            @NonNull TaskListener listener) {
        listener.getLogger().println("Qualys IaC Scan Started");
        QualysApiConfiguration qac = getSelectedIaCServiceEndpoint();
        listener.getLogger().println("Configuration name : " + qac.getName());
        listener.getLogger().println("Qualys Platform URL : " + qac.getQualysPlatformURL());
        listener.getLogger().println("Qualys Username : " + qac.getQualysUserName());
        listener.getLogger().println("Scan Name : " + getFormattedScanName());
        listener.getLogger().println("Directory Path : " + getScanDirectories());
        listener.getLogger().println("Failed Results only : " + getFailedResultsOnly());
        listener.getLogger().println(MessageFormat.format("Build Failure Conditions High={0}, Medium={1}, Low={2}", getHigh(), getMedium(), getLow()));
        listener.getLogger().println(MessageFormat.format("Timeout settings scanResultInterval={0}, jobCompletionTotalTime={1}", getScanResultInterval(), getTotalJobCompletionTime()));
        IQualysService iQualysService = new QualysServiceImpl();
        QualysBuildConfiguration qbc = new QualysBuildConfiguration(qac.getQualysPlatformURL(), qac.getQualysUserName(), qac.getQualysPassword().getPlainText(), getFailedResultsOnly(), StringUtils.isEmpty(getScanName()) ? getFormattedScanName() : getScanName(), getScanDirectories());
        io.jenkins.plugins.commons.model.Util util = Util.getInstance();
        String DEFAULT_WORKSPACE_DIR = util.concatPath(util.concatPath(Jenkins.get().getRootDir().getPath() + File.separator + "workspace", workspace.getName()), QualysConstants.EMPTY_STRING);
        listener.getLogger().println(MessageFormat.format("Workspace directory : {0}", DEFAULT_WORKSPACE_DIR));
        if (qbc.isCredentialsBlank()) {
            throw new AbortException("Unable to launch Qualys IaC Scan due to platform  url, username or password is blank.");
        }
        if (!iQualysService.isUserAuthenticated(qbc)) {
            throw new AbortException("Unable to launch Qualys IaC Scan due to invalid platform  url, username or password.");
        }
        if (StringUtils.isBlank(getScanDirectories())) {
            throw new AbortException("Unable to launch Qualys IaC Scan due to scan directories are blank.");
        }
        Map<String, Object> map = iQualysService.postZip(DEFAULT_WORKSPACE_DIR, qbc);
        if (map.get(QualysConstants.HTTP_POST_FAILED) != null && !Boolean.parseBoolean(map.get(QualysConstants.HTTP_POST_FAILED).toString())) {
            String scanUuid = map.get(QualysConstants.KEY_SCAN_UUID).toString();
            listener.getLogger().println("Qualys IaC Scan ID : " + scanUuid);
            if (!StringUtils.isEmpty(scanUuid)) {
                boolean isBuildFailed = false;
                Instant start = Instant.now();
                while (true) {
                    try {
                        String scanStatus = iQualysService.getScanStatus(scanUuid, qbc);
                        if (StringUtils.isNotEmpty(scanStatus)) {
                            listener.getLogger().println(MessageFormat.format("Qualys scan status : {0}", scanStatus));
                            if (scanStatus.equalsIgnoreCase(FINAL_STATUS)) {
                                try {
                                    FailedStats failedStatsFromBuildConfiguration = new FailedStats(getHigh(), getMedium(), getLow());
                                    String scanResult = iQualysService.getScanResult(scanUuid, qbc);
                                    String fileName = "ScanResult"+ File.separator+ env.get("BUILD_NUMBER").toString() + File.separator +"Qualys_IaC_Scan_Reponse_" + env.get("BUILD_NUMBER").toString() + ".json";
                                    String filePath = DEFAULT_WORKSPACE_DIR + fileName;
                                    FileUtils.writeStringToFile(new File(filePath), scanResult, Charset.forName("UTF-8"));
                                    listener.getLogger().println("Qualys IaC Scan Json Response saved  at location : " + filePath);
                                    ScanResult scanResultObj = iQualysService.mapScanResult(scanResult, failedStatsFromBuildConfiguration);
                                    if (scanResultObj.getSummary() != null) {
                                        scanResultObj.setScanId(scanUuid);
                                        scanResultObj.setScanName(qbc.getScanName());
                                        scanResultObj.setFailedResultsOnly(getFailedResultsOnly());
                                        scanResultObj.setAppliedBuildSetting(isBuildFailureSettings());
                                        scanResultObj.setQualysJsonResponse(scanResult);
                                        scanResultObj.setScanStatus(scanStatus);
                                        run.addAction(new ScanResultAction(listener, scanResultObj));
                                        if (isBuildFailureSettings()) {
                                            FailedStats scanResultFailedStats = scanResultObj.getSummary().getFailedStats();
                                            isBuildFailed = iQualysService.checkBuildFailed(scanResultFailedStats, failedStatsFromBuildConfiguration);
                                            listener.getLogger().println(MessageFormat.format("Scan result failed stats High={0}, Medium={1}, Low={2}", scanResultFailedStats.getHigh(), scanResultFailedStats.getMedium(), scanResultFailedStats.getLow()));
                                        }
                                        listener.getLogger().println("Qualys IaC scan result mapping ended and IaC scan report generated.");
                                    } else {
                                        listener.getLogger().println("The scan is FINISHED, but the scan result is empty. Check the scan configuration files.");
                                    }
                                    break;
                                } catch (IOException | URISyntaxException ex) {
                                    Logger.getLogger(TemplateScanBuilder.class.getName()).log(Level.SEVERE, null, ex);
                                    break;
                                }
                            } else if (scanStatus.equalsIgnoreCase(ERROR_STATUS)) {
                                throw new AbortException("Getting error after IaC scan");
                            }
                        }
                        Thread.sleep(1000 * Integer.parseInt(getScanResultInterval()));
                    } catch (InterruptedException ex) {
                        Logger.getLogger(TemplateScanBuilder.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Instant end = Instant.now();
                    long durationInMinutes = Duration.between(start, end).toMinutes();
                    if (durationInMinutes > Integer.parseInt(getTotalJobCompletionTime())) {
                        throw new AbortException("Qualys IaC scan taking more than " + getTotalJobCompletionTime() + " minutes, so ignoring IaC Scan report.");
                    }
                }
                if (isBuildFailed) {
                    throw new AbortException("After comparing build failure settings IaC scan result causing build failure.");
                }
            }
        } else {
            listener.getLogger().println(map.get(QualysConstants.HTTP_POST_FAILED_REASON).toString());
            throw new AbortException("Unable to post files for IaC Scan.");
        }
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private final UIValidation uIValidation = new UIJenkinsValidation();
        public String getUUID() {
            return java.util.UUID.randomUUID().toString();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Qualys IaC Scan";
        }

        public QualysApiConfiguration[] getIaCServiceEndpoints() {
            if (GlobalConfiguration.all() != null) {
                Config config = GlobalConfiguration.all().get(Config.class);
                if (config != null) {
                    return config.getQualysApiConfigurations();
                }
            }
            QualysApiConfiguration[] qualysApiConfigurations = new QualysApiConfiguration[QualysConstants.DEFAULT_LENGTH];
            return qualysApiConfigurations;
        }

        public FormValidation doCheckIaCServiceEndpoint(@QueryParameter String IaCServiceEndpoint, @QueryParameter String isPageLoad) {
            return JenkinsUtil.showErrorIfExists(uIValidation.validateIaCServiceEndpoint(IaCServiceEndpoint, "-1"), isPageLoad);
        }

        public FormValidation doCheckScanName(@QueryParameter String scanName, @QueryParameter String isPageLoad) {
            return JenkinsUtil.showErrorIfExists(uIValidation.validateScanName(scanName), isPageLoad);
        }

        public FormValidation doCheckScanDirectories(@AncestorInPath Job job, @QueryParameter String isPageLoad, @QueryParameter String scanDirectories) {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            return JenkinsUtil.showErrorIfExists(uIValidation.validateFolderPaths(scanDirectories, Jenkins.get().getRootDir().getPath() + File.separator + "workspace" + File.separator + job.getDisplayName()), isPageLoad);
        }

        public FormValidation doCheckHigh(@QueryParameter String high, @QueryParameter String isPageLoad) {
            return JenkinsUtil.showErrorIfExists(uIValidation.validateNumber(high, 0, Integer.MAX_VALUE), isPageLoad);
        }

        public FormValidation doCheckMedium(@QueryParameter String medium, @QueryParameter String isPageLoad) {
            return JenkinsUtil.showErrorIfExists(uIValidation.validateNumber(medium, 0, Integer.MAX_VALUE), isPageLoad);
        }

        public FormValidation doCheckLow(@QueryParameter String low, @QueryParameter String isPageLoad) {
            return JenkinsUtil.showErrorIfExists(uIValidation.validateNumber(low, 0, Integer.MAX_VALUE), isPageLoad);
        }

        public FormValidation doCheckScanResultInterval(@QueryParameter String scanResultInterval, @QueryParameter String isPageLoad) {
            return JenkinsUtil.showErrorIfExists(uIValidation.validateNumber(scanResultInterval, QualysConstants.MIN_SCAN_RESULTS_INTERVAL, QualysConstants.MAX_SCAN_RESULTS_INTERVAL), isPageLoad);
        }

        public FormValidation doCheckTotalJobCompletionTime(@QueryParameter String totalJobCompletionTime, @QueryParameter String isPageLoad) {
            return JenkinsUtil.showErrorIfExists(uIValidation.validateNumber(totalJobCompletionTime, QualysConstants.MIN_JOB_COMPLETION_TIME, QualysConstants.MAX_JOB_COMPLETION_TIME), isPageLoad);
        }
    }
}
