package com.syfproject.img_store.entity;

import com.syfproject.img_store.entity.User;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
@Entity
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String imgurId;
    private String link;

    @ManyToOne
    @JoinColumn(name = "user")
    private User user;

}