package com.syfproject.img_store.config;

import com.syfproject.img_store.entity.User;
import com.syfproject.img_store.repository.UserRepository;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

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

        LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class).info("\n\n\ngithub {}username {}email {}\n\n\n", githubId, username, email);


        userRepository.findByGithubId(githubId).orElseGet(() -> {
            User newUser = new User(githubId, username, email);
            return userRepository.save(newUser);
        });

        response.sendRedirect("/register");
    }
}

