package io.jenkins.plugins.util;

import java.net.URI;
import java.net.URISyntaxException;

public class Helper {
    public static String getGatewayUrl(String portalUrl) throws IllegalArgumentException {
        try {
            URI uri = new URI(portalUrl);
            String domain = uri.getHost();

            if (domain.contains("gateway"))
                return portalUrl;
            if (!domain.startsWith("qualysguard.")) {
                throw new IllegalArgumentException("Input URL must start with 'https://qualysguard.'");
            }
            String gatewayDomain;
            if (domain.equals("qualysguard.qualys.com"))
                gatewayDomain = "gateway.qg1.apps.qualys.com";
            else if (domain.equals("qualysguard.qualys.eu"))
                gatewayDomain = "gateway.qg1.apps.qualys.eu";
            else if (domain.matches("qualysguard\\.(qg[1-9]\\.)?apps\\.qualys.+")) {
                gatewayDomain = domain.replaceFirst("qualysguard\\.", "gateway.");
            } else if (domain.contains("qualysguard.") && domain.contains("qualys.com")) {
                gatewayDomain = domain.replaceFirst("qualysguard\\.", "gateway.");
            } else {
                //private platform url
                gatewayDomain = domain.replaceFirst("qualysguard\\.", "qualysgateway.");
                return new URI(uri.getScheme(), gatewayDomain, uri.getPath(), uri.getQuery(), uri.getFragment()).toString();
            }

            return new URI(uri.getScheme(), gatewayDomain, uri.getPath(), uri.getQuery(), uri.getFragment()).toString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL format: " + portalUrl, e);
        }
    }
}
