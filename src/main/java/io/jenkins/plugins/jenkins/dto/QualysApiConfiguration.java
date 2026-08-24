package io.jenkins.plugins.jenkins.dto;

import io.jenkins.plugins.actions.Config;
import hudson.util.Secret;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

@Getter
public class QualysApiConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_SCOPE = "API.ACCESS";

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

    @DataBoundSetter
    @Setter
    private String tokenUrl;

    @DataBoundSetter
    @Setter
    private String scope;

    @DataBoundSetter
    @Setter
    private String audience;

    private String authType;

    public void setAuthType(String authType) { this.authType = authType; }

    public String getScope() {
        return StringUtils.defaultIfBlank(scope, DEFAULT_SCOPE);
    }

    public static final QualysApiConfiguration[] all() {
        return Config.get().getQualysApiConfigurations();
    }
}
