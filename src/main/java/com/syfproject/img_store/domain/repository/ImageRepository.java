package com.syfproject.img_store.domain.repository;

import com.syfproject.img_store.domain.entity.Image;
import com.syfproject.img_store.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByUser(User user);
    Optional<Image> findByDeleteHashAndUser(String deleteHash, User user);
    Optional<Image> findByIdAndUser(Long id, User user);
}
