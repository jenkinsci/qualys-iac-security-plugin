package io.jenkins.plugins.jenkins.dto;

import io.jenkins.plugins.actions.Config;
import hudson.util.Secret;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

@Getter
public class QualysApiConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    @DataBoundConstructor
    public QualysApiConfiguration(String name, String qualysPlatformURL,String authType, String qualysUserName, Secret qualysPassword) {
        this.name = name;
        this.qualysPlatformURL = qualysPlatformURL;
        this.qualysUserName = qualysUserName;
        this.qualysPassword = qualysPassword;
        this.authType =authType;
    }
    @DataBoundSetter
    @Setter
    private String name;
    @DataBoundSetter
    @Setter
    private String qualysPlatformURL;
    @DataBoundSetter
    @Setter
    private String qualysUserName;
    @DataBoundSetter
    @Setter
    private Secret qualysPassword;

    private String authType;

    public void setAuthType(String authType) { this.authType = authType; }

    public static final QualysApiConfiguration[] all() {
        return Config.get().getQualysApiConfigurations();
    }
}
