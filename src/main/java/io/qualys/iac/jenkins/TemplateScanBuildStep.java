package io.qualys.iac.jenkins;

import io.qualys.iac.commons.model.QualysConstants;
import io.qualys.iac.commons.model.Util;
import io.qualys.iac.validation.UIJenkinsValidation;
import io.qualys.iac.validation.UIValidation;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import io.qualys.iac.commons.JenkinsUtil;
import io.qualys.iac.jenkins.dto.QualysApiConfiguration;

import java.io.File;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;

import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class TemplateScanBuildStep extends Step {

    @Setter
    @Getter
    private String IaCServiceEndpoint;

    @Setter
    @Getter
    private String scanName;

    @Setter
    @Getter
    private String scanDirectories;

    private boolean isFailedResultsOnly;

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

    private static final String DEFAULT_WORKSPACE_DIR = Jenkins.get().getRootDir().getPath() + File.separator + "workspace";
    private final Util util = Util.getInstance();
    private final UIValidation uIValidation = new UIJenkinsValidation();

    @DataBoundConstructor
    public TemplateScanBuildStep(String IaCServiceEndpoint, String scanName, String scanDirectories, boolean isFailedResultsOnly, boolean buildFailureSettings, String high, String medium, String low, boolean timeoutSettings, String scanResultInterval, String totalJobCompletionTime, String jobName) {
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
        this.jobName = jobName;
    }

    public TemplateScanBuildStep() {
    }

    public void setIsFailedResultsOnly(boolean isFailedResultsOnly) {
        this.isFailedResultsOnly = isFailedResultsOnly;
    }

    public boolean getIsFailedResultsOnly() {
        return isFailedResultsOnly;
    }

    public String getFormattedScanName() {
        if (StringUtils.isEmpty(scanName.trim())) {
            return "jenkins_" + getSelectedIaCServiceEndpoint().getQualysUserName() + "_" + Instant.now().getEpochSecond();
        }
        return scanName;
    }

    @Override
    public TemplateScanBuildStep.DescriptorImpl getDescriptor() {
        return (TemplateScanBuildStep.DescriptorImpl) super.getDescriptor();
    }
    public QualysApiConfiguration getSelectedIaCServiceEndpoint() {
        QualysApiConfiguration[] qualysApiConfigurations = getDescriptor().getIaCServiceEndpoints();
        if (qualysApiConfigurations != null && qualysApiConfigurations.length > 0) {
            for (QualysApiConfiguration qac : qualysApiConfigurations) {
                if (getIaCServiceEndpoint() != null && getIaCServiceEndpoint().equals(qac.getName())) {
                    return qac;
                }
            }
            // If no installation match then take the first one
            return qualysApiConfigurations[0];
        }
        return null;
    }

    @Override
    public StepExecution start(StepContext sc) throws Exception {
        TemplateScanBuilder templateScanBuilder = new TemplateScanBuilder(getIaCServiceEndpoint(), getScanName(), getScanDirectories(),
                getIsFailedResultsOnly(), isBuildFailureSettings(), getHigh(),
                getMedium(), getLow(), isTimeoutSettings(), getScanResultInterval(), getTotalJobCompletionTime(), getJobName());
        return new IaCScanBuildExecution(sc, templateScanBuilder);
    }

    @Extension
    public static final class DescriptorImpl extends StepDescriptor {

        private final UIValidation uIValidation = new UIJenkinsValidation();

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

        @Override
        public String getFunctionName() {
            return "qualysIaCScan";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.singleton(Run.class);
        }
    }

    public static final class IaCScanBuildExecution extends StepExecution {

        private static final long serialVersionUID = 1L;
        private final transient TemplateScanBuilder templateScanBuilder;

        private IaCScanBuildExecution(StepContext sc, TemplateScanBuilder templateScanBuilder) {
            super(sc);
            this.templateScanBuilder = templateScanBuilder;
        }

        @Override
        public boolean start() throws Exception {
            StepContext stepContext = getContext();
            templateScanBuilder.perform(stepContext.get(Run.class), stepContext.get(FilePath.class), stepContext.get(EnvVars.class), stepContext.get(Launcher.class), stepContext.get(TaskListener.class));
            getContext().onSuccess(true);
            return true;
        }

        @Override
        public void stop(@Nonnull Throwable cause) throws Exception {
            getContext().onFailure(cause);
        }
    }
}
