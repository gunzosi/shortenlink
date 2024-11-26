package boostech.code.component;


import boostech.code.service.serviceImpl.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFacade {

    public UserDetailsImpl getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User isn't authenticated");
        }

        return (UserDetailsImpl) authentication.getPrincipal();
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
