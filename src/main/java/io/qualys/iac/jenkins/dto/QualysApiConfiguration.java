package io.qualys.iac.jenkins.dto;

import hudson.util.Secret;
import io.qualys.iac.jenkins.Config;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class QualysApiConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    @DataBoundConstructor
    public QualysApiConfiguration(String name, String qualysPlatformURL, String qualysUserName, Secret qualysPassword) {
        this.name = name;
        this.qualysPlatformURL = qualysPlatformURL;
        this.qualysUserName = qualysUserName;
        this.qualysPassword = qualysPassword;
    }
    @DataBoundSetter
    @Setter
    @Getter
    private String name;
    @DataBoundSetter
    @Setter
    @Getter
    private String qualysPlatformURL;
    @DataBoundSetter
    @Setter
    @Getter
    private String qualysUserName;
    @DataBoundSetter
    @Setter
    @Getter
    private Secret qualysPassword;

    public static final QualysApiConfiguration[] all() {
        return Config.get().getQualysApiConfigurations();
    }
}
