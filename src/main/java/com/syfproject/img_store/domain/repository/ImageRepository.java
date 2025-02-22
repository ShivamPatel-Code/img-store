package com.syfproject.img_store.domain.repository;

import com.syfproject.img_store.domain.entity.Image;
import com.syfproject.img_store.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
}
