package com.syfproject.img_store.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "images")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imgurId;
    private String link;
    private String deleteHash;
    private String filename;

    @ManyToOne()
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
}

