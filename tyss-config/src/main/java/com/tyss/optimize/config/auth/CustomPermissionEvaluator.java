package com.tyss.optimize.config.auth;

import com.tyss.optimize.common.model.auth.AccessTokenMapper;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.common.util.Privilege;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

import java.io.Serializable;
import java.util.Objects;

public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(
            Authentication auth, Object targetDomainObject, Object permission) {
        if ((auth == null) || (targetDomainObject == null) || !(permission instanceof String)){
            return false;
        }
        String targetType = targetDomainObject.getClass().getSimpleName().toUpperCase();

        return hasPrivilege(auth, targetType, permission.toString().toUpperCase());
    }

    @Override
    public boolean hasPermission(
            Authentication auth, Serializable targetId, String targetType, Object permission) {
        if ((auth == null) || (targetType == null) || !(permission instanceof String)) {
            return false;
        }
        return hasPrivilege(auth, targetType.toUpperCase(),
                permission.toString().toUpperCase());
    }

    private boolean hasPrivilege(Authentication auth, String targetType, String permission) {

        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) auth.getDetails();
        AccessTokenMapper accessTokenMapper  = (AccessTokenMapper) details.getDecodedDetails();
        String privilege = accessTokenMapper.getPrivilege();
        if(Privilege.SUPER_ADMIN.equals(privilege)) {
            return true;
        }
        for (GrantedAuthority grantedAuth : auth.getAuthorities()) {
            String grantAuth = grantedAuth.getAuthority();
            if (Objects.nonNull(grantAuth) && grantAuth.startsWith(targetType)) {

                String grant = grantAuth.substring(grantAuth.indexOf(":")+1, grantAuth.length());

                switch (grant)
                {
                    case CommonConstants.FULL_ACCESS:
                        return true;
                    case CommonConstants.NO_ACCESS:
                        return false;
                    case CommonConstants.WRITE:
                        return true;
                    case CommonConstants.VIEW:
                        return (permission.equals(CommonConstants.WRITE)) ? false : true;
                    default:
                        return false;                        //code
                }
            }
        }
        return false;
    }
}
