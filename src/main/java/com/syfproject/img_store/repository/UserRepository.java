package com.syfproject.img_store.repository;

import com.syfproject.img_store.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<Object> findByGithubId(String githubId);
}
