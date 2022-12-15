package io.qualys.iac.jenkins;

import io.qualys.iac.commons.model.QualysBuildConfiguration;
import hudson.CopyOnWrite;
import hudson.Extension;
import hudson.util.FormValidation;
import io.qualys.iac.commons.service.impl.QualysServiceImpl;
import io.qualys.iac.validation.UIJenkinsValidation;
import io.qualys.iac.validation.UIValidation;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import io.qualys.iac.commons.JenkinsUtil;
import io.qualys.iac.jenkins.dto.QualysApiConfiguration;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.verb.POST;

@Extension
@SuppressWarnings("UUF_UNUSED_FIELD")
public class Config extends GlobalConfiguration {

    private final UIValidation uIValidation = new UIJenkinsValidation();
    private final transient Supplier<Jenkins> supplyJenkins;

    @CopyOnWrite
    private volatile QualysApiConfiguration[] qualysApiConfigurations;

    public Config() {
        this(() -> Optional.ofNullable(Jenkins.getInstanceOrNull()).orElseThrow(() -> new IllegalStateException("Could not get Jenkins instance")));
    }

    public Config(Supplier<Jenkins> supplyJenkins) {
        load();
        this.supplyJenkins = supplyJenkins;
    }

    public static Config get() {
        return GlobalConfiguration.all().get(Config.class);
    }

    public QualysApiConfiguration[] getQualysApiConfigurations() {
        return qualysApiConfigurations.clone();
    }

    public void setQualysApiConfigurations(List<QualysApiConfiguration> qac) {
        List<QualysApiConfiguration> tmp = new ArrayList<>();
        int counter = 0;
        for (int i = 0; i < qac.size(); i++) {
            if (StringUtils.isNotEmpty(qac.get(i).getName())) {
                tmp.add(qac.get(i));
                counter++;
            }
        }
        if (counter > 0) {
            this.qualysApiConfigurations = tmp.stream().toArray(QualysApiConfiguration[] ::new);
        } else {
            this.qualysApiConfigurations = null;
        }
        save();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) {
        List<QualysApiConfiguration> list = req.bindJSONToList(QualysApiConfiguration.class, json.get("inst"));
        setQualysApiConfigurations(list);
        return true;
    }

    public FormValidation doCheckName(@QueryParameter String isPageLoad, @QueryParameter String name) {
        return JenkinsUtil.showErrorIfExists(uIValidation.validateConfigName(name), isPageLoad);
    }

    public FormValidation doCheckQualysUserName(@QueryParameter String isPageLoad, @QueryParameter String qualysUserName) {
        return JenkinsUtil.showErrorIfExists(uIValidation.validateUserName(qualysUserName), isPageLoad);
    }

    public FormValidation doCheckQualysPassword(@QueryParameter String isPageLoad, @QueryParameter String qualysPassword) {
        return JenkinsUtil.showErrorIfExists(uIValidation.validatePassword(qualysPassword), isPageLoad);
    }

    public FormValidation doCheckQualysPlatformURL(@QueryParameter String isPageLoad, @QueryParameter String qualysPlatformURL) {
        try {
            return JenkinsUtil.showErrorIfExists(uIValidation.validatePlatformURL(qualysPlatformURL), isPageLoad);
        } catch (URISyntaxException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
        return FormValidation.ok();
    }

    @POST
    public FormValidation doTestConnection(
            @QueryParameter(value = "qualysPlatformURL") String qualysPlatformURL,
            @QueryParameter(value = "qualysUserName") String qualysUserName,
            @QueryParameter(value = "qualysPassword") String qualysPassword) {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
        try {
            if (StringUtils.isEmpty(qualysPlatformURL) || StringUtils.isEmpty(qualysUserName) || StringUtils.isEmpty(qualysPassword)) {
                return FormValidation.error("PlatformURL, Username and Password are required fields.");
            }
            QualysServiceImpl qualysService = new QualysServiceImpl();
            QualysBuildConfiguration qbc = new QualysBuildConfiguration(qualysPlatformURL, qualysUserName, qualysPassword);
            if (!qualysService.isUserAuthenticated(qbc)) {
                return FormValidation.error("Unable to authenticate user");
            }
        } catch (Exception e) {
            return FormValidation.error((String) ("Client error: " + e.getMessage()));
        }
        return FormValidation.ok((String) "Successfully authenticated user with server");
    }

    private String getPluginVersion() {
        return this.getPlugin().getVersion();
    }

    public String getUUID() {
        return java.util.UUID.randomUUID().toString();
    }
}
