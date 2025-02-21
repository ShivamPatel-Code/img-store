package com.syfproject.img_store.dto;

import lombok.Data;

@Data
public class ImgurUploadResponse {
    private String id;
    private String link;
    private String deleteHash;
}
