package com.syfproject.img_store.config;

import com.syfproject.img_store.entity.User;
import com.syfproject.img_store.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    public OAuth2LoginSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oauthUser.getAttributes();

        String githubId = attributes.get("id").toString();
        String username = attributes.get("login").toString();
        String email = attributes.get("email") != null ? attributes.get("email").toString() : username + "@github.com";
        String location = attributes.get("location") != null ? attributes.get("location").toString() : "";

        log.info("GitHub ID: {}, Username: {}, Email: {}, Location:{}", githubId, username, email, location);

        userRepository.findByGithubId(githubId)
                .orElseGet(() -> userRepository.save(new User(githubId, username, email, location)));

        response.sendRedirect("/register");
    }
}
