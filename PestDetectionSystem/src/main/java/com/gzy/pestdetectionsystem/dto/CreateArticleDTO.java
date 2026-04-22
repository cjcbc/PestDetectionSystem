package com.gzy.pestdetectionsystem.dto;

import lombok.Data;

@Data
public class CreateArticleDTO {

    private String title;

    private String summary;

    private String content;

    private String coverImage;

    private String category;

    private String tags;
}
