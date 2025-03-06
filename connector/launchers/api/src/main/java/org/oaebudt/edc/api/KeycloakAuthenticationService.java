package org.oaebudt.edc.api;

import org.eclipse.edc.api.auth.spi.AuthenticationService;

import java.util.List;
import java.util.Map;

public class KeycloakAuthenticationService  implements AuthenticationService {


    @Override
    public boolean isAuthenticated(Map<String, List<String>> headers) {
        return false;
    }
}
